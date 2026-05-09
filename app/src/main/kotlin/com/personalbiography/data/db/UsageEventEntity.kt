package com.personalbiography.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Per-call cost tracking. Mirrors `usage_events` from `app/store/models.py`.
 *
 * `costUsdMicros` stores cost as integer micros (USD * 1_000_000) so we can
 * sum exactly without floating-point drift. The Python side uses
 * `Decimal(10, 6)`; this gives us the same precision in Room without a
 * BigDecimal converter.
 */
@Entity(
    tableName = "usage_events",
    indices = [
        Index("created_at_epoch_ms"),
        Index("entry_id"),
    ],
)
data class UsageEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
    val kind: String,
    /** Audio length transcribed, in seconds. Whole seconds. Null for non-transcribe events. */
    val seconds: Double? = null,
    @ColumnInfo(name = "tokens_in")
    val tokensIn: Int? = null,
    @ColumnInfo(name = "tokens_out")
    val tokensOut: Int? = null,
    @ColumnInfo(name = "cost_usd_micros")
    val costUsdMicros: Long = 0,
    @ColumnInfo(name = "entry_id")
    val entryId: String? = null,
) {
    companion object {
        const val KIND_TRANSCRIBE = "transcribe"
        const val KIND_STRUCTURE = "structure"
    }
}
