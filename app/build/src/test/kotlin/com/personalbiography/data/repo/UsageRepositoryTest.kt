package com.personalbiography.data.repo

import com.personalbiography.data.db.UsageDao
import com.personalbiography.data.db.UsageEventEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class UsageRepositoryTest {
    private val fixedClock: Clock =
        Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneId.of("UTC"))

    @Test
    fun `record stores cost as integer micros`() =
        runTest {
            val dao = FakeUsageDao()
            val repo = UsageRepository(dao, fixedClock)

            repo.record(
                kind = UsageEventEntity.KIND_STRUCTURE,
                costUsd = BigDecimal("0.001234"),
                tokensIn = 100,
                tokensOut = 50,
            )

            val event = dao.events.single()
            assertEquals(1234L, event.costUsdMicros)
            assertEquals(UsageEventEntity.KIND_STRUCTURE, event.kind)
            assertEquals(100, event.tokensIn)
            assertEquals(50, event.tokensOut)
        }

    @Test
    fun `totalsSince aggregates structure tokens and transcribe seconds`() =
        runTest {
            val dao = FakeUsageDao()
            val repo = UsageRepository(dao, fixedClock)

            repo.record(
                kind = UsageEventEntity.KIND_STRUCTURE,
                costUsd = BigDecimal("0.002"),
                tokensIn = 100,
                tokensOut = 50,
            )
            repo.record(
                kind = UsageEventEntity.KIND_TRANSCRIBE,
                costUsd = BigDecimal("0"),
                seconds = 12.5,
            )
            repo.record(
                kind = UsageEventEntity.KIND_STRUCTURE,
                costUsd = BigDecimal("0.001"),
                tokensIn = 200,
                tokensOut = 75,
            )

            val totals = repo.totalsSince(0L)
            assertEquals(3, totals.events)
            assertEquals(BigDecimal("0.003000"), totals.costUsd)
            assertEquals(12.5, totals.transcribeSeconds, 0.0001)
            assertEquals(300, totals.tokensIn)
            assertEquals(125, totals.tokensOut)
        }
}

class FakeUsageDao : UsageDao {
    val events: MutableList<UsageEventEntity> = mutableListOf()

    override suspend fun insert(event: UsageEventEntity): Long {
        val withId = event.copy(id = (events.size + 1).toLong())
        events += withId
        return withId.id
    }

    override suspend fun since(sinceEpochMs: Long): List<UsageEventEntity> = events.filter { it.createdAtEpochMs >= sinceEpochMs }
}
