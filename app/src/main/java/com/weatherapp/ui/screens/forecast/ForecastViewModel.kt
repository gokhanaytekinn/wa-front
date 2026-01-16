package com.weatherapp.ui.screens.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherapp.data.model.LocationSearchResult
import com.weatherapp.data.model.WeatherData
import com.weatherapp.data.repository.PreferencesRepository
import com.weatherapp.data.repository.WeatherRepository
import com.weatherapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        // Son seçilen konumu yükle
        loadLastSelectedLocation()
        
        // Sıcaklık birimini dinle
        observeTemperatureUnit()
        
        // Arama sorgusunu dinle
        setupSearchQueryListener()
    }
    
    /**
     * Son seçilen konumu yükler ve tahmin verilerini getirir
     */
    private fun loadLastSelectedLocation() {
        viewModelScope.launch {
            // Use combine to get both city and district together
            preferencesRepository.lastSelectedCity.combine(
                preferencesRepository.lastSelectedDistrict
            ) { city, district ->
                Pair(city, district)
            }
            .distinctUntilChanged() // Only emit when values actually change
            .collect { (city, district) ->
                // Load forecast data if we have a city
                if (city != null) {
                    loadForecastData(city, district)
                    // Update search query to show selected location
                    updateSearchQueryForLocation(city, district)
                }
            }
        }
    }
    
    /**
     * Konum için arama sorgusunu günceller
     * Güncellenen sorgu, setupSearchQueryListener'da selectedCity ile kontrol edilerek
     * gereksiz arama yapılması engellenir
     */
    private fun updateSearchQueryForLocation(city: String, district: String?) {
        val newQuery = if (district != null) {
            "$district, $city"
        } else {
            city
        }
        // Update query - search listener will skip this since it matches selected location
        _searchQuery.value = newQuery
        // Clear search results when location is selected
        _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
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
     * Arama sorgusunu dinler ve debounce uygular
     */
    @OptIn(FlowPreview::class)
    private fun setupSearchQueryListener() {
        _searchQuery
            .debounce(500)
            .distinctUntilChanged()
            .onEach { query ->
                // Mevcut seçili konumun query'si ile aynıysa arama yapma
                val currentLocationQuery = _uiState.value.selectedCity?.let { city ->
                    _uiState.value.selectedDistrict?.let { "$it, $city" } ?: city
                }
                
                if (query.isNotBlank() && query != currentLocationQuery) {
                    searchLocations(query)
                } else if (query.isBlank()) {
                    _uiState.update { it.copy(searchResults = emptyList()) }
                }
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Konum arar
     */
    private fun searchLocations(query: String) {
        viewModelScope.launch {
            weatherRepository.searchLocations(query).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSearching = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                searchResults = resource.data ?: emptyList(),
                                isSearching = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                searchResults = emptyList(),
                                isSearching = false
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Arama sorgusunu günceller
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Konum seçer
     */
    fun selectLocation(location: LocationSearchResult) {
        loadForecastData(location.city, location.district)
        // Seçilen konumu arama kutusunda göster
        _searchQuery.value = location.getDisplayName()
        _uiState.update { it.copy(searchResults = emptyList()) }
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
                                error = null,
                                selectedCity = city,
                                selectedDistrict = district
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = resource.message,
                                errorResponse = resource.errorResponse
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
        _uiState.update { it.copy(error = null, errorResponse = null) }
    }
}

/**
 * Tahmin ekranı UI durumu
 */
data class ForecastUiState(
    val weatherData: WeatherData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorResponse: com.weatherapp.data.model.ApiErrorResponse? = null,
    val temperatureUnit: String = "celsius",
    val searchResults: List<LocationSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedCity: String? = null,
    val selectedDistrict: String? = null
)
