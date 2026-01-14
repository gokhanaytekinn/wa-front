package com.weatherapp.ui.screens.settings

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
 * Ayarlar ekranı için ViewModel
 * Kullanıcı tercihlerini yönetir
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * Ayarları yükler
     */
    private fun loadSettings() {
        viewModelScope.launch {
            launch {
                preferencesRepository.language.collect { language ->
                    _uiState.update { it.copy(language = language) }
                }
            }
            launch {
                preferencesRepository.temperatureUnit.collect { unit ->
                    _uiState.update { it.copy(temperatureUnit = unit) }
                }
            }
            launch {
                preferencesRepository.theme.collect { theme ->
                    _uiState.update { it.copy(theme = theme) }
                }
            }
        }
    }
    
    /**
     * Dili değiştirir
     */
    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
        }
    }
    
    /**
     * Sıcaklık birimini değiştirir
     */
    fun setTemperatureUnit(unit: String) {
        viewModelScope.launch {
            preferencesRepository.setTemperatureUnit(unit)
        }
    }
    
    /**
     * Temayı değiştirir
     */
    fun setTheme(theme: String) {
        viewModelScope.launch {
            preferencesRepository.setTheme(theme)
        }
    }
}

/**
 * Ayarlar ekranı UI durumu
 */
data class SettingsUiState(
    val language: String = "en",
    val temperatureUnit: String = "celsius",
    val theme: String = "dark"
)
