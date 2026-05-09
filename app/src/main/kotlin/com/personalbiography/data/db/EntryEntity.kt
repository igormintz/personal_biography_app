package com.personalbiography.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.personalbiography.domain.EntryView
import java.util.UUID

/**
 * Single biography memory. Mirrors the `entries` table from
 * `app/store/models.py` minus the Telegram-specific columns
 * (`tg_chat_id` / `tg_message_id`), plus a local `audio_path`.
 */
@Entity(
    tableName = "entries",
    indices = [
        Index("short_id", unique = true),
        Index("created_at_epoch_ms"),
        Index("status"),
        Index("parent_id"),
    ],
)
data class EntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "short_id")
    val shortId: String,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
    val source: String = "voice",
    @ColumnInfo(name = "parent_id")
    val parentId: String? = null,
    val transcript: String = "",
    val summary: String? = null,
    val tags: List<String> = emptyList(),
    val entities: List<String> = emptyList(),
    @ColumnInfo(name = "follow_up_questions")
    val followUpQuestions: List<String> = emptyList(),
    @ColumnInfo(name = "approx_age")
    val approxAge: Int? = null,
    val year: Int? = null,
    val status: String = STATUS_OK,
    @ColumnInfo(name = "audio_path")
    val audioPath: String? = null,
) {
    fun toView(): EntryView =
        EntryView(
            shortId = shortId,
            transcript = transcript,
            summary = summary,
            tags = tags,
            followUpQuestions = followUpQuestions,
        )

    companion object {
        const val STATUS_OK = "ok"
        const val STATUS_NEEDS_STRUCTURING = "needs_structuring"
        const val STATUS_EDITING = "editing"

        const val SOURCE_VOICE = "voice"
        const val SOURCE_TEXT = "text"
    }
}
