package com.personalbiography.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton-ish factory for the OpenAI API. The API key is read at request
 * time from a supplier so we don't have to rebuild Retrofit when the user
 * updates it in Settings.
 */
class OpenAiClient(
    private val apiKeySupplier: () -> String?,
    private val baseUrl: String = "https://api.openai.com/",
    enableLogging: Boolean = false,
) {
    val json: Json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            explicitNulls = false
        }

    private val authInterceptor =
        Interceptor { chain ->
            val key =
                apiKeySupplier()
                    ?: error("No OpenAI API key configured. Open Settings to add one.")
            val req =
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $key")
                    .build()
            chain.proceed(req)
        }

    private val httpClient: OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .apply {
                if (enableLogging) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.HEADERS
                        },
                    )
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

    val api: OpenAiApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenAiApi::class.java)
}
