package com.personalbiography.ui.chat

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.personalbiography.BiographyApp
import com.personalbiography.data.db.EntryEntity
import com.personalbiography.data.repo.EntryRepository
import com.personalbiography.data.repo.UsageRepository
import com.personalbiography.data.secure.SecureSettings
import com.personalbiography.domain.AudioRecorder
import com.personalbiography.domain.Command
import com.personalbiography.domain.Pipeline
import com.personalbiography.domain.Replies
import com.personalbiography.domain.filterKnownTags
import com.personalbiography.domain.parseCommand
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * Owns the chat conversation state. Messages are the union of:
 *
 * 1. **Persistent**: [Pipeline.Result] entries surfaced by
 *    [EntryRepository.observeRecent] — each entry becomes a user bubble + a
 *    [ChatMessage.BotResult] bubble.
 * 2. **Transient**: in-memory [ChatMessage.BotStatus] / [ChatMessage.BotInfo]
 *    / [ChatMessage.BotError] bubbles produced by the pipeline or by slash
 *    commands. Cleared on process death.
 *
 * Pending edit/tags follow-up state mirrors `_handle_pending` in
 * `app/bot/handlers.py`.
 */
class ChatViewModel(
    private val pipeline: Pipeline,
    private val entryRepo: EntryRepository,
    private val usageRepo: UsageRepository,
    private val secureSettings: SecureSettings,
    private val recorder: AudioRecorder,
) : ViewModel() {
    private val transientMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val pending = MutableStateFlow<Pending?>(null)
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState

    val hasApiKey: StateFlow<Boolean> =
        MutableStateFlow(secureSettings.hasApiKey())

    val pendingPrompt: StateFlow<Pending?> = pending

    /**
     * Combined message list to render. Sorted by timestamp ascending so
     * older bubbles are at the top, newest at the bottom. The [LazyColumn]
     * in [ChatScreen] auto-scrolls to the last item.
     */
    val messages: StateFlow<List<ChatMessage>> =
        combine(
            entryRepo.observeRecent(limit = 200),
            transientMessages,
        ) { entries, transient ->
            buildList {
                entries.sortedBy { it.createdAtEpochMs }.forEach { entry ->
                    add(entryToUserBubble(entry))
                    add(
                        ChatMessage.BotResult(
                            id = "result-${entry.id}",
                            timestampMs = entry.createdAtEpochMs + 1,
                            entry = entry,
                        ),
                    )
                }
                addAll(transient)
            }.sortedBy { it.timestampMs }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ----- text input -----

    fun sendText(raw: String) {
        if (raw.isBlank()) return
        val pendingNow = pending.value
        if (pendingNow != null) {
            handlePending(raw.trim(), pendingNow)
            return
        }
        when (val cmd = parseCommand(raw)) {
            is Command.FreeText -> runFreeText(cmd.body)
            Command.Last -> runLast()
            is Command.Show -> runShow(cmd.shortId)
            is Command.Questions -> runQuestions(cmd.shortId)
            is Command.Edit -> runEdit(cmd.shortId)
            is Command.Tags -> runTagsPrompt(cmd.shortId)
            is Command.Restructure -> runRestructure(cmd.shortId)
            is Command.Search -> runSearch(cmd.text)
            Command.Usage -> runUsage()
            Command.Help -> appendInfo(Replies.HELP)
            Command.Start -> appendInfo(Replies.START)
            is Command.Invalid -> appendInfo(Replies.HELP)
        }
    }

    private fun runFreeText(body: String) {
        if (!secureSettings.hasApiKey()) {
            appendError("חסר מפתח OpenAI. פתח/י הגדרות והוסף/י מפתח.")
            return
        }
        viewModelScope.launch {
            val statusId = "status-${UUID.randomUUID()}"
            replaceStatus(statusId, Replies.ACK_TEXT, stage = 0)
            try {
                pipeline.processText(body) { status -> emitPipelineStatus(statusId, status) }
                removeTransient(statusId)
            } catch (e: Throwable) {
                replaceWithError(statusId, e)
            }
        }
    }

    // ----- voice input -----

    private var pipelineJob: Job? = null

    fun startRecording() {
        if (_recordingState.value !is RecordingState.Idle) return
        try {
            recorder.start()
            _recordingState.value = RecordingState.Recording(startedAtMs = System.currentTimeMillis())
        } catch (e: Throwable) {
            appendError("שגיאה בהקלטה: ${e.message ?: e::class.simpleName}")
        }
    }

    fun stopAndSendRecording() {
        if (_recordingState.value !is RecordingState.Recording) return
        if (!secureSettings.hasApiKey()) {
            recorder.cancel()
            _recordingState.value = RecordingState.Idle
            appendError("חסר מפתח OpenAI. פתח/י הגדרות והוסף/י מפתח.")
            return
        }
        val result =
            try {
                recorder.stop()
            } catch (e: Throwable) {
                _recordingState.value = RecordingState.Idle
                appendError("ההקלטה הייתה קצרה מדי או נכשלה: ${e.message ?: e::class.simpleName}")
                return
            }
        _recordingState.value = RecordingState.Idle

        val voiceBubbleId = "voice-${UUID.randomUUID()}"
        appendTransient(
            ChatMessage.UserVoice(
                id = voiceBubbleId,
                timestampMs = System.currentTimeMillis(),
                durationSec = result.durationSec,
            ),
        )
        runVoicePipeline(result.file, voiceBubbleId)
    }

    fun cancelRecording() {
        recorder.cancel()
        _recordingState.value = RecordingState.Idle
    }

    private fun runVoicePipeline(file: File, voiceBubbleId: String) {
        pipelineJob?.cancel()
        pipelineJob =
            viewModelScope.launch {
                val statusId = "status-${UUID.randomUUID()}"
                replaceStatus(
                    statusId,
                    Replies.statusReceived(fileSizeBytes = file.length()),
                    stage = 1,
                )
                try {
                    pipeline.processVoice(file) { status -> emitPipelineStatus(statusId, status) }
                    // Once the entry lands in Room, the transient user bubble + status are no
                    // longer needed — the persistent BotResult takes over.
                    removeTransient(statusId)
                    removeTransient(voiceBubbleId)
                } catch (e: Throwable) {
                    replaceWithError(statusId, e)
                }
            }
    }

    // ----- slash commands -----

    private fun runLast() =
        viewModelScope.launch {
            val entry = entryRepo.getLast()
            if (entry == null) appendInfo("עדיין אין רישומים.") else appendBotResult(entry)
        }

    private fun runShow(shortId: String) =
        viewModelScope.launch {
            val entry = entryRepo.getByShortId(shortId)
            if (entry == null) appendError(Replies.NOT_FOUND) else appendBotResult(entry)
        }

    private fun runQuestions(shortId: String) =
        viewModelScope.launch {
            val entry = entryRepo.getByShortId(shortId)
            if (entry == null) {
                appendError(Replies.NOT_FOUND)
            } else {
                appendInfo(Replies.formatQuestions(entry.toView()))
            }
        }

    private fun runEdit(shortId: String) =
        viewModelScope.launch {
            val entry = entryRepo.getByShortId(shortId)
            if (entry == null) {
                appendError(Replies.NOT_FOUND)
                return@launch
            }
            pending.value = Pending.Edit(shortId)
            appendInfo("התמליל הנוכחי לרישום ${entry.shortId}:\n\n${entry.transcript}\n\n${Replies.EDIT_PROMPT}")
        }

    private fun runTagsPrompt(shortId: String) =
        viewModelScope.launch {
            val entry = entryRepo.getByShortId(shortId)
            if (entry == null) {
                appendError(Replies.NOT_FOUND)
                return@launch
            }
            pending.value = Pending.Tags(shortId)
            appendInfo(Replies.formatTags(entry.toView()))
        }

    fun runRestructure(shortId: String) =
        viewModelScope.launch {
            if (!secureSettings.hasApiKey()) {
                appendError("חסר מפתח OpenAI. פתח/י הגדרות והוסף/י מפתח.")
                return@launch
            }
            val statusId = "status-${UUID.randomUUID()}"
            replaceStatus(statusId, Replies.statusStructuring(), stage = 4)
            try {
                val result = pipeline.restructure(shortId)
                removeTransient(statusId)
                if (result == null) appendError(Replies.NOT_FOUND)
            } catch (e: Throwable) {
                replaceWithError(statusId, e)
            }
        }

    fun deleteEntry(entry: EntryEntity) =
        viewModelScope.launch {
            entryRepo.delete(entry.id)
            appendInfo("רישום ${entry.shortId} נמחק.")
        }

    fun copyEntryAsJson(entry: EntryEntity): String {
        val obj =
            kotlinx.serialization.json.buildJsonObject {
                put("id", kotlinx.serialization.json.JsonPrimitive(entry.id))
                put("short_id", kotlinx.serialization.json.JsonPrimitive(entry.shortId))
                put(
                    "created_at_epoch_ms",
                    kotlinx.serialization.json.JsonPrimitive(entry.createdAtEpochMs),
                )
                put("source", kotlinx.serialization.json.JsonPrimitive(entry.source))
                put("transcript", kotlinx.serialization.json.JsonPrimitive(entry.transcript))
                put(
                    "summary",
                    entry.summary?.let { kotlinx.serialization.json.JsonPrimitive(it) }
                        ?: kotlinx.serialization.json.JsonNull,
                )
                put(
                    "tags",
                    kotlinx.serialization.json.JsonArray(
                        entry.tags.map { kotlinx.serialization.json.JsonPrimitive(it) },
                    ),
                )
                put(
                    "entities",
                    kotlinx.serialization.json.JsonArray(
                        entry.entities.map { kotlinx.serialization.json.JsonPrimitive(it) },
                    ),
                )
                put(
                    "follow_up_questions",
                    kotlinx.serialization.json.JsonArray(
                        entry.followUpQuestions.map { kotlinx.serialization.json.JsonPrimitive(it) },
                    ),
                )
                put(
                    "approx_age",
                    entry.approxAge?.let { kotlinx.serialization.json.JsonPrimitive(it) }
                        ?: kotlinx.serialization.json.JsonNull,
                )
                put(
                    "year",
                    entry.year?.let { kotlinx.serialization.json.JsonPrimitive(it) }
                        ?: kotlinx.serialization.json.JsonNull,
                )
                put("status", kotlinx.serialization.json.JsonPrimitive(entry.status))
            }
        return PRETTY_JSON.encodeToString(
            kotlinx.serialization.json.JsonObject.serializer(),
            obj,
        )
    }

    private companion object {
        private val PRETTY_JSON = kotlinx.serialization.json.Json {
            prettyPrint = true
            encodeDefaults = false
            explicitNulls = false
        }
    }

    private fun runSearch(text: String) =
        viewModelScope.launch {
            val results = entryRepo.search(text, limit = 5).map { it.toView() }
            appendInfo(Replies.formatSearchResults(results))
        }

    private fun runUsage() =
        viewModelScope.launch {
            val totals = usageRepo.totalsToday()
            val msg =
                buildString {
                    appendLine("📊 שימוש היום")
                    appendLine("• אירועים: ${totals.events}")
                    appendLine("• שניות תמלול: ${"%.1f".format(totals.transcribeSeconds)}")
                    appendLine("• טוקנים LLM: ${totals.tokensIn} ↓ / ${totals.tokensOut} ↑")
                    append("• עלות מוערכת: $${"%.4f".format(totals.costUsd.toDouble())}")
                }
            appendInfo(msg)
        }

    // ----- pending state -----

    private fun handlePending(body: String, p: Pending) {
        pending.value = null
        when (p) {
            is Pending.Edit -> {
                viewModelScope.launch {
                    val statusId = "status-${UUID.randomUUID()}"
                    replaceStatus(statusId, Replies.statusStructuring(), stage = 4)
                    try {
                        val result = pipeline.reEditTranscript(p.shortId, body)
                        removeTransient(statusId)
                        if (result == null) appendError(Replies.NOT_FOUND)
                    } catch (e: Throwable) {
                        replaceWithError(statusId, e)
                    }
                }
            }
            is Pending.Tags -> {
                val newTags = body.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val canonical = filterKnownTags(newTags).map { it.wire }
                viewModelScope.launch {
                    val updated = entryRepo.overrideTags(p.shortId, canonical)
                    if (updated == null) appendError(Replies.NOT_FOUND) else appendInfo(Replies.formatTags(updated.toView()))
                }
            }
        }
    }

    fun cancelPending() {
        if (pending.value != null) {
            pending.value = null
            appendInfo("בוטל.")
        }
    }

    // ----- transient bubble helpers -----

    private fun emitPipelineStatus(statusId: String, status: Pipeline.Status) {
        val (text, stage) =
            when (status) {
                is Pipeline.Status.Received ->
                    Replies.statusReceived(
                        audioSeconds = status.audioSeconds,
                        fileSizeBytes = status.fileSizeBytes,
                    ) to 1
                Pipeline.Status.Transcribing -> Replies.statusTranscribing() to 2
                is Pipeline.Status.Transcribed ->
                    Replies.statusTranscribed(status.computeSeconds, status.chars) to 3
                Pipeline.Status.Structuring -> Replies.statusStructuring() to 4
                Pipeline.Status.Saving -> Replies.statusSaving() to 4
            }
        replaceStatus(statusId, text, stage)
    }

    private fun replaceStatus(id: String, text: String, stage: Int) {
        transientMessages.update { current ->
            val without = current.filterNot { it.id == id }
            without +
                ChatMessage.BotStatus(
                    id = id,
                    timestampMs = System.currentTimeMillis(),
                    text = text,
                    stage = stage,
                )
        }
    }

    private fun replaceWithError(id: String, error: Throwable) {
        transientMessages.update { current ->
            val without = current.filterNot { it.id == id }
            without +
                ChatMessage.BotError(
                    id = id,
                    timestampMs = System.currentTimeMillis(),
                    text = Replies.errorMessage(error = error),
                )
        }
    }

    private fun removeTransient(id: String) {
        transientMessages.update { it.filterNot { m -> m.id == id } }
    }

    private fun appendTransient(message: ChatMessage) {
        transientMessages.update { it + message }
    }

    private fun appendInfo(text: String) =
        appendTransient(
            ChatMessage.BotInfo(
                id = "info-${UUID.randomUUID()}",
                timestampMs = System.currentTimeMillis(),
                text = text,
            ),
        )

    private fun appendError(text: String) =
        appendTransient(
            ChatMessage.BotError(
                id = "err-${UUID.randomUUID()}",
                timestampMs = System.currentTimeMillis(),
                text = text,
            ),
        )

    private fun appendBotResult(entry: EntryEntity) =
        appendTransient(
            ChatMessage.BotResult(
                id = "result-transient-${UUID.randomUUID()}",
                timestampMs = System.currentTimeMillis(),
                entry = entry,
            ),
        )

    private fun entryToUserBubble(entry: EntryEntity): ChatMessage =
        if (entry.source == EntryEntity.SOURCE_VOICE) {
            ChatMessage.UserVoice(
                id = "user-${entry.id}",
                timestampMs = entry.createdAtEpochMs,
                // We don't persist voice duration today; the bubble shows 0:00.
                durationSec = 0.0,
            )
        } else {
            ChatMessage.UserText(
                id = "user-${entry.id}",
                timestampMs = entry.createdAtEpochMs,
                body = entry.transcript,
            )
        }

    sealed interface Pending {
        val shortId: String

        data class Edit(override val shortId: String) : Pending

        data class Tags(override val shortId: String) : Pending
    }

    sealed interface RecordingState {
        data object Idle : RecordingState

        data class Recording(val startedAtMs: Long) : RecordingState
    }
}

/**
 * Compose helper: obtain a [ChatViewModel] using the [BiographyApp]
 * container. Lives at the bottom of the file so callers can simply write
 * `val vm = rememberChatViewModel()` from within a `NavHost`.
 */
@Composable
fun rememberChatViewModel(): ChatViewModel {
    val app =
        androidx.compose.ui.platform.LocalContext.current.applicationContext as BiographyApp
    val owner = LocalViewModelStoreOwner.current
        ?: error("No ViewModelStoreOwner; must be inside a NavHost")
    val factory = viewModelFactory {
        initializer {
            val c = app.container
            ChatViewModel(
                pipeline = c.pipeline,
                entryRepo = c.entryRepository,
                usageRepo = c.usageRepository,
                secureSettings = c.secureSettings,
                recorder = c.audioRecorder,
            )
        }
    }
    return viewModel(viewModelStoreOwner = owner, factory = factory)
}
