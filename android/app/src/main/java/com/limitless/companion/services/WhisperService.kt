/**
 * WhisperService.kt
 * 
 * Service layer wrapper for Whisper.cpp JNI integration.
 * 
 * This service provides a high-level Kotlin API for the Whisper speech-to-text engine,
 * abstracting away the complexities of JNI interactions and native code management.
 * 
 * Key responsibilities:
 * - Model loading and initialization
 * - Audio data preprocessing
 * - Transcription request management
 * - Result parsing and formatting
 * - Error handling and recovery
 * 
 * Architecture: Service Layer (Clean Architecture)
 * 
 * TODO(milestone-1): Implement actual JNI bindings to whisper.cpp
 * TODO(milestone-1): Add model download and management
 * TODO(milestone-2): Implement streaming transcription
 * TODO(milestone-2): Add model quantization support for reduced memory usage
 * TODO(milestone-3): Implement speaker diarization integration
 */

package com.limitless.companion.services

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.nio.ByteBuffer
import android.util.Log

/**
 * Whisper model state representing the current model status.
 */
enum class ModelState {
    UNINITIALIZED,
    DOWNLOADING,
    LOADING,
    READY,
    ERROR
}

/**
 * Whisper transcription result.
 * 
 * @property text The transcribed text
 * @property confidence Confidence score (0.0 to 1.0)
 * @property language Detected language code (e.g., "en", "es")
 * @property segments Optional list of time-segmented transcriptions
 * @property processingTimeMs Time taken for transcription in milliseconds
 */
data class TranscriptionResult(
    val text: String,
    val confidence: Float = 0.0f,
    val language: String = "en",
    val segments: List<TranscriptionSegment>? = null,
    val processingTimeMs: Long = 0L
)

/**
 * A time-segmented portion of the transcription.
 * 
 * @property text The text for this segment
 * @property startTimeMs Start time in milliseconds
 * @property endTimeMs End time in milliseconds
 * @property confidence Confidence score for this segment
 */
data class TranscriptionSegment(
    val text: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val confidence: Float = 0.0f
)

/**
 * Configuration for Whisper transcription.
 * 
 * @property modelPath Path to the Whisper model file
 * @property language Language code to use (null for auto-detect)
 * @property threads Number of threads to use for inference
 * @property enableTimestamps Whether to generate word-level timestamps
 * @property maxSegmentLength Maximum segment length in seconds
 * @property temperature Temperature for sampling (affects randomness)
 */
data class WhisperConfig(
    val modelPath: String,
    val language: String? = null,
    val threads: Int = 4,
    val enableTimestamps: Boolean = true,
    val maxSegmentLength: Int = 30,
    val temperature: Float = 0.0f
)

/**
 * Service for managing Whisper.cpp integration and transcription operations.
 * 
 * This service handles the lifecycle of Whisper models and provides methods
 * for transcribing audio data using the whisper.cpp library via JNI.
 */
class WhisperService(private val context: Context) {

    // Model state
    private val _modelState = MutableStateFlow(ModelState.UNINITIALIZED)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    // Configuration
    private var config: WhisperConfig? = null

    // Native context pointer (from JNI)
    private var nativeContextPtr: Long = 0L

    // Coroutine scope
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        // Model information
        const val DEFAULT_MODEL_NAME = "ggml-base.en.bin"
        const val MODEL_DIRECTORY = "whisper_models"
        
        // Audio format expected by Whisper
        const val WHISPER_SAMPLE_RATE = 16000  // 16kHz
        const val WHISPER_CHANNELS = 1  // Mono
        
