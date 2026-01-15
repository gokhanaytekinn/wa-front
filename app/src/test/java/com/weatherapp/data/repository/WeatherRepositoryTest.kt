package com.weatherapp.data.repository

import com.google.gson.Gson
import com.weatherapp.data.api.WeatherApiService
import com.weatherapp.data.model.*
import com.weatherapp.util.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.Response

/**
 * WeatherRepository için unit test sınıfı
 * API hata yanıtlarının doğru şekilde parse edildiğini test eder
 */
class WeatherRepositoryTest {
    
    private lateinit var apiService: WeatherApiService
    private lateinit var gson: Gson
    private lateinit var repository: WeatherRepository
    
    @Before
    fun setup() {
        apiService = mock()
        gson = Gson()
        repository = WeatherRepository(apiService, gson)
    }
    
    @Test
    fun `getCurrentWeather should parse error response correctly`() = runTest {
        // Given
        val errorJson = """
            {
                "message": "Konum bulunamadı: Etimesgut Ankara",
                "status": 400,
                "timestamp": "2026-01-15T22:46:08.2376856",
                "path": "/api/weather/current"
            }
        """.trimIndent()
        
        val errorBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<WeatherData>(400, errorBody)
        
        whenever(apiService.getCurrentWeather(any(), any())).thenReturn(errorResponse)
        
        // When
        val flow = repository.getCurrentWeather("Ankara", "Etimesgut")
        val loadingResult = flow.first()
        
        // Skip loading and get the error
        val errorResult = repository.getCurrentWeather("Ankara", "Etimesgut")
            .first { it is Resource.Error }
        
        // Then
        assert(errorResult is Resource.Error)
        errorResult as Resource.Error
        assert(errorResult.message == "Konum bulunamadı: Etimesgut Ankara")
        assert(errorResult.errorResponse != null)
        assert(errorResult.errorResponse?.status == 400)
        assert(errorResult.errorResponse?.path == "/api/weather/current")
    }
    
    @Test
    fun `getCurrentWeather should handle successful response`() = runTest {
        // Given
        val mockWeatherData = createMockWeatherData()
        val successResponse = Response.success(mockWeatherData)
        
        whenever(apiService.getCurrentWeather(any(), any())).thenReturn(successResponse)
        
        // When
        val flow = repository.getCurrentWeather("Istanbul", null)
        
        // Skip loading and get the success
        val successResult = flow.first { it is Resource.Success }
        
        // Then
        assert(successResult is Resource.Success)
        successResult as Resource.Success
        assert(successResult.data != null)
        assert(successResult.data?.location?.city == "Istanbul")
    }
    
    @Test
    fun `getForecast should parse error response correctly`() = runTest {
        // Given
        val errorJson = """
            {
                "message": "Tahmin verileri alınamadı",
                "status": 404,
                "timestamp": "2026-01-15T22:46:08.2376856",
                "path": "/api/weather/forecast"
            }
        """.trimIndent()
        
        val errorBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<WeatherData>(404, errorBody)
        
        whenever(apiService.getForecast(any(), any(), any())).thenReturn(errorResponse)
        
        // When
        val flow = repository.getForecast("Istanbul", null)
        val errorResult = flow.first { it is Resource.Error }
        
        // Then
        assert(errorResult is Resource.Error)
        errorResult as Resource.Error
        assert(errorResult.message == "Tahmin verileri alınamadı")
        assert(errorResult.errorResponse != null)
        assert(errorResult.errorResponse?.status == 404)
        assert(errorResult.errorResponse?.path == "/api/weather/forecast")
    }
    
    @Test
    fun `searchLocations should parse error response correctly`() = runTest {
        // Given
        val errorJson = """
            {
                "message": "Konum araması başarısız",
                "status": 500,
                "timestamp": "2026-01-15T22:46:08.2376856",
                "path": "/api/location/search"
            }
        """.trimIndent()
        
        val errorBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<List<LocationSearchResult>>(500, errorBody)
        
        whenever(apiService.searchLocations(any())).thenReturn(errorResponse)
        
        // When
        val flow = repository.searchLocations("test")
        val errorResult = flow.first { it is Resource.Error }
        
        // Then
        assert(errorResult is Resource.Error)
        errorResult as Resource.Error
        assert(errorResult.message == "Konum araması başarısız")
        assert(errorResult.errorResponse != null)
        assert(errorResult.errorResponse?.status == 500)
        assert(errorResult.errorResponse?.path == "/api/location/search")
    }
    
    @Test
    fun `getCurrentWeather should return generic error message when error body is null`() = runTest {
        // Given
        val errorResponse = Response.error<WeatherData>(
            400,
            "".toResponseBody("application/json".toMediaTypeOrNull())
        )
        
        whenever(apiService.getCurrentWeather(any(), any())).thenReturn(errorResponse)
        
        // When
        val flow = repository.getCurrentWeather("Istanbul", null)
        val errorResult = flow.first { it is Resource.Error }
        
        // Then
        assert(errorResult is Resource.Error)
        errorResult as Resource.Error
        assert(errorResult.message == "Hava durumu verileri alınamadı")
        assert(errorResult.errorResponse == null)
    }
    
    private fun createMockWeatherData(): WeatherData {
        return WeatherData(
            location = Location(
                city = "Istanbul",
                district = null,
                country = "Turkey",
                latitude = 41.0082,
                longitude = 28.9784
            ),
            sources = listOf(
                WeatherSource(
                    sourceName = "OpenWeather",
                    current = CurrentWeather(
                        temperature = 20.0,
                        feelsLike = 18.0,
                        humidity = 65,
                        windSpeed = 15.0,
                        precipitation = 0.0,
                        pressure = 1013,
                        visibility = 10.0,
                        uvIndex = 5,
                        condition = "Clear",
                        icon = null
                    ),
                    forecast = null
                )
            ),
            timestamp = System.currentTimeMillis()
        )
    }
}
