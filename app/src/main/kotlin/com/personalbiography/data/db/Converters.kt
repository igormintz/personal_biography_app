package com.personalbiography.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }
private val stringListSerializer = ListSerializer(String.serializer())

class Converters {
    @TypeConverter
    fun listToJson(value: List<String>): String = json.encodeToString(stringListSerializer, value)

    @TypeConverter
    fun jsonToList(value: String): List<String> = if (value.isBlank()) emptyList() else json.decodeFromString(stringListSerializer, value)
}
