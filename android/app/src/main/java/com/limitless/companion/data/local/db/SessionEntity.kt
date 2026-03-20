package com.limitless.companion.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,       // epoch ms
    val endedAt: Long? = null, // null while still active
    val audioSource: String = "UNKNOWN" // "BLUETOOTH" or "MICROPHONE"
)
