/**
 * AudioCaptureService.kt
 *
 * Foreground service for continuous audio capture from Bluetooth devices,
 * with automatic fallback to the device microphone.
 *
 * Records audio in 30-second chunks, transcribes each chunk via WhisperService,
 * and persists transcripts to the local Room database.
 */

package com.limitless.companion.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.limitless.companion.data.local.preferences.AppPreferences
import com.limitless.companion.data.remote.NetworkClient
import com.limitless.companion.data.remote.DeviceRegisterRequest
import com.limitless.companion.data.remote.TranscriptUploadRequest
import android.provider.Settings
import java.nio.ByteBuffer
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import com.limitless.companion.data.local.db.SessionEntity
import com.limitless.companion.data.local.db.TranscriptDatabase
import com.limitless.companion.data.local.db.TranscriptEntity
import java.util.UUID

enum class CaptureState {
    IDLE, INITIALIZING, RECORDING, PAUSED, STOPPED, ERROR
}

enum class AudioSource {
    BLUETOOTH, MICROPHONE, NONE
}

data class AudioCaptureConfig(
    val sampleRate: Int = 16000,
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    val bufferSizeMultiplier: Int = 2
)

class AudioCaptureService : Service() {

    private val binder = AudioCaptureBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _captureState = MutableStateFlow(CaptureState.IDLE)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    private val _audioSource = MutableStateFlow(AudioSource.NONE)
    val audioSource: StateFlow<AudioSource> = _audioSource.asStateFlow()

    private val _transcriptCount = MutableStateFlow(0)
    val transcriptCount: StateFlow<Int> = _transcriptCount.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioManager: AudioManager? = null

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scoReceiver: BroadcastReceiver? = null
    private var scoConnected = false

    private lateinit var whisperService: WhisperService
    private lateinit var db: TranscriptDatabase
    private var currentSessionId: String? = null
    private lateinit var appPreferences: AppPreferences
    private var deviceId: String = ""

    private var config = AudioCaptureConfig()

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "audio_capture_channel"
        private const val CHANNEL_NAME = "Audio Recording"
        private const val WAKE_LOCK_TAG = "LimitlessCompanion::AudioCaptureWakeLock"
        private const val BUFFER_READ_SIZE = 1024
        private const val TAG = "AudioCaptureService"

        const val ACTION_START_RECORDING = "com.limitless.companion.ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.limitless.companion.ACTION_STOP_RECORDING"
        const val ACTION_PAUSE_RECORDING = "com.limitless.companion.ACTION_PAUSE_RECORDING"
        const val ACTION_RESUME_RECORDING = "com.limitless.companion.ACTION_RESUME_RECORDING"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AudioCaptureService created")

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        db = TranscriptDatabase.getInstance(this)
        whisperService = WhisperService(this)

