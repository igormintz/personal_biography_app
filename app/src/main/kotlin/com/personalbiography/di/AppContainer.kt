package com.personalbiography.di

import android.content.Context
import com.personalbiography.data.db.BiographyDatabase
import com.personalbiography.data.remote.OpenAiClient
import com.personalbiography.data.remote.OpenAiStructurer
import com.personalbiography.data.remote.OpenAiTranscriber
import com.personalbiography.data.repo.EntryRepository
import com.personalbiography.data.repo.UsageRepository
import com.personalbiography.data.secure.SecureSettings
import com.personalbiography.domain.AudioRecorder
import com.personalbiography.domain.Pipeline

/**
 * Manual DI container — no Hilt for MVP. Built once in
 * [com.personalbiography.BiographyApp.onCreate] and accessed from ViewModels
 * by reaching back to the Application via the [Context.applicationContext].
 *
 * Heavy/long-lived objects (database, OkHttp/Retrofit) are eagerly built
 * once. The OpenAI key is read lazily on each request so updating it from
 * the Settings screen takes effect immediately without rebuilding Retrofit.
 */
class AppContainer private constructor(
    appContext: Context,
) {
    val database: BiographyDatabase = BiographyDatabase.build(appContext)
    val entryRepository: EntryRepository = EntryRepository(database.entryDao())
    val usageRepository: UsageRepository = UsageRepository(database.usageDao())
    val secureSettings: SecureSettings = SecureSettings(appContext)
    val audioRecorder: AudioRecorder = AudioRecorder(appContext)

    private val openAiClient: OpenAiClient =
        OpenAiClient(apiKeySupplier = { secureSettings.openAiApiKey })

    val transcriber: OpenAiTranscriber
        get() =
            OpenAiTranscriber(
                api = openAiClient.api,
                model = secureSettings.transcribeModel,
                language = secureSettings.transcribeLanguage,
            )

    val structurer: OpenAiStructurer
        get() =
            OpenAiStructurer(
                api = openAiClient.api,
                model = secureSettings.chatModel,
                json = openAiClient.json,
            )

    val pipeline: Pipeline
        get() =
            Pipeline(
                transcriber = transcriber,
                structurer = structurer,
                entryRepo = entryRepository,
                usageRepo = usageRepository,
            )

    companion object {
        fun create(context: Context): AppContainer = AppContainer(context.applicationContext)
    }
}
