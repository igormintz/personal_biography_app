package com.personalbiography.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandsTest {
    @Test
    fun `non-slash text is FreeText`() {
        val cmd = parseCommand("שלום, זה זיכרון מילדות.")
        assertTrue(cmd is Command.FreeText)
        assertEquals("שלום, זה זיכרון מילדות.", (cmd as Command.FreeText).body)
    }

    @Test
    fun `last with no arg`() {
        assertTrue(parseCommand("/last") is Command.Last)
        assertTrue(parseCommand("/LAST  ") is Command.Last)
    }

    @Test
    fun `show requires id`() {
        val ok = parseCommand("/show abc123")
        assertTrue(ok is Command.Show)
        assertEquals("ABC123", (ok as Command.Show).shortId)
        // missing id => Invalid
        assertTrue(parseCommand("/show") is Command.Invalid)
        assertTrue(parseCommand("/show   ") is Command.Invalid)
    }

    @Test
    fun `questions edit tags restructure all take id`() {
        assertEquals("ABC123", (parseCommand("/questions abc123") as Command.Questions).shortId)
        assertEquals("ABC123", (parseCommand("/edit abc123") as Command.Edit).shortId)
        assertEquals("ABC123", (parseCommand("/tags abc123") as Command.Tags).shortId)
        assertEquals("ABC123", (parseCommand("/restructure abc123") as Command.Restructure).shortId)
        assertTrue(parseCommand("/edit") is Command.Invalid)
    }

    @Test
    fun `search requires text`() {
        val s = parseCommand("/search childhood בחיפה") as Command.Search
        assertEquals("childhood בחיפה", s.text)
        assertTrue(parseCommand("/search") is Command.Invalid)
        assertTrue(parseCommand("/search   ") is Command.Invalid)
    }

    @Test
    fun `usage and help`() {
        assertTrue(parseCommand("/usage") is Command.Usage)
        assertTrue(parseCommand("/help") is Command.Help)
        assertTrue(parseCommand("/start") is Command.Start)
    }

    @Test
    fun `unknown slash is Invalid`() {
        assertTrue(parseCommand("/nope") is Command.Invalid)
    }

    @Test
    fun `whitespace and case are normalized`() {
        val cmd = parseCommand("   /Show   ABc123  ") as Command.Show
        assertEquals("ABC123", cmd.shortId)
    }
}