        // Register SCO state receiver
        scoReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val state = intent?.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                scoConnected = (state == AudioManager.SCO_AUDIO_STATE_CONNECTED)
                Log.d(TAG, "SCO state changed: connected=$scoConnected")
            }
        }
        @Suppress("DEPRECATION")
        registerReceiver(scoReceiver, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED))

        appPreferences = AppPreferences(this)
        // Hardcode the RunOS server URL for now if not set
        if (appPreferences.getServerUrl() == "https://api.limitless.app") {
            appPreferences.setServerUrl("https://app-z0dhx-8000.tdc.zoskw.beta.runos.xyz")
        }

        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"

        // Download model and initialize Whisper in the background
        serviceScope.launch {
            // Register device
            try {
                val api = NetworkClient.getApiService(appPreferences.getServerUrl())
                val response = api.registerDevice(DeviceRegisterRequest(deviceId, android.os.Build.MODEL))
                Log.d(TAG, "Device registration status: ${response.code()}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register device: ${e.message}")
            }

            val modelPath = whisperService.downloadModel(WhisperService.DEFAULT_MODEL_NAME)
            if (modelPath != null) {
                val result = whisperService.initialize(WhisperConfig(modelPath = modelPath))
                Log.d(TAG, "Whisper initialized: $result")
                // Update transcript count from DB
                _transcriptCount.value = db.transcriptDao().count()
                // Auto-start recording once the model is ready
                if (result) {
                    startRecording()
                }
            } else {
                Log.e(TAG, "Failed to download Whisper model")
            }
        }

        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        when (intent?.action) {
            ACTION_START_RECORDING -> startRecording()
            ACTION_STOP_RECORDING -> stopRecording()
            ACTION_PAUSE_RECORDING -> pauseRecording()
            ACTION_RESUME_RECORDING -> resumeRecording()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        stopRecording()
        releaseWakeLock()
        scoReceiver?.let { unregisterReceiver(it) }
        disconnectBluetoothSco()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ---- Public API ----

    fun startRecording(): Boolean {
        if (_captureState.value == CaptureState.RECORDING) return true

        _captureState.value = CaptureState.INITIALIZING

        // Try Bluetooth SCO (non-blocking — best effort)
        val scoAttempted = tryConnectBluetoothSco()

        if (!initializeAudioRecord(useSco = scoAttempted && scoConnected)) {
            // SCO might not be ready yet — fall back to mic
            Log.w(TAG, "AudioRecord init failed with SCO=$scoAttempted, trying plain mic")
            if (!initializeAudioRecord(useSco = false)) {
                Log.e(TAG, "AudioRecord init failed on both sources")
                _captureState.value = CaptureState.ERROR
                return false
            }
        }

        // Create a recording session in DB
        val sessionId = UUID.randomUUID().toString()
        currentSessionId = sessionId
        serviceScope.launch {
            db.sessionDao().insert(
                SessionEntity(
                    id = sessionId,
                    startedAt = System.currentTimeMillis(),
                    audioSource = _audioSource.value.name
                )
            )
        }

        startAudioCapture()
        _captureState.value = CaptureState.RECORDING
        Log.i(TAG, "Recording started, source=${_audioSource.value}")
        return true
    }

    fun stopRecording() {
        if (_captureState.value == CaptureState.IDLE || _captureState.value == CaptureState.STOPPED) return

        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        disconnectBluetoothSco()
        _captureState.value = CaptureState.STOPPED

        // Close the session
        val sid = currentSessionId
        if (sid != null) {
            serviceScope.launch {
                val session = db.sessionDao().getActiveSession()
                if (session != null) {
                    db.sessionDao().update(session.copy(endedAt = System.currentTimeMillis()))
                }
            }
            currentSessionId = null
        }
    }

    fun pauseRecording() {
        if (_captureState.value == CaptureState.RECORDING) {
            _captureState.value = CaptureState.PAUSED
        }
    }

    fun resumeRecording() {
        if (_captureState.value == CaptureState.PAUSED) {
            _captureState.value = CaptureState.RECORDING
        }
    }

    // ---- Private helpers ----

    /**
     * Attempt Bluetooth SCO — best effort, returns whether attempt was made.
     * Does NOT block or fail if unavailable.
     */
    private fun tryConnectBluetoothSco(): Boolean {
        return try {
            if (bluetoothAdapter?.isEnabled == true) {
                audioManager?.startBluetoothSco()
                audioManager?.isBluetoothScoOn = true
                Log.d(TAG, "Bluetooth SCO requested")
                true
            } else {
                Log.d(TAG, "Bluetooth not enabled, skipping SCO")
                false
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "No BLUETOOTH_CONNECT permission, skipping SCO: ${e.message}")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start SCO: ${e.message}")
            false
        }
    }

    private fun disconnectBluetoothSco() {
        try {
            audioManager?.stopBluetoothSco()
            audioManager?.isBluetoothScoOn = false
            Log.d(TAG, "Bluetooth SCO disconnected")
        } catch (e: Exception) {
            Log.w(TAG, "Error disconnecting SCO: ${e.message}")
        }
    }

    private fun initializeAudioRecord(useSco: Boolean): Boolean {
        // Audio sources to try in order of preference for non-SCO recording
        val sourcesToTry = if (useSco) {
            listOf(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
        } else {
            listOf(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION, // good near-field, used for VOIP calls
                MediaRecorder.AudioSource.VOICE_RECOGNITION,   // AGC on, tuned for speech recognition
                MediaRecorder.AudioSource.MIC                   // standard fallback
            )
        }

        val minBuf = AudioRecord.getMinBufferSize(
            config.sampleRate, config.channelConfig, config.audioFormat
        )
        if (minBuf == AudioRecord.ERROR_BAD_VALUE) return false

        val bufferSize = minBuf * config.bufferSizeMultiplier

        for (source in sourcesToTry) {
            try {
                val record = AudioRecord(
                    source, config.sampleRate, config.channelConfig, config.audioFormat, bufferSize
                )
                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord = record
                    _audioSource.value = if (useSco) AudioSource.BLUETOOTH else AudioSource.MICROPHONE
                    Log.d(TAG, "AudioRecord initialized: source=$source (${_audioSource.value}), bufferSize=$bufferSize")
                    return true
                }
                record.release()
                Log.w(TAG, "AudioRecord source $source failed to initialize, trying next")
            } catch (e: SecurityException) {
                Log.e(TAG, "RECORD_AUDIO permission denied: ${e.message}")
                return false
            } catch (e: Exception) {
                Log.w(TAG, "AudioRecord source $source threw ${e.message}, trying next")
            }
        }
        Log.e(TAG, "All audio sources exhausted")
        return false
    }

    private fun startAudioCapture() {
        recordingJob = serviceScope.launch {
            val buffer = ByteArray(BUFFER_READ_SIZE)
            val bytesPerChunk = config.sampleRate * 2 * 30 // 30s of 16-bit mono PCM
            val pcmAccumulator = java.io.ByteArrayOutputStream(bytesPerChunk)
            var chunkStartTime = System.currentTimeMillis()

            try {
                audioRecord?.startRecording()
                Log.d(TAG, "Audio capture loop started")

                while (isActive && _captureState.value == CaptureState.RECORDING) {
                    val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                    if (bytesRead > 0) {
                        pcmAccumulator.write(buffer, 0, bytesRead)

                        // When we've accumulated ~30s of audio, transcribe it
                        if (pcmAccumulator.size() >= bytesPerChunk) {
                            val chunkEndTime = System.currentTimeMillis()
                            val pcmData = pcmAccumulator.toByteArray()
                            val chunkDurationMs = chunkEndTime - chunkStartTime
                            val capturedSessionId = currentSessionId

                            // Calculate RMS amplitude to detect silence
                            val rms = calculateRms(pcmData)
                            val peak = calculatePeak(pcmData)
                            Log.d(TAG, "Chunk RMS: %.0f, Peak: %d (speech threshold RMS>200)".format(rms, peak))

                            if (rms < 200.0) {
                                // Near-silent — skip Whisper to avoid hallucinations
                                Log.d(TAG, "Chunk is near-silent (RMS=$rms), skipping transcription")
                            } else {
                                Log.d(TAG, "Starting transcription of ${pcmData.size / 1024}KB chunk (RMS=$rms)")
                                // Transcribe in parallel — don't block the capture loop
                                launch {
                                    if (whisperService.modelState.value == ModelState.READY && capturedSessionId != null) {
                                        val result = whisperService.transcribe(ByteBuffer.wrap(pcmData))
                                        if (result != null && result.text.isNotBlank()) {
                                            val entity = TranscriptEntity(
                                                id = UUID.randomUUID().toString(),
                                                sessionId = capturedSessionId,
                                                text = result.text.trim(),
                                                startTime = chunkStartTime,
                                                durationMs = chunkDurationMs,
                                                source = "on_device"
                                            )
                                            db.transcriptDao().insert(entity)
                                            _transcriptCount.value = db.transcriptDao().count()
                                            Log.i(TAG, "Transcript saved locally: \"${result.text.take(80)}...\"")
                                            
                                            // Upload to server
                                            serviceScope.launch {
                                                uploadToServer(entity)
                                            }
                                        } else {
                                            Log.d(TAG, "Whisper returned empty/blank result for this chunk")
                                        }
                                    }
                                }
                            }

                            pcmAccumulator.reset()
                            chunkStartTime = System.currentTimeMillis()
                        }

                    }

                    delay(10)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in audio capture loop: ${e.message}")
                _captureState.value = CaptureState.ERROR
            }
        }
    }

    /**
     * Calculate RMS amplitude of 16-bit little-endian PCM data.
     * Values range 0-32768. Quiet room ≈ 100-500, speech ≈ 1000-5000.
     */
    private fun calculateRms(pcm: ByteArray): Double {
        var sum = 0.0
        var i = 0
        var count = 0
        while (i + 1 < pcm.size) {
            val sample = (pcm[i].toInt() and 0xFF) or (pcm[i + 1].toInt() shl 8)
            sum += sample.toDouble() * sample.toDouble()
            i += 2
            count++
        }
        return if (count == 0) 0.0 else Math.sqrt(sum / count)
    }

    private fun calculatePeak(pcm: ByteArray): Int {
        var peak = 0
        var i = 0
        while (i + 1 < pcm.size) {
            val sample = Math.abs((pcm[i].toInt() and 0xFF) or (pcm[i + 1].toInt() shl 8))
            if (sample > peak) peak = sample
            i += 2
        }
        return peak
    }

    private suspend fun uploadToServer(transcript: TranscriptEntity) {
        try {
            val serverUrl = appPreferences.getServerUrl()
            val api = NetworkClient.getApiService(serverUrl)
            val request = TranscriptUploadRequest(
                session_id = transcript.sessionId,
                text = transcript.text,
                start_time = transcript.startTime,
                duration_ms = transcript.durationMs,
                source = "on_device"
            )
            val response = api.uploadTranscript(request)
            if (response.isSuccessful) {
                Log.i(TAG, "Transcript uploaded to server: ${response.body()?.id}")
            } else {
                Log.e(TAG, "Failed to upload transcript: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading transcript: ${e.message}")
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        val intent = Intent(this, Class.forName("com.limitless.companion.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Limitless Companion")
            .setContentText("Tap to open · Audio source: ${_audioSource.value.name.lowercase().replaceFirstChar { it.uppercase() }}")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shows when Limitless Companion is recording"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    inner class AudioCaptureBinder : Binder() {
        fun getService(): AudioCaptureService = this@AudioCaptureService
    }
}
