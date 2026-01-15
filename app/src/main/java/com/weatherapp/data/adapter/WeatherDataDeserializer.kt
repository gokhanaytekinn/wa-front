package com.weatherapp.data.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.weatherapp.data.model.*
import java.lang.reflect.Type

/**
 * WeatherData için özel deserializer
 * 
 * Backend'in gönderdiği flat JSON yapısını frontend'in beklediği nested yapıya dönüştürür.
 * 
 * Backend formatı:
 * {
 *   "city": "Ankara",
 *   "district": "Ankara",
 *   "temperature": 1.3,
 *   "temperatureUnit": "C",
 *   "feelsLike": -1.5,
 *   "humidity": 86,
 *   "windSpeed": 3.6,
 *   "precipitation": 0,
 *   "description": "Bulutlu",
 *   "weatherCode": "3",
 *   "source": "Open-Meteo",
 *   "timestamp": "2026-01-15T23:45"
 * }
 * 
 * Frontend formatı:
 * {
 *   "location": { "city": "...", "district": "...", ... },
 *   "sources": [{ "source_name": "...", "current": {...} }],
 *   "timestamp": 1234567890
 * }
 */
class WeatherDataDeserializer : JsonDeserializer<WeatherData> {
    
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): WeatherData {
        val jsonObject = json.asJsonObject
        
        // Backend'in gönderdiği flat formatı tespit et
        val isFlat = jsonObject.has("city") && 
                     jsonObject.has("temperature") && 
                     !jsonObject.has("location") && 
                     !jsonObject.has("sources")
        
        return if (isFlat) {
            // Flat formatı nested yapıya dönüştür
            deserializeFlatFormat(jsonObject, context)
        } else {
            // Normal nested formatı kullan
            deserializeNestedFormat(jsonObject, context)
        }
    }
    
    /**
     * Flat backend formatını nested yapıya dönüştürür
     */
    private fun deserializeFlatFormat(
        json: JsonObject,
        context: JsonDeserializationContext
    ): WeatherData {
        // Location oluştur
        val location = Location(
            city = json.get("city")?.asString ?: "",
            district = json.get("district")?.asString,
            country = "Turkey", // Backend göndermediği için varsayılan
            latitude = 0.0, // Backend göndermediği için varsayılan
            longitude = 0.0  // Backend göndermediği için varsayılan
        )
        
        // CurrentWeather oluştur
        val currentWeather = CurrentWeather(
            temperature = json.get("temperature")?.asDouble ?: 0.0,
            feelsLike = json.get("feelsLike")?.asDouble ?: 0.0,
            humidity = json.get("humidity")?.asInt ?: 0,
            windSpeed = json.get("windSpeed")?.asDouble ?: 0.0,
            precipitation = json.get("precipitation")?.asDouble ?: 0.0,
            pressure = 0, // Backend göndermediği için varsayılan
            visibility = 0.0, // Backend göndermediği için varsayılan
            uvIndex = 0, // Backend göndermediği için varsayılan
            condition = json.get("description")?.asString ?: "",
            icon = json.get("weatherCode")?.asString
        )
        
        // WeatherSource oluştur
        val source = WeatherSource(
            sourceName = json.get("source")?.asString ?: "Unknown",
            current = currentWeather,
            forecast = null
        )
        
        // Timestamp'i parse et (String veya Long olabilir)
        val timestamp = parseTimestamp(json.get("timestamp"), context)
        
        return WeatherData(
            location = location,
            sources = listOf(source),
            timestamp = timestamp
        )
    }
    
    /**
     * Normal nested formatı deserialize eder
     */
    private fun deserializeNestedFormat(
        json: JsonObject,
        context: JsonDeserializationContext
    ): WeatherData {
        val location = if (json.has("location")) {
            context.deserialize<Location>(json.get("location"), Location::class.java)
        } else {
            null
        }
        
        val sources = if (json.has("sources")) {
            context.deserialize<List<WeatherSource>>(
                json.get("sources"),
                object : com.google.gson.reflect.TypeToken<List<WeatherSource>>() {}.type
            )
        } else {
            null
        }
        
        val timestamp = parseTimestamp(json.get("timestamp"), context)
        
        return WeatherData(
            location = location,
            sources = sources,
            timestamp = timestamp
        )
    }
    
    /**
     * Timestamp'i parse eder (String ISO 8601 veya Long Unix epoch)
     */
    private fun parseTimestamp(
        timestampElement: JsonElement?,
        context: JsonDeserializationContext
    ): Long {
        if (timestampElement == null || timestampElement.isJsonNull) {
            return System.currentTimeMillis()
        }
        
        return if (timestampElement.isJsonPrimitive) {
            val primitive = timestampElement.asJsonPrimitive
            if (primitive.isNumber) {
                primitive.asLong
            } else {
                // String timestamp - TimestampAdapter kullanarak parse et
                context.deserialize(timestampElement, Long::class.java)
            }
        } else {
            System.currentTimeMillis()
        }
    }
}
