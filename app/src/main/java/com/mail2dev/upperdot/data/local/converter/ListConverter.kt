package com.mail2dev.upperdot.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun toList(json: String): List<String> {
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
