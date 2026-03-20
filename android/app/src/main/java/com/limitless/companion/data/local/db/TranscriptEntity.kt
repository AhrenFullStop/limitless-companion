package com.limitless.companion.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transcripts",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class TranscriptEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val text: String,
    val startTime: Long,       // epoch ms when transcription started
    val durationMs: Long = 0L, // how many ms of audio this covers
    val source: String = "on_device", // "on_device" or "remote_api"
    val confidence: Float = 0f
)
