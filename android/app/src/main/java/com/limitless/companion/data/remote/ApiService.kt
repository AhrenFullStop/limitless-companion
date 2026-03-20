package com.limitless.companion.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class TranscriptUploadRequest(
    val session_id: String,
    val text: String,
    val start_time: Long,
    val duration_ms: Long,
    val source: String = "on_device"
)

data class TranscriptResponse(
    val id: String,
    val text: String,
    val created_at: String
)

data class DeviceRegisterRequest(
    val device_id: String,
    val name: String? = null
)

interface ApiService {
    @POST("transcripts/")
    suspend fun uploadTranscript(@Body request: TranscriptUploadRequest): Response<TranscriptResponse>

    @POST("transcripts/register-device")
    suspend fun registerDevice(@Body request: DeviceRegisterRequest): Response<Unit>
}
