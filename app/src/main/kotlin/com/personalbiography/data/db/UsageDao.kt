package com.personalbiography.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsageDao {
    @Insert
    suspend fun insert(event: UsageEventEntity): Long

    @Query("SELECT * FROM usage_events WHERE created_at_epoch_ms >= :sinceEpochMs")
    suspend fun since(sinceEpochMs: Long): List<UsageEventEntity>
}
