package com.personalbiography.data.remote

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File

class OpenAiTranscriberTest {
    private lateinit var server: MockWebServer
    private lateinit var api: OpenAiApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val authInterceptor =
            Interceptor { chain ->
                chain.proceed(chain.request().newBuilder().addHeader("Authorization", "Bearer test").build())
            }
        val client = OkHttpClient.Builder().addInterceptor(authInterceptor).build()
        val json = Json { ignoreUnknownKeys = true }
        api =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(OpenAiApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `multipart hits transcriptions endpoint with form fields`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(
                        """{"text":" שלום עולם ","duration":4.2,"language":"he"}""",
                    ),
            )
            val tempFile =
                File.createTempFile("audio", ".m4a").apply {
                    writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03))
                    deleteOnExit()
                }
            val transcriber = OpenAiTranscriber(api, model = "whisper-1", language = "he")

            val result = transcriber.transcribe(tempFile)

            assertEquals("שלום עולם", result.text)
            assertEquals(4.2, result.audioSeconds, 0.001)
            assertTrue(result.computeSeconds >= 0.0)

            val recorded = server.takeRequest()
            assertEquals("/v1/audio/transcriptions", recorded.path)
            assertEquals("Bearer test", recorded.getHeader("Authorization"))
            val bodyString = recorded.body.readUtf8()
            assertTrue("expected multipart with model field", bodyString.contains("name=\"model\""))
            assertTrue("expected whisper-1 model value", bodyString.contains("whisper-1"))
            assertTrue("expected language field", bodyString.contains("name=\"language\""))
            assertTrue(bodyString.contains("he"))
            assertTrue("expected response_format field", bodyString.contains("name=\"response_format\""))
            assertTrue(bodyString.contains("verbose_json"))
            assertTrue("expected file part", bodyString.contains("name=\"file\""))
        }

    @Test
    fun `empty file fails fast without hitting the network`() =
        runTest {
            val tempFile = File.createTempFile("empty", ".m4a").apply { deleteOnExit() }
            val transcriber = OpenAiTranscriber(api, model = "whisper-1", language = "he")
            try {
                transcriber.transcribe(tempFile)
                error("expected IllegalArgumentException")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message!!.contains("empty"))
            }
            assertEquals(0, server.requestCount)
        }
}
