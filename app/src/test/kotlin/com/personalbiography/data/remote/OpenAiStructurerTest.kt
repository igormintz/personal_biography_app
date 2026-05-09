package com.personalbiography.data.remote

import com.personalbiography.domain.Tag
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class OpenAiStructurerTest {
    private lateinit var server: MockWebServer
    private lateinit var api: OpenAiApi
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val authInterceptor =
            Interceptor { chain ->
                chain.proceed(chain.request().newBuilder().addHeader("Authorization", "Bearer test").build())
            }
        val client = OkHttpClient.Builder().addInterceptor(authInterceptor).build()
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
    fun `structure parses chat completion content into Structured`() =
        runTest {
            // OpenAI returns the JSON as a *string* in `content`.
            val structuredContent =
                """{"summary":"זיכרון מילדות","tags":["childhood","family"],"entities":["חיפה"],"timeline":{"approx_age":7,"year":1990},"follow_up_questions":["מי?","איפה?","למה?"]}"""
            val responseBody =
                """{"choices":[{"index":0,"message":{"role":"assistant","content":${escapeForJson(
                    structuredContent,
                )}}}],"usage":{"prompt_tokens":42,"completion_tokens":18}}"""
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(responseBody),
            )

            val structurer = OpenAiStructurer(api, model = "gpt-4o-mini")
            val result = structurer.structure("טקסט עברית")

            assertEquals("זיכרון מילדות", result.data.summary)
            assertEquals(listOf(Tag.CHILDHOOD, Tag.FAMILY), result.data.tags)
            assertEquals(listOf("חיפה"), result.data.entities)
            assertEquals(7, result.data.timeline.approxAge)
            assertEquals(1990, result.data.timeline.year)
            assertEquals(listOf("מי?", "איפה?", "למה?"), result.data.followUpQuestions)
            assertEquals(42, result.tokensIn)
            assertEquals(18, result.tokensOut)

            val recorded = server.takeRequest()
            assertEquals("/v1/chat/completions", recorded.path)
            assertEquals("Bearer test", recorded.getHeader("Authorization"))
            val sentBody = recorded.body.readUtf8()
            assertTrue(sentBody.contains("\"model\":\"gpt-4o-mini\""))
            assertTrue("expected response_format json_schema", sentBody.contains("\"json_schema\""))
            assertTrue("expected strict true", sentBody.contains("\"strict\":true"))
            // Tag enum should be embedded
            assertTrue(sentBody.contains("childhood"))
            assertTrue(sentBody.contains("daily_life"))
        }

    @Test
    fun `non-conforming json raises SerializationException`() =
        runTest {
            val badContent = """{"summary":"x","tags":[],"entities":[],"timeline":{"approx_age":null,"year":null}}"""
            // missing follow_up_questions
            val responseBody =
                """{"choices":[{"index":0,"message":{"role":"assistant","content":${escapeForJson(
                    badContent,
                )}}}],"usage":{"prompt_tokens":10,"completion_tokens":5}}"""
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(responseBody),
            )
            val structurer = OpenAiStructurer(api, model = "gpt-4o-mini")
            try {
                structurer.structure("טקסט")
                fail("expected SerializationException")
            } catch (e: SerializationException) {
                assertTrue(e.message!!.contains("non-conforming"))
            }
        }

    @Test
    fun `unknown tag values are rejected by deserialization`() =
        runTest {
            val bad =
                """{"summary":"x","tags":["family","not_a_real_tag"],"entities":[],"timeline":{"approx_age":null,"year":null},"follow_up_questions":["a","b","c"]}"""
            val responseBody =
                """{"choices":[{"index":0,"message":{"role":"assistant","content":${escapeForJson(
                    bad,
                )}}}],"usage":{"prompt_tokens":1,"completion_tokens":1}}"""
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(responseBody),
            )
            val structurer = OpenAiStructurer(api, model = "gpt-4o-mini")
            try {
                structurer.structure("טקסט")
                fail("expected SerializationException")
            } catch (_: SerializationException) {
                // OK — kotlinx.serialization rejects unknown enum values for Tag.
            }
        }

    @Test
    fun `empty choices raises SerializationException`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"choices":[]}"""),
            )
            val structurer = OpenAiStructurer(api, model = "gpt-4o-mini")
            try {
                structurer.structure("טקסט")
                fail("expected SerializationException")
            } catch (e: SerializationException) {
                assertTrue(e.message!!.contains("no content"))
            }
        }

    private fun escapeForJson(s: String): String = json.encodeToString(String.serializer(), s)
}
