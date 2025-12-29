/**
 * ApiModels.kt
 * 
 * Data Transfer Objects (DTOs) for API communication.
 * 
 * These classes define the structure of data sent to and received from the
 * Limitless server API. They are separate from domain models to decouple
 * the network layer from business logic.
 * 
 * Architecture: Data Layer - Network DTOs (Clean Architecture)
 * 
 * TODO(milestone-1): Add proper JSON serialization annotations
 * TODO(milestone-1): Implement DTO to domain model mappers
 * TODO(milestone-2): Add validation logic
 * TODO(milestone-3): Add support for batch operations
 */

package com.limitless.companion.data.remote.dto

import com.google.gson.annotations.SerializedName

// ============================================================================
// Request DTOs
// ============================================================================

/**
 * Request to create a new transcript.
 */
data class CreateTranscriptRequest(
    @SerializedName("session_id")
    val sessionId: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("start_time_ms")
    val startTimeMs: Long,
    
    @SerializedName("end_time_ms")
    val endTimeMs: Long,
    
    @SerializedName("confidence")
    val confidence: Float,
    
    @SerializedName("language")
    val language: String = "en",
    
    @SerializedName("speaker_id")
    val speakerId: String? = null
)

/**
 * Request to create a new session.
 */
data class CreateSessionRequest(
    @SerializedName("session_id")
    val sessionId: String,
    
    @SerializedName("start_time")
    val startTime: Long,
    
    @SerializedName("device_info")
    val deviceInfo: String,
    
    @SerializedName("bluetooth_device_name")
    val bluetoothDeviceName: String? = null
)

/**
 * Search request.
 */
data class SearchRequest(
    @SerializedName("query")
    val query: String,
    
    @SerializedName("limit")
    val limit: Int = 20,
    
    @SerializedName("offset")
    val offset: Int = 0
)

// ============================================================================
// Response DTOs
// ============================================================================

/**
 * Generic API response wrapper.
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: T? = null
)

/**
 * Transcript DTO from server.
 */
data class TranscriptDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("session_id")
    val sessionId: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("confidence")
    val confidence: Float,
    
    @SerializedName("language")
    val language: String
)

/**
 * Session DTO from server.
 */
data class SessionDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("session_id")
    val sessionId: String,
    
    @SerializedName("start_time")
    val startTime: Long,
    
    @SerializedName("end_time")
    val endTime: Long? = null
)

/**
 * Search result.
 */
data class SearchResultDto(
    @SerializedName("transcript")
    val transcript: TranscriptDto,
    
    @SerializedName("score")
    val score: Float
)

/**
 * Search response.
 */
data class SearchResponseDto(
    @SerializedName("results")
    val results: List<SearchResultDto>,
    
    @SerializedName("total")
    val total: Int
)
