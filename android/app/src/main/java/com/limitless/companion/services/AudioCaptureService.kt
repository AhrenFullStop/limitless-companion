/**
 * AudioCaptureService.kt
 * 
 * Android Foreground Service responsible for capturing audio from Bluetooth devices.
 * 
 * This service manages the complete audio capture pipeline:
 * - Bluetooth SCO (Synchronous Connection-Oriented) setup for headset audio
 * - AudioRecord configuration for raw audio capture
 * - Audio buffer management and streaming
 * - Power management for continuous recording
 * 
 * Architecture: Service Layer (Clean Architecture)
 * 
 * TODO(milestone-1): Implement actual audio capture logic
 * TODO(milestone-1): Add Bluetooth device discovery and connection management
 * TODO(milestone-2): Implement adaptive buffer sizing based on device capabilities
 * TODO(milestone-2): Add audio quality monitoring and automatic adjustment
 */

package com.limitless.companion.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
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
import java.nio.ByteBuffer
// import timber.log.Timber

/**
 * Audio capture state representing the current recording status.
 */
enum class CaptureState {
    IDLE,
    INITIALIZING,
    RECORDING,
    PAUSED,
    STOPPED,
    ERROR
}

/**
 * Configuration for audio capture parameters.
 */
data class AudioCaptureConfig(
    val sampleRate: Int = 16000,  // 16kHz optimal for Whisper
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    val bufferSizeMultiplier: Int = 2  // Multiplier for minimum buffer size
)

/**
 * Foreground service for continuous audio capture from Bluetooth devices.
 * 
 * This service runs as a foreground service to ensure it's not killed by the system
 * during extended recording sessions. It manages Bluetooth SCO connections and
 * provides audio data to consumers via a Flow.
 */
class AudioCaptureService : Service() {

    // Service binding
    private val binder = AudioCaptureBinder()

    // Coroutine scope for service lifecycle
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Audio capture state
    private val _captureState = MutableStateFlow(CaptureState.IDLE)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    // Audio stream
    private val _audioStream = MutableStateFlow<ByteBuffer?>(null)
    val audioStream: StateFlow<ByteBuffer?> = _audioStream.asStateFlow()

    // Audio components
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // Bluetooth components
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothHeadset: BluetoothHeadset? = null
    private var scoConnectionState = BluetoothHeadset.STATE_AUDIO_DISCONNECTED

    // Configuration
    private var config = AudioCaptureConfig()

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "audio_capture_channel"
        private const val CHANNEL_NAME = "Audio Recording"
        private const val WAKE_LOCK_TAG = "LimitlessCompanion::AudioCaptureWakeLock"

