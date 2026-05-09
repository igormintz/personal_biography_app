package com.personalbiography.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepliesTest {
    private fun entry(
        shortId: String = "ABC123",
        transcript: String = "שלום זה תמליל לדוגמה",
        summary: String? = "זה תקציר.",
        tags: List<String> = listOf("family", "childhood"),
        followUps: List<String> = listOf("שאלה 1?", "שאלה 2?", "שאלה 3?"),
    ): EntryView =
        EntryView(
            shortId = shortId,
            transcript = transcript,
            summary = summary,
            tags = tags,
            followUpQuestions = followUps,
        )

    @Test
    fun `format full bundle contains all sections`() {
        val msg = Replies.formatFullBundle(entry())
        assertTrue(msg.contains("📝 התמליל"))
        assertTrue(msg.contains("📌 תקציר"))
        assertTrue(msg.contains("🏷 תגיות"))
        assertTrue(msg.contains("❓ שאלות המשך"))
        assertTrue(msg.contains("🆔 ABC123"))
    }

    @Test
    fun `format compact is short`() {
        val msg = Replies.formatCompact(entry())
        assertTrue(msg.contains("ABC123"))
        assertTrue(msg.contains("📌"))
    }

    @Test
    fun `format questions lists each question`() {
        val msg = Replies.formatQuestions(entry())
        assertTrue(msg.contains("שאלה 1?"))
        assertTrue(msg.contains("ABC123"))
    }

    @Test
    fun `format search empty`() {
        assertEquals("לא נמצאו תוצאות.", Replies.formatSearchResults(emptyList()))
    }

    @Test
    fun `format search truncates long transcript`() {
        val longEntry = entry(transcript = "א".repeat(500))
        val msg = Replies.formatSearchResults(listOf(longEntry))
        assertTrue(msg.contains("ABC123"))
        assertTrue(msg.contains("…"))
    }

    @Test
    fun `status received with size only is step one`() {
        val msg = Replies.statusReceived(fileSizeBytes = 44_225)
        assertTrue(msg.startsWith("1/5"))
        assertTrue(msg.contains("KB"))
    }

    @Test
    fun `status received prefers duration over size`() {
        val msg = Replies.statusReceived(audioSeconds = 4.2, fileSizeBytes = 44_225)
        assertTrue(msg.startsWith("1/5"))
        assertTrue(msg.contains("4.2"))
        assertFalse(msg.contains("KB"))
    }

    @Test
    fun `status transcribing is step two`() {
        val msg = Replies.statusTranscribing()
        assertTrue(msg.startsWith("2/5"))
        assertTrue(msg.contains("מתמלל"))
    }

    @Test
    fun `status transcribed includes timing and chars`() {
        val msg = Replies.statusTranscribed(elapsedSeconds = 9.92, chars = 41)
        assertTrue(msg.startsWith("3/5"))
        assertTrue(msg.contains("9.9"))
        assertTrue(msg.contains("41"))
        assertTrue(msg.contains("תווים"))
    }

    @Test
    fun `status saving is step four`() {
        assertTrue(Replies.statusSaving().startsWith("4/5"))
    }

    @Test
    fun `status structuring is step four`() {
        val msg = Replies.statusStructuring()
        assertTrue(msg.startsWith("4/5"))
        assertTrue(msg.contains("מנתח"))
    }

    @Test
    fun `error message is short hebrew`() {
        assertTrue(Replies.errorMessage().contains("שגיאה"))
    }

    @Test
    fun `error message includes class and text`() {
        val msg = Replies.errorMessage(error = IllegalArgumentException("audio download failed"))
        assertTrue(msg.contains("שגיאה"))
        assertTrue(msg.contains("IllegalArgumentException"))
        assertTrue(msg.contains("audio download failed"))
    }

    @Test
    fun `error message truncates long text`() {
        val long = "x".repeat(500)
        val msg = Replies.errorMessage(error = RuntimeException(long))
        assertTrue(msg.length < 500)
        assertTrue(msg.contains("…"))
    }

    @Test
    fun `error message no args returns generic`() {
        assertEquals("שגיאה זמנית. נסה שוב.", Replies.errorMessage())
    }

    @Test
    fun `error message includes update id`() {
        val msg = Replies.errorMessage(error = RuntimeException("boom"), correlationId = "128258000")
        assertTrue(msg.contains("128258000"))
    }
}
