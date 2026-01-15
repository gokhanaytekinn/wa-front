package com.weatherapp.data.adapter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.weatherapp.data.model.WeatherData
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TimestampAdapter'ın Gson ile entegrasyonunu test eder
 * Gerçek JSON yanıtlarının doğru şekilde parse edildiğini doğrular
 */
class TimestampAdapterIntegrationTest {
    
    private lateinit var gson: Gson
    
    @Before
    fun setup() {
        gson = GsonBuilder()
            .setLenient()
            .registerTypeAdapter(Long::class.java, TimestampAdapter())
            .registerTypeAdapter(Long::class.javaObjectType, TimestampAdapter())
            .create()
    }
    
    @Test
    fun `should parse JSON with Long timestamp`() {
        // Given - API spec formatı
        val json = """
            {
                "location": {
                    "city": "Istanbul",
                    "district": "Kadikoy",
                    "country": "Turkey",
                    "latitude": 41.0082,
                    "longitude": 28.9784
                },
                "sources": [],
                "timestamp": 1640000000000
            }
        """.trimIndent()
        
        // When
        val result = gson.fromJson(json, WeatherData::class.java)
        
        // Then
        assertNotNull(result)
        assertTrue(result.timestamp > 0)
    }
    
    @Test
    fun `should parse JSON with ISO 8601 timestamp string without seconds`() {
        // Given - Backend'in gerçekte gönderdiği format
        val json = """
            {
                "location": {
                    "city": "Ankara",
                    "district": "Ankara",
                    "country": "Turkey",
                    "latitude": 39.9334,
                    "longitude": 32.8597
                },
                "sources": [],
                "timestamp": "2026-01-15T23:15"
            }
        """.trimIndent()
        
        // When - Bu önceden NumberFormatException fırlatıyordu
        val result = gson.fromJson(json, WeatherData::class.java)
        
        // Then
        assertNotNull(result)
        assertTrue(result.timestamp > 0)
        // Timestamp 2026 yılına ait olmalı (Unix epoch > 1700000000000)
        assertTrue(result.timestamp > 1700000000000L)
    }
    
    @Test
    fun `should parse JSON with ISO 8601 timestamp string with seconds`() {
        // Given
        val json = """
            {
                "location": {
                    "city": "Ankara",
                    "district": "Ankara",
                    "country": "Turkey",
                    "latitude": 39.9334,
                    "longitude": 32.8597
                },
                "sources": [],
                "timestamp": "2026-01-15T23:15:00"
            }
        """.trimIndent()
        
        // When
        val result = gson.fromJson(json, WeatherData::class.java)
        
        // Then
        assertNotNull(result)
        assertTrue(result.timestamp > 0)
    }
    
    @Test
    fun `should handle invalid timestamp gracefully`() {
        // Given
        val json = """
            {
                "location": {
                    "city": "Istanbul",
                    "district": null,
                    "country": "Turkey",
                    "latitude": 41.0082,
                    "longitude": 28.9784
                },
                "sources": [],
                "timestamp": "invalid-format"
            }
        """.trimIndent()
        
        // When - İnvalid timestamp'de exception fırlatmamalı, current time kullanmalı
        val beforeTest = System.currentTimeMillis()
        val result = gson.fromJson(json, WeatherData::class.java)
        val afterTest = System.currentTimeMillis()
        
        // Then
        assertNotNull(result)
        assertTrue(result.timestamp > 0)
        // Invalid timestamp durumunda current time döndürülmeli
        assertTrue(result.timestamp >= beforeTest && result.timestamp <= afterTest)
    }
    
    @Test
    fun `should handle null timestamp gracefully`() {
        // Given
        val json = """
            {
                "location": {
                    "city": "Istanbul",
                    "district": null,
                    "country": "Turkey",
                    "latitude": 41.0082,
                    "longitude": 28.9784
                },
                "sources": [],
                "timestamp": null
            }
        """.trimIndent()
        
        // When
        val beforeTest = System.currentTimeMillis()
        val result = gson.fromJson(json, WeatherData::class.java)
        val afterTest = System.currentTimeMillis()
        
        // Then
        assertNotNull(result)
        assertTrue(result.timestamp > 0)
        assertTrue(result.timestamp >= beforeTest && result.timestamp <= afterTest)
    }
}
