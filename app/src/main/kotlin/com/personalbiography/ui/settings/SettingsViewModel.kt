package com.personalbiography.ui.settings

import android.content.Context
import android.os.Environment
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
import com.personalbiography.data.repo.UsageTotals
import com.personalbiography.data.secure.SecureSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SettingsUiState(
    val apiKeyMasked: String = "",
    val hasApiKey: Boolean = false,
    val chatModel: String = SecureSettings.DEFAULT_CHAT_MODEL,
    val transcribeModel: String = SecureSettings.DEFAULT_TRANSCRIBE_MODEL,
    val language: String = SecureSettings.DEFAULT_LANGUAGE,
    val todayUsage: UsageTotals =
        UsageTotals(events = 0, costUsd = BigDecimal.ZERO, transcribeSeconds = 0.0, tokensIn = 0, tokensOut = 0),
    val lastExportPath: String? = null,
    val errorMessage: String? = null,
)

class SettingsViewModel(
    private val secureSettings: SecureSettings,
    private val usageRepo: UsageRepository,
    private val entryRepo: EntryRepository,
    private val appContext: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(snapshotInitial())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        refreshUsage()
    }

    fun saveApiKey(value: String) {
        secureSettings.openAiApiKey = value.trim().ifBlank { null }
        _state.value = _state.value.copy(
            apiKeyMasked = mask(secureSettings.openAiApiKey),
            hasApiKey = secureSettings.hasApiKey(),
        )
    }

    fun setChatModel(model: String) {
        secureSettings.chatModel = model
        _state.value = _state.value.copy(chatModel = model)
    }

    fun setTranscribeModel(model: String) {
        secureSettings.transcribeModel = model
        _state.value = _state.value.copy(transcribeModel = model)
    }

    fun setLanguage(language: String) {
        secureSettings.transcribeLanguage = language
        _state.value = _state.value.copy(language = language)
    }

    fun refreshUsage() {
        viewModelScope.launch {
            val totals = usageRepo.totalsToday()
            _state.value = _state.value.copy(todayUsage = totals)
        }
    }

    fun exportAll() {
        viewModelScope.launch {
            try {
                val entries = entryRepo.dumpAll()
                val payload = entries.map(::entryToJson)
                val arr = JsonArray(payload)
                val text = PRETTY_JSON.encodeToString(JsonArray.serializer(), arr)

                val docs =
                    appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        ?: appContext.filesDir
                val outFile =
                    File(
                        docs,
                        "biography_export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json",
                    )
                outFile.parentFile?.mkdirs()
                outFile.writeText(text)
                _state.value = _state.value.copy(lastExportPath = outFile.absolutePath, errorMessage = null)
            } catch (e: Throwable) {
                _state.value = _state.value.copy(errorMessage = e.message ?: e::class.simpleName)
            }
        }
    }

    private fun entryToJson(entry: EntryEntity): JsonObject =
        JsonObject(
            mapOf(
                "id" to JsonPrimitive(entry.id),
                "short_id" to JsonPrimitive(entry.shortId),
                "created_at_epoch_ms" to JsonPrimitive(entry.createdAtEpochMs),
                "source" to JsonPrimitive(entry.source),
                "transcript" to JsonPrimitive(entry.transcript),
                "summary" to (entry.summary?.let(::JsonPrimitive) ?: JsonNull),
                "tags" to JsonArray(entry.tags.map(::JsonPrimitive)),
                "entities" to JsonArray(entry.entities.map(::JsonPrimitive)),
                "follow_up_questions" to JsonArray(entry.followUpQuestions.map(::JsonPrimitive)),
                "approx_age" to (entry.approxAge?.let(::JsonPrimitive) ?: JsonNull),
                "year" to (entry.year?.let(::JsonPrimitive) ?: JsonNull),
                "status" to JsonPrimitive(entry.status),
            ),
        )

    private fun snapshotInitial(): SettingsUiState =
        SettingsUiState(
            apiKeyMasked = mask(secureSettings.openAiApiKey),
            hasApiKey = secureSettings.hasApiKey(),
            chatModel = secureSettings.chatModel,
            transcribeModel = secureSettings.transcribeModel,
            language = secureSettings.transcribeLanguage,
        )

    private fun mask(value: String?): String {
        if (value.isNullOrBlank()) return ""
        if (value.length <= 8) return "•".repeat(value.length)
        return "••••" + value.takeLast(4)
    }

    private companion object {
        private val PRETTY_JSON =
            Json {
                prettyPrint = true
                encodeDefaults = false
                explicitNulls = false
            }
    }
}

@Composable
fun rememberSettingsViewModel(): SettingsViewModel {
    val app =
        androidx.compose.ui.platform.LocalContext.current.applicationContext as BiographyApp
    val owner = LocalViewModelStoreOwner.current
        ?: error("No ViewModelStoreOwner; must be inside a NavHost")
    val factory = viewModelFactory {
        initializer {
            val c = app.container
            SettingsViewModel(
                secureSettings = c.secureSettings,
                usageRepo = c.usageRepository,
                entryRepo = c.entryRepository,
                appContext = app,
            )
        }
    }
    return viewModel(viewModelStoreOwner = owner, factory = factory)
}
