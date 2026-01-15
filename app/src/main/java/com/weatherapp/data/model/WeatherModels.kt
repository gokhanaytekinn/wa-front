package com.weatherapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Hava durumu verisi için veri modeli
 * Backend API'den gelen hava durumu bilgisini temsil eder
 */
data class WeatherData(
    @SerializedName("location")
    val location: Location,
    
    @SerializedName("sources")
    val sources: List<WeatherSource>,
    
    @SerializedName("timestamp")
    val timestamp: Long
)

/**
 * Konum bilgisi için veri modeli
 */
data class Location(
    @SerializedName("city")
    val city: String,
    
    @SerializedName("district")
    val district: String? = null,
    
    @SerializedName("country")
    val country: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)

/**
 * Hava durumu kaynağı (farklı hava durumu servislerinden gelen veri)
 */
data class WeatherSource(
    @SerializedName("source_name")
    val sourceName: String,
    
    @SerializedName("current")
    val current: CurrentWeather,
    
    @SerializedName("forecast")
    val forecast: List<ForecastDay>? = null
)

/**
 * Güncel hava durumu bilgisi
 */
data class CurrentWeather(
    @SerializedName("temperature")
    val temperature: Double,
    
    @SerializedName("feels_like")
    val feelsLike: Double,
    
    @SerializedName("humidity")
    val humidity: Int,
    
    @SerializedName("wind_speed")
    val windSpeed: Double,
    
    @SerializedName("precipitation")
    val precipitation: Double,
    
    @SerializedName("pressure")
    val pressure: Int,
    
    @SerializedName("visibility")
    val visibility: Double,
    
    @SerializedName("uv_index")
    val uvIndex: Int,
    
    @SerializedName("condition")
    val condition: String,
    
    @SerializedName("icon")
    val icon: String? = null
)

/**
 * Tahmin günü verisi
 */
data class ForecastDay(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("day")
    val day: DayWeather,
    
    @SerializedName("hourly")
    val hourly: List<HourlyWeather>? = null
)

/**
 * Günlük hava durumu özeti
 */
data class DayWeather(
    @SerializedName("max_temp")
    val maxTemp: Double,
    
    @SerializedName("min_temp")
    val minTemp: Double,
    
    @SerializedName("avg_temp")
    val avgTemp: Double,
    
    @SerializedName("condition")
    val condition: String,
    
    @SerializedName("icon")
    val icon: String? = null,
    
    @SerializedName("precipitation_chance")
    val precipitationChance: Int,
    
    @SerializedName("humidity")
    val humidity: Int
)

/**
 * Saatlik hava durumu verisi
 */
data class HourlyWeather(
    @SerializedName("time")
    val time: String,
    
    @SerializedName("temperature")
    val temperature: Double,
    
    @SerializedName("condition")
    val condition: String,
    
    @SerializedName("icon")
    val icon: String? = null,
    
    @SerializedName("precipitation_chance")
    val precipitationChance: Int
)

/**
 * Konum arama sonucu
 */
data class LocationSearchResult(
    @SerializedName("city")
    val city: String,
    
    @SerializedName("district")
    val district: String? = null,
    
    @SerializedName("country")
    val country: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
) {
    fun getDisplayName(): String {
        return if (district != null) {
            "$district, $city"
        } else {
            city
        }
    }
}

/**
 * API hata yanıtı
 * Backend'den dönen hata mesajlarını temsil eder
 */
data class ApiErrorResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("status")
    val status: Int,
    
    @SerializedName("timestamp")
    val timestamp: String? = null,
    
    @SerializedName("path")
    val path: String? = null
)
