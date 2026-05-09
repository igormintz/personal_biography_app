package com.personalbiography.domain

import com.personalbiography.data.db.EntryEntity
import com.personalbiography.data.db.UsageEventEntity
import com.personalbiography.data.remote.OpenAiTranscriber
import com.personalbiography.data.repo.EntryRepository
import com.personalbiography.data.repo.UsageRepository
import java.io.File
import java.math.BigDecimal

/**
 * End-to-end flow: transcribe → structure → persist. Direct port of
 * `process_voice` / `process_text` / `_persist_and_structure` in
 * `app/pipeline/orchestrator.py`.
 *
 * Status updates flow through [emit] so the chat ViewModel can mutate the
 * status bubble in place; callers that don't care can pass `{}`.
 */
class Pipeline(
    private val transcriber: OpenAiTranscriber,
    private val structurer: Structurer,
    private val entryRepo: EntryRepository,
    private val usageRepo: UsageRepository,
) {
    sealed interface Status {
        data class Received(val audioSeconds: Double? = null, val fileSizeBytes: Long? = null) : Status

        data object Transcribing : Status

        data class Transcribed(val computeSeconds: Double, val chars: Int) : Status

        data object Structuring : Status

        data object Saving : Status
    }

    data class Result(
        val entry: EntryEntity,
        val structured: Structured?,
        val needsStructuring: Boolean,
    )

    suspend fun processVoice(
        audioFile: File,
        emit: suspend (Status) -> Unit = {},
    ): Result {
        emit(Status.Received(fileSizeBytes = audioFile.length()))
        emit(Status.Transcribing)
        val tx = transcriber.transcribe(audioFile)
        emit(Status.Transcribed(tx.computeSeconds, tx.text.length))
        emit(if (tx.text.isNotBlank()) Status.Structuring else Status.Saving)
        return persistAndStructure(
            transcript = tx.text,
            source = EntryEntity.SOURCE_VOICE,
            audioSeconds = tx.audioSeconds,
            audioPath = audioFile.absolutePath,
        )
    }

    suspend fun processText(
        text: String,
        emit: suspend (Status) -> Unit = {},
    ): Result {
        val cleaned = text.trim()
        emit(if (cleaned.isNotBlank()) Status.Structuring else Status.Saving)
        return persistAndStructure(
            transcript = cleaned,
            source = EntryEntity.SOURCE_TEXT,
            audioSeconds = null,
            audioPath = null,
        )
    }

    /**
     * Re-run structuring on an existing entry, replacing its summary / tags
     * / entities / timeline / follow-up questions in place. Mirrors
     * `cmd_restructure` in `app/bot/handlers.py`.
     */
    suspend fun restructure(shortId: String): Result? {
        val current = entryRepo.getByShortId(shortId) ?: return null
        val structured = structureWithRetry(current.transcript, structurer)
        return if (structured != null) {
            usageRepo.record(
                kind = UsageEventEntity.KIND_STRUCTURE,
                costUsd = structured.costUsd,
                tokensIn = structured.tokensIn,
                tokensOut = structured.tokensOut,
                entryId = current.id,
            )
            val applied =
                entryRepo.applyStructured(
                    shortId = shortId,
                    summary = structured.data.summary,
                    tags = structured.data.tags.map { it.wire },
                    entities = structured.data.entities,
                    followUpQuestions = structured.data.followUpQuestions,
                    approxAge = structured.data.timeline.approxAge,
                    year = structured.data.timeline.year,
                ) ?: return null
            Result(applied, structured.data, needsStructuring = false)
        } else {
            val updated =
                entryRepo.setStatus(shortId, EntryEntity.STATUS_NEEDS_STRUCTURING) ?: return null
            Result(updated, structured = null, needsStructuring = true)
        }
    }

    /**
     * Replace transcript and re-structure. Mirrors the `_handle_pending` /
     * `kind=edit` path in `app/bot/handlers.py`.
     */
    suspend fun reEditTranscript(
        shortId: String,
        newTranscript: String,
    ): Result? {
        entryRepo.setTranscript(shortId, newTranscript) ?: return null
        return restructure(shortId)
    }

    private suspend fun persistAndStructure(
        transcript: String,
        source: String,
        audioSeconds: Double?,
        audioPath: String?,
    ): Result {
        val structured =
            if (transcript.isNotBlank()) structureWithRetry(transcript, structurer) else null
        val initialStatus =
            if (structured != null) EntryEntity.STATUS_OK else EntryEntity.STATUS_NEEDS_STRUCTURING

        val entry =
            entryRepo.create(
                transcript = transcript,
                source = source,
                status = initialStatus,
                audioPath = audioPath,
            )

        var current = entry
        if (structured != null) {
            current =
                entryRepo.applyStructured(
                    shortId = entry.shortId,
                    summary = structured.data.summary,
                    tags = structured.data.tags.map { it.wire },
                    entities = structured.data.entities,
                    followUpQuestions = structured.data.followUpQuestions,
                    approxAge = structured.data.timeline.approxAge,
                    year = structured.data.timeline.year,
                ) ?: entry
            usageRepo.record(
                kind = UsageEventEntity.KIND_STRUCTURE,
                costUsd = structured.costUsd,
                tokensIn = structured.tokensIn,
                tokensOut = structured.tokensOut,
                entryId = current.id,
            )
        }

        if (audioSeconds != null && audioSeconds > 0) {
            usageRepo.record(
                kind = UsageEventEntity.KIND_TRANSCRIBE,
                costUsd = BigDecimal.ZERO,
                seconds = audioSeconds,
                entryId = current.id,
            )
        }

        return Result(
            entry = current,
            structured = structured?.data,
            needsStructuring = structured == null,
        )
    }
}
