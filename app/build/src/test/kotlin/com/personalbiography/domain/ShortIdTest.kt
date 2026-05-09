package com.personalbiography.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortIdTest {
    @Test
    fun `length is 6 and uses crockford-ish alphabet`() {
        val sid = makeShortId()
        assertEquals(6, sid.length)
        val allowed = "ABCDEFGHJKMNPQRSTVWXYZ23456789".toSet()
        assertTrue("expected only allowed chars in $sid", sid.all { it in allowed })
    }

    @Test
    fun `ambiguous characters are excluded`() {
        repeat(200) {
            val sid = makeShortId()
            // Crockford-ish: skip 0, O, 1, I, L, U.
            assertTrue("ambiguous char in $sid", sid.none { it in "01ILOU" })
        }
    }

    @Test
    fun `random across many calls`() {
        val seen = (1..100).map { makeShortId() }.toSet()
        assertTrue("expected high uniqueness, got ${seen.size}", seen.size > 90)
    }
}
