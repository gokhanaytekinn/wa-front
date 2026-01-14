package com.weatherapp.data.repository

import com.weatherapp.data.api.WeatherApiService
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
    private val apiService: WeatherApiService
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
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(message = "Hava durumu verileri alınamadı"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: "Bilinmeyen bir hata oluştu"))
        }
    }
    
    /**
     * 5 günlük hava durumu tahminini getirir
     * @param city Şehir adı
     * @param district İlçe adı (opsiyonel)
     * @return Flow ile sarılmış Resource<WeatherData>
     */
    fun getForecast(city: String, district: String? = null): Flow<Resource<WeatherData>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getForecast(city, district, days = 5)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(message = "Tahmin verileri alınamadı"))
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
                emit(Resource.Error(message = "Konum araması başarısız"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: "Bilinmeyen bir hata oluştu"))
        }
    }
}
