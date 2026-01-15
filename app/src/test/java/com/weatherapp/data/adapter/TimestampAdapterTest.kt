package com.weatherapp.data.adapter

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * TimestampAdapter için unit test
 * String ve Long timestamp formatlarının doğru şekilde işlendiğini test eder
 */
class TimestampAdapterTest {
    
    private lateinit var adapter: TimestampAdapter
    
    @Before
    fun setup() {
        adapter = TimestampAdapter()
    }
    
    @Test
    fun `read should parse Long timestamp correctly`() {
        // Given
        val timestampMillis = 1640000000000L
        val jsonString = timestampMillis.toString()
        val reader = JsonReader(StringReader(jsonString))
        
        // When
        val result = adapter.read(reader)
        
        // Then
        assertEquals(timestampMillis, result)
    }
    
    @Test
    fun `read should parse ISO 8601 timestamp without seconds correctly`() {
        // Given
        val timestampString = "\"2026-01-15T23:15\""
        val reader = JsonReader(StringReader(timestampString))
        
        // When
        val result = adapter.read(reader)
        
        // Then
        // Verify the result is a valid timestamp (not 0 or negative)
        assertTrue(result > 0)
        
        // Verify the timestamp is close to the expected date
        val expectedDateTime = LocalDateTime.of(2026, 1, 15, 23, 15)
        val expectedMillis = expectedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expectedMillis, result)
    }
    
    @Test
    fun `read should parse ISO 8601 timestamp with seconds correctly`() {
        // Given
        val timestampString = "\"2026-01-15T23:15:00\""
        val reader = JsonReader(StringReader(timestampString))
        
        // When
        val result = adapter.read(reader)
        
        // Then
        assertTrue(result > 0)
        
        val expectedDateTime = LocalDateTime.of(2026, 1, 15, 23, 15, 0)
        val expectedMillis = expectedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expectedMillis, result)
    }
    
    @Test
    fun `read should return current time for null value`() {
        // Given
        val jsonString = "null"
        val reader = JsonReader(StringReader(jsonString))
        val beforeTest = System.currentTimeMillis()
        
        // When
        val result = adapter.read(reader)
        
        // Then
        val afterTest = System.currentTimeMillis()
        assertTrue(result >= beforeTest && result <= afterTest)
    }
    
    @Test
    fun `read should return current time for invalid string`() {
        // Given
        val timestampString = "\"invalid-timestamp\""
        val reader = JsonReader(StringReader(timestampString))
        val beforeTest = System.currentTimeMillis()
        
        // When
        val result = adapter.read(reader)
        
        // Then
        val afterTest = System.currentTimeMillis()
        assertTrue(result >= beforeTest && result <= afterTest)
    }
    
    @Test
    fun `write should write Long value correctly`() {
        // Given
        val timestampMillis = 1640000000000L
        val stringWriter = StringWriter()
        val writer = JsonWriter(stringWriter)
        
        // When
        adapter.write(writer, timestampMillis)
        
        // Then
        assertEquals(timestampMillis.toString(), stringWriter.toString())
    }
    
    @Test
    fun `write should write null value correctly`() {
        // Given
        val stringWriter = StringWriter()
        val writer = JsonWriter(stringWriter)
        
        // When
        adapter.write(writer, null)
        
        // Then
        assertEquals("null", stringWriter.toString())
    }
}
