package com.weatherapp.data.adapter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.weatherapp.data.model.WeatherData
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * WeatherDataDeserializer için unit test
 * Flat ve nested JSON formatlarının doğru şekilde deserialize edildiğini test eder
 */
class WeatherDataDeserializerTest {
    
    private lateinit var gson: Gson
    
    @Before
    fun setup() {
        gson = GsonBuilder()
            .setLenient()
            .registerTypeAdapter(Long::class.java, TimestampAdapter())
            .registerTypeAdapter(Long::class.javaObjectType, TimestampAdapter())
            .registerTypeAdapter(WeatherData::class.java, WeatherDataDeserializer())
            .create()
    }
    
    @Test
    fun `should deserialize flat backend format correctly`() {
        // Given - Backend'in gerçekte gönderdiği format
        val flatJson = """
            {
              "city": "Ankara",
              "district": "Ankara",
              "temperature": 1.3,
              "temperatureUnit": "C",
              "feelsLike": -1.5,
              "humidity": 86,
              "windSpeed": 3.6,
              "precipitation": 0,
              "description": "Bulutlu",
              "weatherCode": "3",
              "source": "Open-Meteo",
              "timestamp": "2026-01-15T23:45"
            }
        """.trimIndent()
        
        // When
        val result = gson.fromJson(flatJson, WeatherData::class.java)
        
        // Then
        assertNotNull(result)
        assertNotNull(result.location)
        assertNotNull(result.sources)
        
        // Location kontrolü
        assertEquals("Ankara", result.location?.city)
        assertEquals("Ankara", result.location?.district)
        
        // Sources kontrolü
        assertEquals(1, result.sources?.size)
        val source = result.sources?.firstOrNull()
        assertNotNull(source)
        assertEquals("Open-Meteo", source?.sourceName)
        
        // CurrentWeather kontrolü
        val current = source?.current
        assertNotNull(current)
        assertEquals(1.3, current?.temperature ?: 0.0, 0.001)
        assertEquals(-1.5, current?.feelsLike ?: 0.0, 0.001)
        assertEquals(86, current?.humidity)
        assertEquals(3.6, current?.windSpeed ?: 0.0, 0.001)
        assertEquals(0.0, current?.precipitation ?: 0.0, 0.001)
        assertEquals("Bulutlu", current?.condition)
        assertEquals("3", current?.icon)
        
        // Timestamp kontrolü (String ISO 8601 olarak geldi)
        assertTrue(result.timestamp > 0)
    }
    
    @Test
    fun `should deserialize nested API spec format correctly`() {
        // Given - API spec formatı
        val nestedJson = """
            {
                "location": {
                    "city": "Istanbul",
                    "district": "Kadikoy",
                    "country": "Turkey",
                    "latitude": 41.0082,
                    "longitude": 28.9784
                },
                "sources": [
                    {
                        "source_name": "OpenWeather",
                        "current": {
                            "temperature": 20.5,
                            "feels_like": 19.0,
                            "humidity": 65,
                            "wind_speed": 15.0,
                            "precipitation": 0.0,
                            "pressure": 1013,
                            "visibility": 10.0,
                            "uv_index": 5,
                            "condition": "Clear",
                            "icon": "01d"
                        }
                    }
                ],
                "timestamp": 1640000000000
            }
        """.trimIndent()
        
        // When
        val result = gson.fromJson(nestedJson, WeatherData::class.java)
        
        // Then
        assertNotNull(result)
        assertNotNull(result.location)
        assertNotNull(result.sources)
        
        // Location kontrolü
        assertEquals("Istanbul", result.location?.city)
        assertEquals("Kadikoy", result.location?.district)
        assertEquals("Turkey", result.location?.country)
        
        // Sources kontrolü
        assertEquals(1, result.sources?.size)
        val source = result.sources?.firstOrNull()
        assertEquals("OpenWeather", source?.sourceName)
        
        // Timestamp kontrolü
        assertEquals(1640000000000L, result.timestamp)
    }
    
    @Test
    fun `should handle flat format with Long timestamp`() {
        // Given - Flat format ama Long timestamp ile
        val json = """
            {
              "city": "Izmir",
              "district": "Konak",
              "temperature": 15.5,
              "feelsLike": 14.0,
              "humidity": 70,
              "windSpeed": 5.2,
              "precipitation": 0.5,
              "description": "Parçalı Bulutlu",
              "weatherCode": "2",
              "source": "WeatherAPI",
              "timestamp": 1640000000000
            }
        """.trimIndent()
        
        // When
        val result = gson.fromJson(json, WeatherData::class.java)
        
        // Then
        assertNotNull(result)
        assertEquals("Izmir", result.location?.city)
        assertEquals("Konak", result.location?.district)
        assertEquals(1640000000000L, result.timestamp)
    }
    
    @Test
    fun `should handle flat format with missing optional fields`() {
        // Given - Minimum gerekli alanlarla flat format
        val json = """
            {
              "city": "Bursa",
              "temperature": 12.0,
              "feelsLike": 10.0,
              "humidity": 80,
              "windSpeed": 2.5,
              "precipitation": 0,
              "description": "Açık",
              "source": "TestSource",
              "timestamp": "2026-01-15T12:00"
            }
        """.trimIndent()
        
        // When
        val result = gson.fromJson(json, WeatherData::class.java)
        
        // Then
        assertNotNull(result)
        assertEquals("Bursa", result.location?.city)
        assertNull(result.location?.district) // district yok
        assertEquals("TestSource", result.sources?.firstOrNull()?.sourceName)
    }
}
