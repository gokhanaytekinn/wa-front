package com.weatherapp.data.repository

import com.google.gson.Gson
import com.weatherapp.data.api.WeatherApiService
import com.weatherapp.data.model.ApiErrorResponse
import com.weatherapp.data.model.LocationSearchResult
import com.weatherapp.data.model.WeatherData
import com.weatherapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hava durumu verilerini yönetmek için repository
 * API çağrılarını ve veri akışını yönetir
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val gson: Gson
) {
    
    /**
     * Güncel hava durumu verilerini getirir
     * @param city Şehir adı
     * @param district İlçe adı (opsiyonel)
     * @return Flow ile sarılmış Resource<WeatherData>
     */
    fun getCurrentWeather(city: String, district: String? = null): Flow<Resource<WeatherData>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getCurrentWeather(city, district)
            if (response.isSuccessful && response.body() != null) {
                val weatherData = response.body()!!
                // Backend yanıt formatı doğrulaması
                if (!isValidWeatherData(weatherData)) {
                    emit(Resource.Error(
                        message = "Backend API formatı hatalı. Beklenen veri yapısı ile uyuşmuyor."
                    ))
                } else {
                    emit(Resource.Success(weatherData))
                }
            } else {
                val errorResponse = parseErrorResponse(response.errorBody()?.string())
                emit(Resource.Error(
                    message = errorResponse?.message ?: "Hava durumu verileri alınamadı",
                    errorResponse = errorResponse
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: "Bilinmeyen bir hata oluştu"))
        }
    }
    
    /**
     * 5 günlük hava durumu tahminini getirir
     * @param city Şehir adı
     * @param district İlçe adı (opsiyonel)
     * @return Flow ile sarılmış Resource<ForecastResponse>
     */
    fun getForecast(city: String, district: String? = null): Flow<Resource<com.weatherapp.data.model.ForecastResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getForecast(city, district, days = 5)
            if (response.isSuccessful && response.body() != null) {
                val forecastData = response.body()!!
                emit(Resource.Success(forecastData))
            } else {
                val errorResponse = parseErrorResponse(response.errorBody()?.string())
                emit(Resource.Error(
                    message = errorResponse?.message ?: "Tahmin verileri alınamadı",
                    errorResponse = errorResponse
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: "Bilinmeyen bir hata oluştu"))
        }
    }
    
    /**
     * Konum arama - otomatik tamamlama
     * @param query Arama sorgusu
     * @return Flow ile sarılmış Resource<List<LocationSearchResult>>
     */
    fun searchLocations(query: String): Flow<Resource<List<LocationSearchResult>>> = flow {
        if (query.isBlank()) {
            emit(Resource.Success(emptyList()))
            return@flow
        }
        
        emit(Resource.Loading())
        try {
            val response = apiService.searchLocations(query)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorResponse = parseErrorResponse(response.errorBody()?.string())
                emit(Resource.Error(
                    message = errorResponse?.message ?: "Konum araması başarısız",
                    errorResponse = errorResponse
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: "Bilinmeyen bir hata oluştu"))
        }
    }
    
    /**
     * WeatherData yanıt yapısını doğrular
     * @param weatherData Doğrulanacak WeatherData
     * @return Veri geçerliyse true, değilse false
     */
    private fun isValidWeatherData(weatherData: WeatherData): Boolean {
        return weatherData.sources != null && weatherData.location != null
    }
    
    /**
     * Hata yanıtını parse eder
     * @param errorBody Hata yanıtı body
     * @return ApiErrorResponse veya null
     */
    private fun parseErrorResponse(errorBody: String?): ApiErrorResponse? {
        return try {
            if (errorBody != null) {
                gson.fromJson(errorBody, ApiErrorResponse::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
