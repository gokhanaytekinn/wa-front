package com.weatherapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.weatherapp.data.repository.PreferencesRepository
import com.weatherapp.ui.navigation.NavigationGraph
import com.weatherapp.ui.theme.WeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

/**
 * Ana Activity
 * Uygulamanın giriş noktası ve tema yönetimi
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesRepository: PreferencesRepository
    
    private var currentLanguage: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge görünüm için window insets kontrolünü aç
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            // Dil durumunu yönet
            var languageState by remember { mutableStateOf("en") }
            
            // Dil tercihini yükle ve değişiklikleri dinle
            LaunchedEffect(Unit) {
                preferencesRepository.language.collect { language ->
                    languageState = language
                    // Dil değiştiğinde locale'i güncelle
                    if (currentLanguage != null && currentLanguage != language) {
                        updateLocale(language)
                        recreate() // Activity'yi yeniden oluştur
                    }
                    currentLanguage = language
                    updateLocale(language)
                }
            }
            
            // Tema durumunu yönet
            var themeState by remember { mutableStateOf("dark") }
            
            // Tema tercihini yükle
            LaunchedEffect(Unit) {
                preferencesRepository.theme.collect { theme ->
                    themeState = theme
                }
            }
            
            // Sistem koyu tema durumu
            val systemDarkTheme = isSystemInDarkTheme()
            
            // Etkin koyu tema durumu
            val isDarkTheme = when (themeState) {
                "light" -> false
                "dark" -> true
                "system" -> systemDarkTheme
                else -> true // Varsayılan olarak koyu tema
            }
            
            WeatherAppTheme(darkTheme = isDarkTheme) {
                NavigationGraph(preferencesRepository = preferencesRepository)
            }
        }
    }
    
    /**
     * Uygulama dilini günceller
     */
    private fun updateLocale(language: String) {
        val locale = when (language) {
            "tr" -> Locale("tr", "TR")
            else -> Locale("en", "US")
        }
        Locale.setDefault(locale)
        
        val config = resources.configuration
        config.setLocale(locale)
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Context'i de güncelle
        createConfigurationContext(config)
    }
}
