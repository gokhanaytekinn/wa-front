package com.weatherapp.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.weatherapp.R
import com.weatherapp.data.repository.PreferencesRepository
import com.weatherapp.ui.screens.favorites.FavoritesScreen
import com.weatherapp.ui.screens.forecast.ForecastScreen
import com.weatherapp.ui.screens.home.HomeScreen
import com.weatherapp.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

/**
 * Ana navigasyon yapısı
 * Alt navigasyon çubuğu ile birlikte tüm ekranları yönetir
 */
@Composable
fun NavigationGraph(
    preferencesRepository: PreferencesRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Tema durumu
    val scope = rememberCoroutineScope()
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
        else -> true
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = getIconForScreen(screen),
                                contentDescription = getNavigationLabel(screen)
                            )
                        },
                        label = { Text(getNavigationLabel(screen)) },
                        selected = currentDestination?.hierarchy?.any { 
                            it.route == screen.route 
                        } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            
            composable(Screen.Forecast.route) {
                ForecastScreen()
            }
            
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onLocationClick = { city, district ->
                        // Konumu kaydet ve ana sayfaya git
                        scope.launch {
                            preferencesRepository.setLastSelectedLocation(city, district)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onThemeChange = { newTheme ->
                        scope.launch {
                            themeState = newTheme
                        }
                    }
                )
            }
        }
    }
}

/**
 * Ekran için ikon döndürür
 */
@Composable
fun getIconForScreen(screen: Screen): androidx.compose.ui.graphics.vector.ImageVector {
    return when (screen) {
        Screen.Home -> Icons.Default.Home
        Screen.Forecast -> Icons.Default.CalendarMonth
        Screen.Favorites -> Icons.Default.Favorite
        Screen.Settings -> Icons.Default.Settings
    }
}

/**
 * Navigasyon etiketi döndürür
 */
@Composable
fun getNavigationLabel(screen: Screen): String {
    return when (screen) {
        Screen.Home -> stringResource(R.string.nav_home)
        Screen.Forecast -> stringResource(R.string.nav_forecast)
        Screen.Favorites -> stringResource(R.string.nav_favorites)
        Screen.Settings -> stringResource(R.string.nav_settings)
    }
}
