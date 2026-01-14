package com.weatherapp.ui.screens.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherapp.data.model.WeatherData
import com.weatherapp.data.repository.PreferencesRepository
import com.weatherapp.data.repository.WeatherRepository
import com.weatherapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tahmin ekranı için ViewModel
 * 5 günlük hava durumu tahminini yönetir
 */
@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()
    
    init {
        // Son seçilen konumu yükle
        loadLastSelectedLocation()
        
        // Sıcaklık birimini dinle
        observeTemperatureUnit()
    }
    
    /**
     * Son seçilen konumu yükler ve tahmin verilerini getirir
     */
    private fun loadLastSelectedLocation() {
        viewModelScope.launch {
            preferencesRepository.lastSelectedCity.collect { city ->
                if (city != null) {
                    preferencesRepository.lastSelectedDistrict.collect { district ->
                        loadForecastData(city, district)
                    }
                }
            }
        }
    }
    
    /**
     * Sıcaklık birimini gözlemler
     */
    private fun observeTemperatureUnit() {
        viewModelScope.launch {
            preferencesRepository.temperatureUnit.collect { unit ->
                _uiState.update { it.copy(temperatureUnit = unit) }
            }
        }
    }
    
    /**
     * Tahmin verilerini yükler
     */
    fun loadForecastData(city: String, district: String? = null) {
        viewModelScope.launch {
            weatherRepository.getForecast(city, district).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                weatherData = resource.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Hata mesajını temizler
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * Tahmin ekranı UI durumu
 */
data class ForecastUiState(
    val weatherData: WeatherData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val temperatureUnit: String = "celsius"
)
