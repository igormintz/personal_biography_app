package com.personalbiography.data.repo

import com.personalbiography.data.db.EntryDao
import com.personalbiography.data.db.EntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Repository unit tests against an in-memory fake [EntryDao]. Real Room
 * coverage lives in `androidTest/`; this layer is fast feedback for
 * business logic (short-id retry, transcript-only updates, etc.).
 */
class EntryRepositoryTest {
    private val fixedClock: Clock =
        Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneId.of("UTC"))

    @Test
    fun `create generates short id and persists with current time`() =
        runTest {
            val dao = FakeEntryDao()
            val ids = ArrayDeque(listOf("AAA111", "BBB222"))
            val repo = EntryRepository(dao, fixedClock) { ids.removeFirst() }

            val entry = repo.create(transcript = "hello")

            assertEquals("AAA111", entry.shortId)
            assertEquals(1_700_000_000_000L, entry.createdAtEpochMs)
            assertEquals("hello", entry.transcript)
            assertEquals(EntryEntity.STATUS_OK, entry.status)
            assertEquals(1, dao.byId.size)
        }

    @Test
    fun `create retries on short id collision`() =
        runTest {
            val dao = FakeEntryDao()
            dao.insert(
                EntryEntity(
                    id = "existing",
                    shortId = "AAA111",
                    createdAtEpochMs = 1L,
                    transcript = "x",
                ),
            )
            val ids = ArrayDeque(listOf("AAA111", "BBB222"))
            val repo = EntryRepository(dao, fixedClock) { ids.removeFirst() }

            val entry = repo.create(transcript = "hello")

            assertEquals("BBB222", entry.shortId)
        }

    @Test
    fun `getByShortId is case-insensitive`() =
        runTest {
            val dao = FakeEntryDao()
            val repo = EntryRepository(dao, fixedClock) { "ABC123" }
            repo.create(transcript = "x")

            assertNotNull(repo.getByShortId("abc123"))
            assertNotNull(repo.getByShortId("ABC123"))
            assertNull(repo.getByShortId("ZZZZZZ"))
        }

    @Test
    fun `applyStructured updates fields and clears status`() =
        runTest {
            val dao = FakeEntryDao()
            val repo = EntryRepository(dao, fixedClock) { "ABC123" }
            repo.create(
                transcript = "x",
                status = EntryEntity.STATUS_NEEDS_STRUCTURING,
            )

            val updated =
                repo.applyStructured(
                    shortId = "ABC123",
                    summary = "סיכום",
                    tags = listOf("family"),
                    entities = listOf("חיפה"),
                    followUpQuestions = listOf("a?", "b?", "c?"),
                    approxAge = 7,
                    year = 1990,
                )

            assertNotNull(updated)
            assertEquals("סיכום", updated!!.summary)
            assertEquals(listOf("family"), updated.tags)
            assertEquals(7, updated.approxAge)
            assertEquals(EntryEntity.STATUS_OK, updated.status)
        }

    @Test
    fun `setTranscript marks as editing by default`() =
        runTest {
            val dao = FakeEntryDao()
            val repo = EntryRepository(dao, fixedClock) { "ABC123" }
            repo.create(transcript = "old")

            val updated = repo.setTranscript("ABC123", "new")
            assertEquals("new", updated!!.transcript)
            assertEquals(EntryEntity.STATUS_EDITING, updated.status)
        }

    @Test
    fun `search empty term returns empty list`() =
        runTest {
            val dao = FakeEntryDao()
            val repo = EntryRepository(dao, fixedClock) { "ABC123" }
            repo.create(transcript = "hello world")

            assertTrue(repo.search("").isEmpty())
            assertTrue(repo.search("   ").isEmpty())
        }
}

/** Minimal in-memory [EntryDao] for repository tests. */
class FakeEntryDao : EntryDao {
    val byId: MutableMap<String, EntryEntity> = linkedMapOf()
    private val flow = MutableStateFlow<List<EntryEntity>>(emptyList())

    override suspend fun insert(entry: EntryEntity) {
        require(byId.values.none { it.shortId == entry.shortId }) {
            "duplicate short_id ${entry.shortId}"
        }
        byId[entry.id] = entry
        flow.value = byId.values.sortedByDescending { it.createdAtEpochMs }
    }

    override suspend fun update(entry: EntryEntity) {
        byId[entry.id] = entry
        flow.value = byId.values.sortedByDescending { it.createdAtEpochMs }
    }

    override suspend fun getById(id: String): EntryEntity? = byId[id]

    override suspend fun getByShortId(shortId: String): EntryEntity? = byId.values.firstOrNull { it.shortId == shortId }

    override suspend fun getLast(): EntryEntity? = byId.values.maxByOrNull { it.createdAtEpochMs }

    override suspend fun searchTranscript(
        term: String,
        limit: Int,
    ): List<EntryEntity> =
        byId.values
            .filter { it.transcript.contains(term, ignoreCase = true) }
            .sortedByDescending { it.createdAtEpochMs }
            .take(limit)

    override suspend fun pendingStructuring(limit: Int): List<EntryEntity> =
        byId.values
            .filter { it.status == EntryEntity.STATUS_NEEDS_STRUCTURING }
            .sortedBy { it.createdAtEpochMs }
            .take(limit)

    override fun observeRecent(limit: Int): Flow<List<EntryEntity>> = flow

    override suspend fun dumpAll(): List<EntryEntity> = byId.values.sortedBy { it.createdAtEpochMs }

    override suspend fun deleteById(id: String) {
        byId.remove(id)
        flow.value = byId.values.sortedByDescending { it.createdAtEpochMs }
    }
}
