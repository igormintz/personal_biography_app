package com.personalbiography.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): EntryEntity?

    @Query("SELECT * FROM entries WHERE short_id = :shortId LIMIT 1")
    suspend fun getByShortId(shortId: String): EntryEntity?

    @Query("SELECT * FROM entries ORDER BY created_at_epoch_ms DESC LIMIT 1")
    suspend fun getLast(): EntryEntity?

    @Query(
        "SELECT * FROM entries WHERE transcript LIKE '%' || :term || '%' " +
            "ORDER BY created_at_epoch_ms DESC LIMIT :limit",
    )
    suspend fun searchTranscript(
        term: String,
        limit: Int,
    ): List<EntryEntity>

    @Query(
        "SELECT * FROM entries WHERE status = '${EntryEntity.STATUS_NEEDS_STRUCTURING}' " +
            "ORDER BY created_at_epoch_ms ASC LIMIT :limit",
    )
    suspend fun pendingStructuring(limit: Int): List<EntryEntity>

    @Query("SELECT * FROM entries ORDER BY created_at_epoch_ms DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries ORDER BY created_at_epoch_ms ASC")
    suspend fun dumpAll(): List<EntryEntity>

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
