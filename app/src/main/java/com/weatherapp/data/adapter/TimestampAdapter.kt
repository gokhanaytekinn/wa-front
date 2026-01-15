package com.weatherapp.data.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Timestamp alanı için özel Gson TypeAdapter
 * 
 * Backend'den gelen timestamp değerlerini esnek şekilde işler:
 * - Long (Unix epoch milliseconds): 1640000000000
 * - String (ISO 8601): "2026-01-15T23:15" veya "2026-01-15T23:15:00"
 * 
 * Her iki formatı da Long değerine çevirir.
 */
class TimestampAdapter : TypeAdapter<Long>() {
    
    override fun write(out: JsonWriter, value: Long?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }
    
    override fun read(`in`: JsonReader): Long {
        return when (`in`.peek()) {
            JsonToken.NUMBER -> {
                // Long olarak geliyorsa direkt oku
                `in`.nextLong()
            }
            JsonToken.STRING -> {
                // String olarak geliyorsa parse et
                val timestampString = `in`.nextString()
                parseTimestampString(timestampString)
            }
            JsonToken.NULL -> {
                `in`.nextNull()
                System.currentTimeMillis()
            }
            else -> {
                throw IllegalStateException("Beklenmeyen JSON token tipi: ${`in`.peek()}")
            }
        }
    }
    
    /**
     * String timestamp'i Long'a çevirir
     * Desteklenen formatlar:
     * - ISO 8601 with time: "2026-01-15T23:15"
     * - ISO 8601 with seconds: "2026-01-15T23:15:00"
     * - ISO 8601 full: "2026-01-15T23:15:00.000"
     */
    private fun parseTimestampString(timestampString: String): Long {
        return try {
            // ISO 8601 formatında parse et
            val dateTime = when {
                // "2026-01-15T23:15" formatı
                timestampString.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) -> {
                    LocalDateTime.parse(timestampString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                }
                // "2026-01-15T23:15:00" veya daha uzun formatlar
                else -> {
                    LocalDateTime.parse(timestampString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                }
            }
            
            // LocalDateTime'ı Unix timestamp'e çevir
            dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            // Parse edilemezse current time döndür
            System.currentTimeMillis()
        } catch (e: Exception) {
            // Diğer hatalar için de current time döndür
            System.currentTimeMillis()
        }
    }
}
