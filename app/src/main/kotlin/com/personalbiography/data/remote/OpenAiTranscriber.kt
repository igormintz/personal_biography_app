package com.personalbiography.data.remote

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Hebrew transcription via OpenAI's `audio.transcriptions` endpoint.
 *
 * Direct port of `_transcribe_with_openai` in
 * `app/pipeline/transcribe.py`. The legacy `faster-whisper` path is gone —
 * the on-device app always uses the API.
 */
class OpenAiTranscriber(
    private val api: OpenAiApi,
    private val model: String,
    private val language: String = "he",
) {
    data class Result(
        val text: String,
        val audioSeconds: Double,
        val computeSeconds: Double,
    )

    suspend fun transcribe(audioFile: File): Result {
        require(audioFile.exists() && audioFile.length() > 0) {
            "audio file does not exist or is empty: ${audioFile.absolutePath}"
        }
        val mime = guessMime(audioFile)
        val filePart =
            MultipartBody.Part.createFormData(
                name = "file",
                filename = audioFile.name,
                body = audioFile.asRequestBody(mime.toMediaTypeOrNull()),
            )
        val started = System.nanoTime()
        val response =
            api.transcribe(
                file = filePart,
                model = textPart(model),
                language = textPart(language),
                responseFormat = textPart("verbose_json"),
            )
        val elapsed = (System.nanoTime() - started) / 1_000_000_000.0
        return Result(
            text = response.text.trim(),
            audioSeconds = response.duration,
            computeSeconds = elapsed,
        )
    }

    private fun textPart(value: String): RequestBody = value.toRequestBody("text/plain".toMediaTypeOrNull())

    private fun guessMime(file: File): String =
        when (file.extension.lowercase()) {
            "m4a" -> "audio/mp4"
            "mp3" -> "audio/mpeg"
            "ogg", "oga" -> "audio/ogg"
            "wav" -> "audio/wav"
            "webm" -> "audio/webm"
            "flac" -> "audio/flac"
            else -> "application/octet-stream"
        }
}
