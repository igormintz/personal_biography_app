package com.personalbiography.data.repo

import com.personalbiography.data.db.UsageDao
import com.personalbiography.data.db.UsageEventEntity
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/**
 * Records usage events (LLM tokens + transcription seconds) and aggregates
 * them for the `/usage` command.
 */
class UsageRepository(
    private val dao: UsageDao,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend fun record(
        kind: String,
        costUsd: BigDecimal,
        seconds: Double? = null,
        tokensIn: Int? = null,
        tokensOut: Int? = null,
        entryId: String? = null,
    ) {
        dao.insert(
            UsageEventEntity(
                createdAtEpochMs = clock.millis(),
                kind = kind,
                seconds = seconds,
                tokensIn = tokensIn,
                tokensOut = tokensOut,
                costUsdMicros = costUsd.movePointRight(6).setScale(0, RoundingMode.HALF_UP).toLong(),
                entryId = entryId,
            ),
        )
    }

    suspend fun totalsSince(sinceEpochMs: Long): UsageTotals {
        val events = dao.since(sinceEpochMs)
        val totalCostMicros = events.sumOf { it.costUsdMicros }
        val totalSeconds =
            events
                .filter { it.kind == UsageEventEntity.KIND_TRANSCRIBE }
                .sumOf { it.seconds ?: 0.0 }
        val tokensIn =
            events
                .filter { it.kind == UsageEventEntity.KIND_STRUCTURE }
                .sumOf { it.tokensIn ?: 0 }
        val tokensOut =
            events
                .filter { it.kind == UsageEventEntity.KIND_STRUCTURE }
                .sumOf { it.tokensOut ?: 0 }
        return UsageTotals(
            events = events.size,
            costUsd = BigDecimal(totalCostMicros).movePointLeft(6),
            transcribeSeconds = totalSeconds,
            tokensIn = tokensIn,
            tokensOut = tokensOut,
        )
    }

    suspend fun totalsToday(zone: ZoneId = ZoneId.of("UTC")): UsageTotals {
        val startOfDay = LocalDate.now(clock.withZone(zone)).atStartOfDay(zone).toInstant().toEpochMilli()
        return totalsSince(startOfDay)
    }
}

data class UsageTotals(
    val events: Int,
    val costUsd: BigDecimal,
    val transcribeSeconds: Double,
    val tokensIn: Int,
    val tokensOut: Int,
)
