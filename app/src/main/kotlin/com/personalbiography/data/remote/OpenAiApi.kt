package com.personalbiography.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OpenAiApi {
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("language") language: RequestBody,
        @Part("response_format") responseFormat: RequestBody,
    ): TranscriptionResponse

    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Body request: ChatRequest,
    ): ChatResponse
}
