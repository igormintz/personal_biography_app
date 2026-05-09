package com.personalbiography.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal

class StructuringTest {
    private val sample =
        Structured(
            summary = "זיכרון מילדות בחיפה.",
            tags = listOf(Tag.CHILDHOOD, Tag.FAMILY),
            entities = listOf("חיפה", "סבתא"),
            timeline = Timeline(approxAge = 7, year = 1990),
            followUpQuestions =
            listOf(
                "מי עוד היה שם?",
                "באיזו שכונה?",
                "מה הרגשת?",
            ),
        )

    @Test
    fun `cost calc matches gpt-4o-mini price card`() {
        // 1M input + 0 output = $0.15
        val a = StructureCost.estimate(1_000_000, 0)
        assertEquals(BigDecimal("0.150000"), a)
        // 0 input + 1M output = $0.60
        val b = StructureCost.estimate(0, 1_000_000)
        assertEquals(BigDecimal("0.600000"), b)
    }

    @Test
    fun `single attempt success`() =
        runTest {
            val fake =
                FakeStructurer { _ ->
                    StructureResult(data = sample, tokensIn = 100, tokensOut = 50)
                }
            val result = structureWithRetry("טקסט", fake, maxAttempts = 3)
            assertNotNull(result)
            assertEquals(sample, result!!.data)
            assertEquals(1, fake.calls)
            assertEquals(BigDecimal("0.000045"), result.costUsd)
        }

    @Test
    fun `retries up to limit then gives up`() =
        runTest {
            val fake =
                FakeStructurer { _ ->
                    throw IOException("simulated bad json")
                }
            val result =
                structureWithRetry(
                    "טקסט",
                    fake,
                    maxAttempts = 3,
                    backoffMs = { 0L },
                )
            assertNull(result)
            assertEquals(3, fake.calls)
        }

    @Test
    fun `succeeds on second attempt after one failure`() =
        runTest {
            var n = 0
            val fake =
                FakeStructurer { _ ->
                    n += 1
                    if (n == 1) throw IOException("transient")
                    StructureResult(data = sample, tokensIn = 10, tokensOut = 5)
                }
            val result =
                structureWithRetry(
                    "טקסט",
                    fake,
                    maxAttempts = 3,
                    backoffMs = { 0L },
                )
            assertNotNull(result)
            assertEquals(2, fake.calls)
        }

    @Test
    fun `unknown tag strings are dropped from a list`() {
        val raw = listOf("family", "not_a_real_tag", "childhood")
        val parsed = filterKnownTags(raw)
        assertTrue("family" in parsed.map { it.wire })
        assertTrue("childhood" in parsed.map { it.wire })
        assertEquals(2, parsed.size)
    }

    private class FakeStructurer(
        private val handler: suspend (String) -> StructureResult,
    ) : Structurer {
        var calls: Int = 0
            private set

        override suspend fun structure(transcript: String): StructureResult {
            calls += 1
            return handler(transcript)
        }
    }
}
