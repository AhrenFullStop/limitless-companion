package com.limitless.companion.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SessionEntity::class, TranscriptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TranscriptDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun transcriptDao(): TranscriptDao

    companion object {
        @Volatile private var INSTANCE: TranscriptDatabase? = null

        fun getInstance(context: Context): TranscriptDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TranscriptDatabase::class.java,
                    "limitless.db"
                ).build().also { INSTANCE = it }
            }
    }
}
