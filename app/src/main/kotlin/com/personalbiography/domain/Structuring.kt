package com.personalbiography.domain

import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Schema returned by the structuring LLM. Direct port of the `Structured`
 * pydantic model in `app/pipeline/structure.py`.
 *
 * `tags` uses the [Tag] enum so unknown values from the model are rejected
 * during deserialization. The accompanying [filterKnownTags] helper handles
 * the (rare) case where we get raw strings out-of-band.
 */
@Serializable
data class Structured(
    val summary: String,
    val tags: List<Tag> = emptyList(),
    val entities: List<String> = emptyList(),
    val timeline: Timeline = Timeline(),
    @kotlinx.serialization.SerialName("follow_up_questions")
    val followUpQuestions: List<String>,
)

@Serializable
data class Timeline(
    @kotlinx.serialization.SerialName("approx_age")
    val approxAge: Int? = null,
    val year: Int? = null,
)

data class StructureResult(
    val data: Structured,
    val tokensIn: Int,
    val tokensOut: Int,
    val costUsd: BigDecimal = StructureCost.estimate(tokensIn, tokensOut),
)

/**
 * Pluggable LLM call. Real implementation lives in
 * `data/remote/OpenAiStructurer.kt`; tests use an in-memory fake.
 */
interface Structurer {
    suspend fun structure(transcript: String): StructureResult
}

/** gpt-4o-mini pricing (USD per 1M tokens), accurate as of plan date. */
object StructureCost {
    private val PRICE_IN_PER_M = BigDecimal("0.15")
    private val PRICE_OUT_PER_M = BigDecimal("0.60")
    private val MILLION = BigDecimal(1_000_000)

    fun estimate(
        tokensIn: Int,
        tokensOut: Int,
    ): BigDecimal {
        val inCost = BigDecimal(tokensIn).multiply(PRICE_IN_PER_M).divide(MILLION, 12, RoundingMode.HALF_UP)
        val outCost = BigDecimal(tokensOut).multiply(PRICE_OUT_PER_M).divide(MILLION, 12, RoundingMode.HALF_UP)
        return inCost.add(outCost).setScale(6, RoundingMode.HALF_UP)
    }
}

/**
 * Drop unknown tag strings. Matches the defensive `_drop_unknown_tags`
 * validator on the Python `Structured` class. Used when we get a raw list
 * from a non-strict source (e.g. a manual `/tags` override).
 */
fun filterKnownTags(raw: List<String>): List<Tag> = raw.mapNotNull { Tag.fromWireOrNull(it) }

/**
 * Retry on transient errors with exponential backoff. Returns `null` when we
 * give up — callers persist the entry with `status = needs_structuring` and
 * the polish task wires a WorkManager retry.
 */
suspend fun structureWithRetry(
    transcript: String,
    structurer: Structurer,
    maxAttempts: Int = 3,
    backoffMs: (attempt: Int) -> Long = { attempt -> 1000L * (1L shl attempt) },
): StructureResult? {
    var lastError: Throwable? = null
    for (attempt in 0 until maxAttempts) {
        try {
            return structurer.structure(transcript)
        } catch (e: IOException) {
            lastError = e
        } catch (e: IllegalArgumentException) {
            lastError = e
        } catch (e: kotlinx.serialization.SerializationException) {
            lastError = e
        }
        if (attempt + 1 < maxAttempts) {
            delay(backoffMs(attempt))
        }
    }
    if (lastError != null) {
        // Surface the cause via println for now; the real impl will use Logcat
        // through a thin Logger interface. Tests rely on the return being null.
        System.err.println("structuring_giving_up: ${lastError.message}")
    }
    return null
}
