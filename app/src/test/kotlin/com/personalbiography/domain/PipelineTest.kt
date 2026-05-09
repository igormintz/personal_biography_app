package com.personalbiography.domain

import com.personalbiography.data.db.EntryEntity
import com.personalbiography.data.remote.OpenAiTranscriber
import com.personalbiography.data.repo.EntryRepository
import com.personalbiography.data.repo.FakeEntryDao
import com.personalbiography.data.repo.FakeUsageDao
import com.personalbiography.data.repo.UsageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class PipelineTest {
    private val fixedClock: Clock =
        Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneId.of("UTC"))

    private val sample =
        Structured(
            summary = "סיכום",
            tags = listOf(Tag.CHILDHOOD, Tag.FAMILY),
            entities = listOf("חיפה"),
            timeline = Timeline(approxAge = 7, year = 1990),
            followUpQuestions = listOf("a?", "b?", "c?"),
        )

    private fun newPipeline(
        structurer: Structurer,
        ids: ArrayDeque<String> = ArrayDeque(listOf("ABC123", "DEF456")),
        transcriberMock: OpenAiTranscriber = mockk(relaxed = true),
    ): Pair<Pipeline, EntryRepository> {
        val entryDao = FakeEntryDao()
        val usageDao = FakeUsageDao()
        val entryRepo = EntryRepository(entryDao, fixedClock) { ids.removeFirst() }
        val usageRepo = UsageRepository(usageDao, fixedClock)
        val pipeline = Pipeline(transcriberMock, structurer, entryRepo, usageRepo)
        return pipeline to entryRepo
    }

    @Test
    fun `text flow with structuring success persists ok status`() =
        runTest {
            val structurer =
                object : Structurer {
                    override suspend fun structure(transcript: String): StructureResult =
                        StructureResult(sample, tokensIn = 100, tokensOut = 50)
                }
            val (pipeline, repo) = newPipeline(structurer)

            val result = pipeline.processText("שלום, זה זיכרון מילדות")

            assertEquals("ABC123", result.entry.shortId)
            assertEquals(EntryEntity.STATUS_OK, result.entry.status)
            assertEquals("סיכום", result.entry.summary)
            assertEquals(listOf("childhood", "family"), result.entry.tags)
            assertEquals(7, result.entry.approxAge)
            // The repo state matches the result.
            assertEquals(EntryEntity.STATUS_OK, repo.getByShortId("ABC123")!!.status)
        }

    @Test
    fun `text flow with structuring failure stays needs_structuring`() =
        runTest {
            val structurer =
                object : Structurer {
                    override suspend fun structure(transcript: String): StructureResult = throw java.io.IOException("boom")
                }
            val (pipeline, _) = newPipeline(structurer)

            val result = pipeline.processText("שלום")

            assertEquals(EntryEntity.STATUS_NEEDS_STRUCTURING, result.entry.status)
            assertNull(result.structured)
            assertTrue(result.needsStructuring)
        }

    @Test
    fun `voice flow records audio seconds + structure usage`() =
        runTest {
            val transcriber =
                mockk<OpenAiTranscriber> {
                    coEvery { transcribe(any()) } returns
                        OpenAiTranscriber.Result(
                            text = "שלום זה תמליל",
                            audioSeconds = 12.5,
                            computeSeconds = 1.1,
                        )
                }
            val structurer =
                object : Structurer {
                    override suspend fun structure(transcript: String): StructureResult =
                        StructureResult(sample, tokensIn = 30, tokensOut = 20)
                }
            val (pipeline, _) = newPipeline(structurer, transcriberMock = transcriber)

            val statuses = mutableListOf<Pipeline.Status>()
            val tempFile =
                File.createTempFile("audio", ".m4a").apply {
                    writeBytes(byteArrayOf(0x00))
                    deleteOnExit()
                }

            val result =
                pipeline.processVoice(tempFile) { statuses += it }

            assertNotNull(result.structured)
            assertTrue(statuses.first() is Pipeline.Status.Received)
            assertTrue(statuses.any { it is Pipeline.Status.Transcribing })
            assertTrue(statuses.any { it is Pipeline.Status.Transcribed })
            assertTrue(statuses.any { it is Pipeline.Status.Structuring })
        }

    @Test
    fun `restructure replaces structured fields without creating a new entry`() =
        runTest {
            val structurer =
                object : Structurer {
                    var calls = 0

                    override suspend fun structure(transcript: String): StructureResult {
                        calls += 1
                        return StructureResult(
                            sample.copy(summary = "v$calls"),
                            tokensIn = 1,
                            tokensOut = 1,
                        )
                    }
                }
            val (pipeline, repo) = newPipeline(structurer)

            val first = pipeline.processText("טקסט").entry
            val again = pipeline.restructure(first.shortId)

            assertNotNull(again)
            assertEquals("v2", again!!.entry.summary)
            // No new entry should exist.
            assertEquals(first.id, repo.getByShortId(first.shortId)!!.id)
        }

    @Test
    fun `reEdit replaces transcript and re-structures`() =
        runTest {
            val structurer =
                object : Structurer {
                    override suspend fun structure(transcript: String): StructureResult =
                        StructureResult(sample.copy(summary = "from $transcript"), tokensIn = 1, tokensOut = 1)
                }
            val (pipeline, _) = newPipeline(structurer)
            val first = pipeline.processText("v1").entry

            val updated = pipeline.reEditTranscript(first.shortId, "v2")

            assertNotNull(updated)
            assertEquals("v2", updated!!.entry.transcript)
            assertEquals("from v2", updated.entry.summary)
        }
}
