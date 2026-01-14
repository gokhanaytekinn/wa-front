package com.weatherapp.data.api

import com.weatherapp.data.model.LocationSearchResult
import com.weatherapp.data.model.WeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Hava durumu API servisi için Retrofit interface
 * Backend API endpoint'lerini tanımlar
 */
interface WeatherApiService {
    
    /**
     * Belirtilen konum için güncel hava durumu verilerini getirir
     * @param city Şehir adı
     * @param district İlçe adı (opsiyonel)
     * @return Hava durumu verisi
     */
    @GET("weather/current")
    suspend fun getCurrentWeather(
        @Query("city") city: String,
        @Query("district") district: String? = null
    ): Response<WeatherData>
    
    /**
     * Belirtilen konum için 5 günlük hava durumu tahminini getirir
     * @param city Şehir adı
     * @param district İlçe adı (opsiyonel)
     * @return Tahmin verileri içeren hava durumu verisi
     */
    @GET("weather/forecast")
    suspend fun getForecast(
        @Query("city") city: String,
        @Query("district") district: String? = null,
        @Query("days") days: Int = 5
    ): Response<WeatherData>
    
    /**
     * Konum arama - şehir ve ilçe otomatik tamamlama
     * @param query Arama sorgusu
     * @return Eşleşen konum listesi
     */
    @GET("location/search")
    suspend fun searchLocations(
        @Query("q") query: String
    ): Response<List<LocationSearchResult>>
}
