package com.limitless.companion.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transcript: TranscriptEntity)

    @Query("SELECT * FROM transcripts ORDER BY startTime DESC")
    fun getAllAsFlow(): Flow<List<TranscriptEntity>>

    @Query("SELECT * FROM transcripts ORDER BY startTime DESC")
    suspend fun getAll(): List<TranscriptEntity>

    @Query("SELECT * FROM transcripts WHERE text LIKE '%' || :query || '%' ORDER BY startTime DESC")
    suspend fun search(query: String): List<TranscriptEntity>

    @Query("SELECT COUNT(*) FROM transcripts")
    suspend fun count(): Int

    @Query("DELETE FROM transcripts WHERE startTime < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)
}
