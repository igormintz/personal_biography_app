package com.personalbiography.ui.chat

import com.personalbiography.data.db.EntryEntity

/**
 * UI model for chat bubbles. Persistent bubbles ([UserText] / [UserVoice]
 * paired with [BotResult]) are derived from `entries` rows on the DB-driven
 * Flow; transient ones ([BotStatus] / [BotInfo] / [BotError]) live only in
 * the ViewModel and disappear on process death.
 */
sealed interface ChatMessage {
    val id: String
    val timestampMs: Long

    data class UserText(
        override val id: String,
        override val timestampMs: Long,
        val body: String,
    ) : ChatMessage

    data class UserVoice(
        override val id: String,
        override val timestampMs: Long,
        val durationSec: Double,
    ) : ChatMessage

    /**
     * In-place mutable status that walks through the 5 Hebrew stages.
     * Replaced by a [BotResult] (or [BotError]) when the pipeline finishes.
     */
    data class BotStatus(
        override val id: String,
        override val timestampMs: Long,
        val text: String,
        val stage: Int,
    ) : ChatMessage

    /** A persistent entry rendered as the final bot reply. */
    data class BotResult(
        override val id: String,
        override val timestampMs: Long,
        val entry: EntryEntity,
    ) : ChatMessage

    /** Transient info bubble — `/help`, `/usage`, search results. */
    data class BotInfo(
        override val id: String,
        override val timestampMs: Long,
        val text: String,
    ) : ChatMessage

    data class BotError(
        override val id: String,
        override val timestampMs: Long,
        val text: String,
    ) : ChatMessage
}
