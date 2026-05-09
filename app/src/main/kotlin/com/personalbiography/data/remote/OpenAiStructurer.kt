package com.personalbiography.data.remote

import com.personalbiography.domain.Prompts
import com.personalbiography.domain.StructureResult
import com.personalbiography.domain.Structured
import com.personalbiography.domain.Structurer
import com.personalbiography.domain.Tag
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Calls `POST /v1/chat/completions` with a `response_format` of
 * `{type: json_schema, json_schema: ...}` so the model returns a strict
 * Structured JSON payload. Direct port of `app/pipeline/structure.py`.
 */
class OpenAiStructurer(
    private val api: OpenAiApi,
    private val model: String,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : Structurer {
    override suspend fun structure(transcript: String): StructureResult {
        val request =
            ChatRequest(
                model = model,
                messages =
                listOf(
                    ChatMessageDto(role = "system", content = Prompts.STRUCTURING_SYSTEM),
                    ChatMessageDto(role = "user", content = Prompts.userPrompt(transcript)),
                ),
                temperature = 0.2,
                responseFormat = STRUCTURED_RESPONSE_FORMAT,
            )
        val response = api.chatCompletion(request)
        val content =
            response.choices.firstOrNull()?.message?.content
                ?: throw SerializationException("OpenAI returned no content")
        val data: Structured =
            try {
                json.decodeFromString(Structured.serializer(), content)
            } catch (e: SerializationException) {
                throw SerializationException("OpenAI returned non-conforming JSON: ${e.message}")
            }
        val usage = response.usage
        return StructureResult(
            data = data,
            tokensIn = usage?.promptTokens ?: 0,
            tokensOut = usage?.completionTokens ?: 0,
        )
    }

    companion object {
        /**
         * JSON Schema baked into the request so OpenAI's structured-outputs
         * mode constrains the response. Mirrors the `Structured` pydantic
         * model in `app/pipeline/structure.py` — including the strict tag
         * enum, the [3, 5] follow-up-question bounds, and nullable timeline
         * fields modeled as `["integer", "null"]`.
         */
        val STRUCTURED_RESPONSE_FORMAT: JsonObject =
            buildJsonObject {
                put("type", "json_schema")
                putJsonObject("json_schema") {
                    put("name", "Structured")
                    put("strict", true)
                    put("schema", structuredSchema())
                }
            }

        private fun structuredSchema(): JsonObject =
            buildJsonObject {
                put("type", "object")
                put("additionalProperties", false)
                putJsonArray("required") {
                    add("summary")
                    add("tags")
                    add("entities")
                    add("timeline")
                    add("follow_up_questions")
                }
                putJsonObject("properties") {
                    putJsonObject("summary") { put("type", "string") }
                    putJsonObject("tags") {
                        put("type", "array")
                        putJsonObject("items") {
                            put("type", "string")
                            put("enum", JsonArray(Tag.ALL_WIRE.map { JsonPrimitive(it) }))
                        }
                    }
                    putJsonObject("entities") {
                        put("type", "array")
                        putJsonObject("items") { put("type", "string") }
                    }
                    putJsonObject("timeline") {
                        put("type", "object")
                        put("additionalProperties", false)
                        putJsonArray("required") {
                            add("approx_age")
                            add("year")
                        }
                        putJsonObject("properties") {
                            putJsonObject("approx_age") {
                                put("type", JsonArray(listOf(JsonPrimitive("integer"), JsonPrimitive("null"))))
                            }
                            putJsonObject("year") {
                                put("type", JsonArray(listOf(JsonPrimitive("integer"), JsonPrimitive("null"))))
                            }
                        }
                    }
                    putJsonObject("follow_up_questions") {
                        put("type", "array")
                        putJsonObject("items") { put("type", "string") }
                        put("minItems", 3)
                        put("maxItems", 5)
                    }
                }
            }
    }
}