        init {
            // Load native library
            System.loadLibrary("whisper_android")
        }
    }

    /**
     * Initializes the Whisper service with the specified configuration.
     * 
     * @param config Configuration for Whisper
     * @return true if initialization successful
     * 
     * TODO(milestone-1): Implement actual model loading via JNI
     */
    suspend fun initialize(config: WhisperConfig): Boolean = withContext(Dispatchers.IO) {
        if (_modelState.value == ModelState.READY) {
            // Timber.w("Whisper already initialized")
            return@withContext true
        }

        _modelState.value = ModelState.LOADING
        this@WhisperService.config = config

        try {
            // Timber.d("Initializing Whisper with model: ${config.modelPath}")

            // Validate model file exists
            val modelFile = File(config.modelPath)
            if (!modelFile.exists()) {
                // Timber.e("Model file not found: ${config.modelPath}")
                _modelState.value = ModelState.ERROR
                return@withContext false
            }

            // Initialize native Whisper context via JNI
            nativeContextPtr = nativeInitialize(
                modelPath = config.modelPath,
                useGPU = false
            )

            if (nativeContextPtr == 0L) {
                Log.e("WhisperService", "Failed to initialize native Whisper context")
                _modelState.value = ModelState.ERROR
                return@withContext false
            }

            _modelState.value = ModelState.READY
            // Timber.i("Whisper initialized successfully")
            return@withContext true

        } catch (e: Exception) {
            // Timber.e(e, "Failed to initialize Whisper")
            _modelState.value = ModelState.ERROR
            return@withContext false
        }
    }

    /**
     * Transcribes audio data using Whisper.
     * 
     * @param audioData Raw PCM audio data (16kHz, mono, 16-bit)
     * @return TranscriptionResult or null if transcription failed
     * 
     * TODO(milestone-1): Implement actual transcription via JNI
     */
    suspend fun transcribe(audioData: ByteBuffer): TranscriptionResult? = withContext(Dispatchers.Default) {
        if (_modelState.value != ModelState.READY) {
            // Timber.e("Whisper not ready for transcription")
            return@withContext null
        }

        if (nativeContextPtr == 0L) {
            // Timber.e("Invalid native context pointer")
            return@withContext null
        }

        val startTime = System.currentTimeMillis()

        try {
            // Timber.d("Starting transcription of ${audioData.remaining()} bytes")

            // Convert ByteBuffer to float array (required by whisper.cpp)
            val audioFloats = convertPcmToFloat(audioData)

            // Call native transcription method via JNI
            val result = nativeTranscribe(
                contextPtr = nativeContextPtr,
                audioData = audioFloats,
                language = config?.language,
                threads = config?.threads ?: 4
            )

            val processingTime = System.currentTimeMillis() - startTime
            
            val finalResult = result?.copy(processingTimeMs = processingTime)

            Log.i("WhisperService", "Transcription completed in ${processingTime}ms")
            return@withContext finalResult

        } catch (e: Exception) {
            // Timber.e(e, "Transcription failed")
            return@withContext null
        }
    }

    /**
     * Transcribes audio data in streaming mode.
     * 
     * @param audioChunk Chunk of audio data
     * @return Partial transcription result or null
     * 
     * TODO(milestone-2): Implement streaming transcription
     */
    suspend fun transcribeStreaming(audioChunk: ByteBuffer): TranscriptionResult? {
        // TODO(milestone-2): Implement streaming transcription
        // This will require buffering audio chunks and processing them incrementally
        
        // Timber.d("Streaming transcription not yet implemented")
        return null
    }

    /**
     * Downloads a Whisper model from a remote source.
     * 
     * @param modelName Name of the model to download
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return Path to downloaded model or null if failed
     * 
     * TODO(milestone-1): Implement model download functionality
     */
    suspend fun downloadModel(
        modelName: String,
        onProgress: (Float) -> Unit = {}
    ): String? = withContext(Dispatchers.IO) {
        _modelState.value = ModelState.DOWNLOADING

        try {
            // Timber.d("Downloading model: $modelName")

            val modelDir = File(context.filesDir, MODEL_DIRECTORY)
            if (!modelDir.exists()) {
                modelDir.mkdirs()
            }

            val modelFile = File(modelDir, modelName)
            if (modelFile.exists()) {
                Log.d("WhisperService", "Model already exists at ${modelFile.absolutePath}")
                _modelState.value = ModelState.UNINITIALIZED
                return@withContext modelFile.absolutePath
            }

            // Using Hugging Face whisper.cpp base.en model URL
            // Users can customize this, but we'll default to a known good URL for ggml-base.en.bin
            val modelUrlStr = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/$modelName"
            val url = java.net.URL(modelUrlStr)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            
            if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                Log.e("WhisperService", "Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                _modelState.value = ModelState.ERROR
                return@withContext null
            }
            
            val fileLength = connection.contentLength
            val input = connection.inputStream
            val output = java.io.FileOutputStream(modelFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            
            while (input.read(data).also { count = it } != -1) {
                if (!isActive) {
                    input.close()
                    output.close()
                    modelFile.delete()
                    return@withContext null
                }
                total += count
                output.write(data, 0, count)
                if (fileLength > 0) {
                    onProgress((total.toFloat() / fileLength.toFloat()))
                }
            }

            output.flush()
            output.close()
            input.close()
            connection.disconnect()

            _modelState.value = ModelState.UNINITIALIZED
            Log.i("WhisperService", "Model downloaded to: ${modelFile.absolutePath}")
            return@withContext modelFile.absolutePath

        } catch (e: Exception) {
            // Timber.e(e, "Failed to download model")
            _modelState.value = ModelState.ERROR
            return@withContext null
        }
    }

    /**
     * Lists available local models.
     * 
     * @return List of model file paths
     */
    fun getAvailableModels(): List<String> {
        val modelDir = File(context.filesDir, MODEL_DIRECTORY)
        if (!modelDir.exists()) {
            return emptyList()
        }

        return modelDir.listFiles()
            ?.filter { it.extension == "bin" }
            ?.map { it.absolutePath }
            ?: emptyList()
    }

    /**
     * Releases Whisper resources and cleans up.
     * 
     * TODO(milestone-1): Implement native resource cleanup via JNI
     */
    fun release() {
        try {
            // Timber.d("Releasing Whisper resources")

            if (nativeContextPtr != 0L) {
                nativeRelease(nativeContextPtr)
                nativeContextPtr = 0L
            }

            _modelState.value = ModelState.UNINITIALIZED
            serviceScope.cancel()

            // Timber.i("Whisper resources released")

        } catch (e: Exception) {
            // Timber.e(e, "Error releasing Whisper resources")
        }
    }

    // Private helper methods

    /**
     * Converts PCM 16-bit audio to float array normalized to [-1.0, 1.0].
     * 
     * Whisper.cpp expects audio as float array with values in [-1, 1] range.
     * 
     * @param pcmData Raw PCM audio data
     * @return Float array normalized to [-1.0, 1.0]
     */
    private fun convertPcmToFloat(pcmData: ByteBuffer): FloatArray {
        // AudioRecord writes PCM in little-endian byte order.
        // ByteBuffer default is big-endian, so we MUST set LITTLE_ENDIAN
        // before interpreting as ShortBuffer, or every sample's bytes will
        // be swapped and Whisper will receive garbage /hallucinate non-speech.
        val ordered = pcmData.duplicate().apply {
            rewind()
            order(java.nio.ByteOrder.LITTLE_ENDIAN)
        }
        val shortBuffer = ordered.asShortBuffer()
        val floatArray = FloatArray(shortBuffer.remaining())

        for (i in floatArray.indices) {
            // Normalize 16-bit signed PCM → float [-1.0, 1.0]
            floatArray[i] = shortBuffer.get(i) / 32768.0f
        }

        return floatArray
    }

    /**
     * Validates audio format is compatible with Whisper.
     * 
     * @param sampleRate Sample rate in Hz
     * @param channels Number of channels
     * @return true if format is valid
     */
    fun validateAudioFormat(sampleRate: Int, channels: Int): Boolean {
        if (sampleRate != WHISPER_SAMPLE_RATE) {
            // Timber.w("Invalid sample rate: $sampleRate (expected $WHISPER_SAMPLE_RATE)")
            return false
        }

        if (channels != WHISPER_CHANNELS) {
            // Timber.w("Invalid channel count: $channels (expected $WHISPER_CHANNELS)")
            return false
        }

        return true
    }

    // Native methods
    private external fun nativeInitialize(modelPath: String, useGPU: Boolean): Long

    private external fun nativeTranscribe(
        contextPtr: Long,
        audioData: FloatArray,
        language: String?,
        threads: Int
    ): TranscriptionResult?

    private external fun nativeRelease(contextPtr: Long)
    private external fun nativeGetVersion(): String
}
