package com.limitless.companion.services

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.zip.GZIPOutputStream
import android.util.Log

class WavWriter(private val outputFile: File, private val sampleRate: Int, private val channels: Int) {
    private var dataSize = 0
    private var outputStream = FileOutputStream(outputFile)

    init {
        writeWavHeader()
    }

    fun write(data: ByteArray, length: Int) {
        outputStream.write(data, 0, length)
        dataSize += length
    }

    fun closeAndCompress(): File {
        outputStream.close()
        updateWavHeader()
        return compressFile(outputFile)
    }

    private fun writeWavHeader() {
        val header = ByteArray(44)
        val audioFormat: Short = 1 // PCM
        val bitsPerSample: Short = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = (channels * bitsPerSample / 8).toShort()

        // Generate basic WAV Header without sizes
        val headerBuffer = java.nio.ByteBuffer.wrap(header).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        headerBuffer.put("RIFF".toByteArray())
        headerBuffer.putInt(0) // totalDataLen
        headerBuffer.put("WAVE".toByteArray())
        headerBuffer.put("fmt ".toByteArray())
        headerBuffer.putInt(16) // Subchunk1Size for PCM
        headerBuffer.putShort(audioFormat)
        headerBuffer.putShort(channels.toShort())
        headerBuffer.putInt(sampleRate)
        headerBuffer.putInt(byteRate)
        headerBuffer.putShort(blockAlign)
        headerBuffer.putShort(bitsPerSample)
        headerBuffer.put("data".toByteArray())
        headerBuffer.putInt(0) // subchunk2Size

        outputStream.write(header)
    }

    private fun updateWavHeader() {
        try {
            val randomAccessFile = RandomAccessFile(outputFile, "rw")
            randomAccessFile.seek(4) // Move to totalDataLen
            val totalDataLen = 36 + dataSize
            randomAccessFile.writeInt(Integer.reverseBytes(totalDataLen))
            randomAccessFile.seek(40) // Move to subchunk2Size
            randomAccessFile.writeInt(Integer.reverseBytes(dataSize))
            randomAccessFile.close()
        } catch (e: Exception) {
            Log.e("WavWriter", "Failed to update WAV header", e)
        }
    }

    private fun compressFile(wavFile: File): File {
        val gzippedFile = File(wavFile.parent, wavFile.name + ".gz")
        try {
            GZIPOutputStream(FileOutputStream(gzippedFile)).use { out ->
                wavFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }
            wavFile.delete()
        } catch (e: Exception) {
            Log.e("WavWriter", "Failed to compress WAV file", e)
        }
        return gzippedFile
    }
}
