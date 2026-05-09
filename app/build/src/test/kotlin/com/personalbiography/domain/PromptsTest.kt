package com.personalbiography.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class PromptsTest {
    @Test
    fun `system prompt mentions all 10 tags`() {
        val sys = Prompts.STRUCTURING_SYSTEM
        Tag.ALL_WIRE.forEach { tag ->
            assertTrue("system prompt missing tag '$tag'", sys.contains(tag))
        }
    }

    @Test
    fun `system prompt asks for hebrew output`() {
        assertTrue(Prompts.STRUCTURING_SYSTEM.contains("Hebrew"))
    }

    @Test
    fun `system prompt requires 3 to 5 follow-up questions`() {
        val sys = Prompts.STRUCTURING_SYSTEM
        assertTrue(sys.contains("3") && sys.contains("5") && sys.contains("follow_up_questions"))
    }

    @Test
    fun `user prompt embeds the transcript`() {
        val transcript = "שלום, זה זיכרון מילדות בחיפה."
        val msg = Prompts.userPrompt(transcript)
        assertTrue(msg.startsWith("Transcript:"))
        assertTrue(msg.contains(transcript))
    }
}