        // Audio buffer configuration
        private const val BUFFER_READ_SIZE = 1024  // Bytes to read per cycle
    }

    override fun onCreate() {
        super.onCreate()
        // Timber.d("AudioCaptureService created")
        
        initializeBluetoothComponents()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Timber.d("AudioCaptureService started")
        
        // Start foreground service with notification
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Handle intent actions
        when (intent?.action) {
            ACTION_START_RECORDING -> startRecording()
            ACTION_STOP_RECORDING -> stopRecording()
            ACTION_PAUSE_RECORDING -> pauseRecording()
            ACTION_RESUME_RECORDING -> resumeRecording()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        // Timber.d("AudioCaptureService destroyed")
        
        stopRecording()
        releaseWakeLock()
        disconnectBluetoothSco()
        serviceScope.cancel()
        
        super.onDestroy()
    }

    // Public API

    /**
     * Starts audio recording from the configured source.
     * 
     * @return true if recording started successfully, false otherwise
     */
    fun startRecording(): Boolean {
        if (_captureState.value == CaptureState.RECORDING) {
            // Timber.w("Already recording")
            return true
        }

        _captureState.value = CaptureState.INITIALIZING

        try {
            // TODO(milestone-1): Implement Bluetooth device selection
            // TODO(milestone-1): Check permissions (RECORD_AUDIO, BLUETOOTH_CONNECT)
            
            if (!connectBluetoothSco()) {
                // Timber.e("Failed to connect Bluetooth SCO")
                _captureState.value = CaptureState.ERROR
                return false
            }

            if (!initializeAudioRecord()) {
                // Timber.e("Failed to initialize AudioRecord")
                _captureState.value = CaptureState.ERROR
                return false
            }

            startAudioCapture()
            _captureState.value = CaptureState.RECORDING
            
            // Timber.i("Audio recording started successfully")
            return true

        } catch (e: Exception) {
            // Timber.e(e, "Failed to start recording")
            _captureState.value = CaptureState.ERROR
            return false
        }
    }

    /**
     * Stops audio recording and releases resources.
     */
    fun stopRecording() {
        if (_captureState.value == CaptureState.IDLE || _captureState.value == CaptureState.STOPPED) {
            return
        }

        // Timber.i("Stopping audio recording")

        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        disconnectBluetoothSco()

        _captureState.value = CaptureState.STOPPED
        _audioStream.value = null
    }

    /**
     * Pauses audio recording without releasing resources.
     * 
     * TODO(milestone-2): Implement pause functionality
     */
    fun pauseRecording() {
        if (_captureState.value != CaptureState.RECORDING) {
            return
        }

        // Timber.i("Pausing audio recording")
        
        // TODO(milestone-2): Implement pause logic
        _captureState.value = CaptureState.PAUSED
    }

    /**
     * Resumes audio recording after pause.
     * 
     * TODO(milestone-2): Implement resume functionality
     */
    fun resumeRecording() {
        if (_captureState.value != CaptureState.PAUSED) {
            return
        }

        // Timber.i("Resuming audio recording")
        
        // TODO(milestone-2): Implement resume logic
        _captureState.value = CaptureState.RECORDING
    }

    /**
     * Updates the audio capture configuration.
     * 
     * @param newConfig New configuration to apply
     * @return true if config was updated successfully
     * 
     * TODO(milestone-2): Implement dynamic configuration updates
     */
    fun updateConfiguration(newConfig: AudioCaptureConfig): Boolean {
        if (_captureState.value == CaptureState.RECORDING) {
            // Timber.w("Cannot update config while recording")
            return false
        }

        config = newConfig
        // Timber.d("Audio config updated: $config")
        return true
    }

    // Private implementation

    /**
     * Initializes Bluetooth components for headset audio.
     * 
     * TODO(milestone-1): Implement full Bluetooth profile management
     */
    private fun initializeBluetoothComponents() {
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            
            if (bluetoothAdapter == null) {
                // Timber.e("Bluetooth not supported on this device")
                return
            }

            // TODO(milestone-1): Register Bluetooth profile listener
            // bluetoothAdapter?.getProfileProxy(this, profileListener, BluetoothProfile.HEADSET)

        } catch (e: Exception) {
            // Timber.e(e, "Failed to initialize Bluetooth components")
        }
    }

    /**
     * Connects Bluetooth SCO for audio streaming.
     * 
     * @return true if connection initiated successfully
     * 
     * TODO(milestone-1): Implement SCO connection with timeout and retry logic
     */
    private fun connectBluetoothSco(): Boolean {
        // TODO(milestone-1): Implement Bluetooth SCO connection
        // - Start SCO audio connection
        // - Wait for STATE_AUDIO_CONNECTED
        // - Handle connection failures
        
        // Timber.d("Connecting Bluetooth SCO...")
        
        return true  // Stub: assume success
    }

    /**
     * Disconnects Bluetooth SCO audio.
     */
    private fun disconnectBluetoothSco() {
        try {
            // TODO(milestone-1): Implement SCO disconnection
            // bluetoothAdapter?.stopBluetoothSco()
            
            // Timber.d("Bluetooth SCO disconnected")
        } catch (e: Exception) {
            // Timber.e(e, "Failed to disconnect Bluetooth SCO")
        }
    }

    /**
     * Initializes AudioRecord for capturing audio.
     * 
     * @return true if initialized successfully
     * 
     * TODO(milestone-1): Implement AudioRecord initialization with proper error handling
     */
    private fun initializeAudioRecord(): Boolean {
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                config.sampleRate,
                config.channelConfig,
                config.audioFormat
            )

            if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                // Timber.e("Invalid AudioRecord configuration")
                return false
            }

            val bufferSize = minBufferSize * config.bufferSizeMultiplier

            // TODO(milestone-1): Implement actual AudioRecord initialization
            // audioRecord = AudioRecord(
            //     MediaRecorder.AudioSource.VOICE_COMMUNICATION,  // Use voice communication source for BT
            //     config.sampleRate,
            //     config.channelConfig,
            //     config.audioFormat,
            //     bufferSize
            // )

            // if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            //     Timber.e("AudioRecord not initialized properly")
            //     return false
            // }

            // Timber.d("AudioRecord initialized: sampleRate=${config.sampleRate}, bufferSize=$bufferSize")
            return true

        } catch (e: Exception) {
            // Timber.e(e, "Failed to initialize AudioRecord")
            return false
        }
    }

    /**
     * Starts the audio capture loop in a coroutine.
     * 
     * TODO(milestone-1): Implement actual audio capture loop
     */
    private fun startAudioCapture() {
        recordingJob = serviceScope.launch {
            // TODO(milestone-1): Implement audio capture loop
            // - Read from AudioRecord
            // - Package into ByteBuffer
            // - Emit via _audioStream
            // - Handle buffer overflows
            // - Monitor audio quality
            
            // Timber.d("Audio capture loop started")

            val buffer = ByteArray(BUFFER_READ_SIZE)
            
            try {
                audioRecord?.startRecording()

                while (isActive && _captureState.value == CaptureState.RECORDING) {
                    // Stub: simulate reading audio
                    // val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    
                    // if (bytesRead > 0) {
                    //     val byteBuffer = ByteBuffer.wrap(buffer.copyOf(bytesRead))
                    //     _audioStream.value = byteBuffer
                    // }

                    delay(100)  // Stub: prevent busy loop
                }

            } catch (e: Exception) {
                // Timber.e(e, "Error in audio capture loop")
                _captureState.value = CaptureState.ERROR
            }
        }
    }

    /**
     * Acquires a partial wake lock to keep CPU running during recording.
     */
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            WAKE_LOCK_TAG
        ).apply {
            acquire(10 * 60 * 1000L)  // 10 minutes timeout
        }
        // Timber.d("Wake lock acquired")
    }

    /**
     * Releases the wake lock.
     */
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                // Timber.d("Wake lock released")
            }
        }
        wakeLock = null
    }

    /**
     * Creates the foreground service notification.
     * 
     * TODO(milestone-1): Improve notification with actions and status
     */
    private fun createNotification(): Notification {
        createNotificationChannel()

        // TODO(milestone-1): Add notification actions (pause, stop)
        val intent = Intent(this, AudioCaptureService::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Limitless Companion")
            .setContentText("Recording audio from Bluetooth device")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)  // TODO: Use app icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Creates the notification channel for Android O and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when Limitless Companion is recording audio"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Binder for bound clients

    inner class AudioCaptureBinder : Binder() {
        fun getService(): AudioCaptureService = this@AudioCaptureService
    }

    // Intent actions
    companion object Actions {
        const val ACTION_START_RECORDING = "com.limitless.companion.ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.limitless.companion.ACTION_STOP_RECORDING"
        const val ACTION_PAUSE_RECORDING = "com.limitless.companion.ACTION_PAUSE_RECORDING"
        const val ACTION_RESUME_RECORDING = "com.limitless.companion.ACTION_RESUME_RECORDING"
    }
}
