package com.personalbiography.domain

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A minimal projection of an `Entry` that the formatters need. Decoupled from
 * the Room entity so domain code can be JVM-only and unit-tested without
 * Android.
 */
data class EntryView(
    val shortId: String,
    val transcript: String?,
    val summary: String?,
    val tags: List<String>,
    val followUpQuestions: List<String>,
)

/**
 * Hebrew copy + formatters. Direct port of `app/bot/replies.py`.
 *
 * Kept neutral / clear-tone — same as the Telegram bot — so chat bubbles and
 * transient command replies in the Android app read identically to the bot
 * messages users are already used to.
 */
object Replies {
    const val ACK_VOICE = "מקבל ומתמלל…"
    const val ACK_TEXT = "מעבד…"
    const val NOT_ALLOWED = "הבוט הזה פרטי."
    const val NOT_FOUND = "לא נמצא רישום עם המזהה הזה."
    const val NEEDS_STRUCTURING = "התמליל נשמר, אך עיבוד הניתוח נכשל וננסה שוב ברקע."
    const val TRANSCRIBE_FAILED = "שגיאה בתמלול. נסה שוב."
    const val EDIT_PROMPT = "שלח את התמליל המתוקן בהודעה הבאה."
    const val EDIT_DONE = "התמליל עודכן ומחדש את הניתוח."
    const val GENERIC_ERROR = "שגיאה זמנית. נסה שוב."

    val START =
        """שלום! זהו בוט הביוגרפיה האישי שלך.
שלח לי הודעה קולית בעברית — אתמלל, אסכם, אתייג ואשאל שאלות המשך.
פקודות: /last /show <id> /questions /edit <id> /tags <id> /restructure <id> /search <text> /usage /help"""

    val HELP =
        """פקודות:
• /last — הרישום האחרון
• /show <id> — מציג רישום
• /questions <id> — שאלות ההמשך
• /edit <id> — עריכת תמליל ידנית
• /tags <id> — דריסת תגיות
• /restructure <id> — הרצה חוזרת של ה-LLM
• /search <text> — חיפוש בתמלילים
• /usage — שימוש מצטבר היום"""

    fun statusReceived(
        audioSeconds: Double? = null,
        fileSizeBytes: Long? = null,
    ): String {
        if (audioSeconds != null && audioSeconds > 0) {
            return "1/5 קיבלתי (${"%.1f".format(audioSeconds)}s)"
        }
        if (fileSizeBytes != null) {
            val kb = max(1L, (fileSizeBytes / 1024.0).roundToInt().toLong())
            return "1/5 קיבלתי (${kb}KB)"
        }
        return "1/5 קיבלתי"
    }

    fun statusTranscribing(): String = "2/5 🎙️ מתמלל…"

    fun statusLoadingModel(): String = "2/5 ⏳ טוען מודל תמלול מהמטמון (כמה שניות)…"

    fun statusTranscribed(
        elapsedSeconds: Double,
        chars: Int,
    ): String = "3/5 ✅ תומלל (${"%.1f".format(elapsedSeconds)}s, $chars תווים)"

    fun statusSaving(): String = "4/5 💾 שומר…"

    fun statusStructuring(): String = "4/5 🧠 מנתח…"

    fun errorMessage(
        error: Throwable? = null,
        correlationId: String? = null,
    ): String {
        if (error == null && correlationId == null) return GENERIC_ERROR

        val parts = mutableListOf(GENERIC_ERROR)
        if (error != null) {
            val cls = error::class.simpleName ?: "Error"
            val raw = (error.message ?: "").trim().replace("\n", " ")
            val text = if (raw.length > 160) raw.substring(0, 160) + "…" else raw
            parts += if (text.isEmpty()) "[$cls]" else "[$cls] $text"
        }
        if (correlationId != null) {
            parts += "🔎 update_id=$correlationId"
        }
        return parts.joinToString("\n")
    }

    fun formatFullBundle(entry: EntryView): String {
        val tags = entry.tags.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "—"
        val qLines =
            entry.followUpQuestions
                .takeIf { it.isNotEmpty() }
                ?.mapIndexed { i, q -> "${i + 1}. $q" }
                ?.joinToString("\n") ?: "—"
        val summary = entry.summary?.takeIf { it.isNotBlank() } ?: "—"
        val transcript = entry.transcript?.takeIf { it.isNotBlank() } ?: "—"
        return buildString {
            appendLine("📝 התמליל")
            appendLine(transcript)
            appendLine()
            appendLine("📌 תקציר")
            appendLine(summary)
            appendLine()
            appendLine("🏷 תגיות: $tags")
            appendLine()
            appendLine("❓ שאלות המשך:")
            appendLine(qLines)
            appendLine()
            append("🆔 ${entry.shortId}")
        }
    }

    fun formatCompact(entry: EntryView): String {
        val tags = entry.tags.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "—"
        val summary = entry.summary?.takeIf { it.isNotBlank() } ?: "—"
        return "📌 $summary\n🏷 $tags\n🆔 ${entry.shortId}"
    }

    fun formatQuestions(entry: EntryView): String {
        if (entry.followUpQuestions.isEmpty()) return "אין שאלות המשך לרישום הזה."
        val body = entry.followUpQuestions.mapIndexed { i, q -> "${i + 1}. $q" }.joinToString("\n")
        return "❓ שאלות המשך לרישום ${entry.shortId}:\n$body"
    }

    fun formatSearchResults(entries: List<EntryView>): String {
        if (entries.isEmpty()) return "לא נמצאו תוצאות."
        return entries.joinToString("\n") { e ->
            val raw = (e.transcript ?: "").take(120).replace("\n", " ")
            "🆔 ${e.shortId} — $raw…"
        }
    }

    fun formatTags(entry: EntryView): String {
        val tags = entry.tags.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "—"
        return "🏷 תגיות לרישום ${entry.shortId}: $tags\nשלח רשימת תגיות מופרדות בפסיקים כדי לדרוס."
    }
}
