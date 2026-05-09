package com.personalbiography.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Verbose-JSON response from `POST /v1/audio/transcriptions`. */
@Serializable
data class TranscriptionResponse(
    val text: String = "",
    val duration: Double = 0.0,
    val language: String? = null,
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val temperature: Double = 0.2,
    @SerialName("response_format")
    val responseFormat: JsonObject? = null,
)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String,
)

@Serializable
data class ChatResponse(
    val choices: List<ChatChoice> = emptyList(),
    val usage: ChatUsage? = null,
)

@Serializable
data class ChatChoice(
    val index: Int = 0,
    val message: ChatChoiceMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@Serializable
data class ChatChoiceMessage(
    val role: String = "assistant",
    val content: String? = null,
    val refusal: String? = null,
    @SerialName("parsed")
    val parsed: JsonElement? = null,
)

@Serializable
data class ChatUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
)
