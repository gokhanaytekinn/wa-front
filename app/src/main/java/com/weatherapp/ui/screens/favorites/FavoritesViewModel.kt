package com.weatherapp.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherapp.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Favoriler ekranı için ViewModel
 * Favori konumları yönetir
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    /**
     * Favori konumları yükler
     */
    private fun loadFavorites() {
        viewModelScope.launch {
            preferencesRepository.favoriteLocations.collect { favorites ->
                _uiState.update { 
                    it.copy(
                        favoriteLocations = favorites.toList(),
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Favori konumu siler
     */
    fun removeFavorite(location: String) {
        viewModelScope.launch {
            preferencesRepository.removeFavoriteLocation(location)
        }
    }
}

/**
 * Favoriler ekranı UI durumu
 */
data class FavoritesUiState(
    val favoriteLocations: List<String> = emptyList(),
    val isLoading: Boolean = true
)
