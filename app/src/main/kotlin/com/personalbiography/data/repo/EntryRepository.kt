package com.personalbiography.data.repo

import com.personalbiography.data.db.EntryDao
import com.personalbiography.data.db.EntryEntity
import com.personalbiography.domain.makeShortId
import kotlinx.coroutines.flow.Flow
import java.time.Clock

/**
 * Repository over [EntryDao] — adds short-id generation with collision retry,
 * partial-update helpers, and field-level convenience methods that the
 * pipeline calls.
 */
class EntryRepository(
    private val dao: EntryDao,
    private val clock: Clock = Clock.systemUTC(),
    private val shortIdProvider: () -> String = ::makeShortId,
) {
    suspend fun create(
        transcript: String,
        source: String = EntryEntity.SOURCE_VOICE,
        status: String = EntryEntity.STATUS_OK,
        parentId: String? = null,
        audioPath: String? = null,
    ): EntryEntity {
        repeat(MAX_SHORT_ID_ATTEMPTS) {
            val shortId = shortIdProvider()
            if (dao.getByShortId(shortId) == null) {
                val entity =
                    EntryEntity(
                        shortId = shortId,
                        createdAtEpochMs = clock.millis(),
                        source = source,
                        parentId = parentId,
                        transcript = transcript,
                        status = status,
                        audioPath = audioPath,
                    )
                dao.insert(entity)
                return entity
            }
        }
        error("Could not generate a unique short_id after $MAX_SHORT_ID_ATTEMPTS attempts")
    }

    suspend fun getById(id: String): EntryEntity? = dao.getById(id)

    suspend fun getByShortId(shortId: String): EntryEntity? = dao.getByShortId(shortId.uppercase())

    suspend fun getLast(): EntryEntity? = dao.getLast()

    suspend fun search(
        term: String,
        limit: Int = 5,
    ): List<EntryEntity> = if (term.isBlank()) emptyList() else dao.searchTranscript(term.trim(), limit)

    suspend fun pendingStructuring(limit: Int = 20): List<EntryEntity> = dao.pendingStructuring(limit)

    suspend fun update(entry: EntryEntity) = dao.update(entry)

    suspend fun applyStructured(
        shortId: String,
        summary: String?,
        tags: List<String>,
        entities: List<String>,
        followUpQuestions: List<String>,
        approxAge: Int?,
        year: Int?,
        status: String = EntryEntity.STATUS_OK,
    ): EntryEntity? {
        val current = dao.getByShortId(shortId.uppercase()) ?: return null
        val next =
            current.copy(
                summary = summary,
                tags = tags,
                entities = entities,
                followUpQuestions = followUpQuestions,
                approxAge = approxAge,
                year = year,
                status = status,
            )
        dao.update(next)
        return next
    }

    suspend fun setTranscript(
        shortId: String,
        transcript: String,
        status: String = EntryEntity.STATUS_EDITING,
    ): EntryEntity? {
        val current = dao.getByShortId(shortId.uppercase()) ?: return null
        val next = current.copy(transcript = transcript, status = status)
        dao.update(next)
        return next
    }

    suspend fun setStatus(
        shortId: String,
        status: String,
    ): EntryEntity? {
        val current = dao.getByShortId(shortId.uppercase()) ?: return null
        val next = current.copy(status = status)
        dao.update(next)
        return next
    }

    suspend fun overrideTags(
        shortId: String,
        tags: List<String>,
    ): EntryEntity? {
        val current = dao.getByShortId(shortId.uppercase()) ?: return null
        val next = current.copy(tags = tags)
        dao.update(next)
        return next
    }

    suspend fun delete(id: String) = dao.deleteById(id)

    fun observeRecent(limit: Int = 100): Flow<List<EntryEntity>> = dao.observeRecent(limit)

    suspend fun dumpAll(): List<EntryEntity> = dao.dumpAll()

    companion object {
        private const val MAX_SHORT_ID_ATTEMPTS = 5
    }
}
