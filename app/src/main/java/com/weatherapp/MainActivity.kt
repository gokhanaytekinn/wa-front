package com.weatherapp

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
import javax.inject.Inject

/**
 * Ana Activity
 * Uygulamanın giriş noktası ve tema yönetimi
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesRepository: PreferencesRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge görünüm için window insets kontrolünü aç
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
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
}
