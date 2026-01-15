package com.weatherapp.ui.screens.home

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ana sayfa için ViewModel
 * Güncel hava durumu verilerini ve konum aramasını yönetir
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        // Son seçilen konumu yükle
        loadLastSelectedLocation()
        
        // Arama sorgusunu dinle ve otomatik tamamlama yap
        setupSearchQueryListener()
        
        // Sıcaklık birimini dinle
        observeTemperatureUnit()
        
        // Favori durumunu gözlemle
        observeFavoriteStatus()
    }
    
    /**
     * Son seçilen konumu yükler
     */
    private fun loadLastSelectedLocation() {
        viewModelScope.launch {
            // Use combine to get both city and district together
            preferencesRepository.lastSelectedCity.combine(
                preferencesRepository.lastSelectedDistrict
            ) { city, district ->
                Pair(city, district)
            }.collect { (city, district) ->
                // Update UI state
                _uiState.update { it.copy(selectedCity = city, selectedDistrict = district) }
                
                // Load weather data if we have a city
                if (city != null) {
                    loadWeatherData(city, district)
                    // Update search query to show selected location
                    updateSearchQueryForLocation(city, district)
                }
            }
        }
    }
    
    /**
     * Konum için arama sorgusunu günceller
     */
    private fun updateSearchQueryForLocation(city: String, district: String?) {
        _searchQuery.value = if (district != null) {
            "$district, $city"
        } else {
            city
        }
    }
    
    /**
     * Arama sorgusunu dinler ve debounce uygular
     */
    @OptIn(FlowPreview::class)
    private fun setupSearchQueryListener() {
        _searchQuery
            .debounce(500) // 500ms bekle
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotBlank()) {
                    searchLocations(query)
                } else {
                    _uiState.update { it.copy(searchResults = emptyList()) }
                }
            }
            .launchIn(viewModelScope)
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
     * Favori durumunu gözlemler
     */
    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            // Combine location and favorites to avoid race conditions
            combine(
                preferencesRepository.lastSelectedCity,
                preferencesRepository.lastSelectedDistrict,
                preferencesRepository.favoriteLocations
            ) { city, district, favorites ->
                val currentLocation = "${city ?: ""}${district?.let { ",$it" } ?: ""}"
                favorites.contains(currentLocation)
            }.collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }
    
    /**
     * Hava durumu verilerini yükler
     */
    fun loadWeatherData(city: String, district: String? = null) {
        viewModelScope.launch {
            weatherRepository.getCurrentWeather(city, district).collect { resource ->
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
                        // Son seçilen konumu kaydet
                        preferencesRepository.setLastSelectedLocation(city, district)
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
        loadWeatherData(location.city, location.district)
        // Seçilen konumu arama kutusunda göster
        _searchQuery.value = location.getDisplayName()
        _uiState.update { it.copy(searchResults = emptyList()) }
    }
    
    /**
     * Hata mesajını temizler
     */
    fun clearError() {
        _uiState.update { it.copy(error = null, errorResponse = null) }
    }
    
    /**
     * Favori konuma ekler/çıkarır
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            val currentLocation = "${_uiState.value.selectedCity}${_uiState.value.selectedDistrict?.let { ",$it" } ?: ""}"
            
            // Use first() instead of collect to get a single value
            val favorites = preferencesRepository.favoriteLocations.first()
            if (favorites.contains(currentLocation)) {
                preferencesRepository.removeFavoriteLocation(currentLocation)
                _uiState.update { it.copy(isFavorite = false) }
            } else {
                preferencesRepository.addFavoriteLocation(currentLocation)
                _uiState.update { it.copy(isFavorite = true) }
            }
        }
    }
}

/**
 * Ana sayfa UI durumu
 */
data class HomeUiState(
    val weatherData: WeatherData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorResponse: com.weatherapp.data.model.ApiErrorResponse? = null,
    val searchResults: List<LocationSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedCity: String? = null,
    val selectedDistrict: String? = null,
    val temperatureUnit: String = "celsius",
    val isFavorite: Boolean = false
)
