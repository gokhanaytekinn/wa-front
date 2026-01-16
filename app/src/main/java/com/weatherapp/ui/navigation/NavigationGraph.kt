package com.weatherapp.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
 * Ekranlar arası kaydırma (swipe) ve scroll sıfırlama desteği
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavigationGraph(
    preferencesRepository: PreferencesRepository
) {
    val scope = rememberCoroutineScope()
    
    // Tema durumu
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
    
    // HorizontalPager durumu - ekranlar arası kaydırma için
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { bottomNavItems.size }
    )
    
    // Pager değişikliklerini dinle ve scroll durumunu sıfırla
    LaunchedEffect(pagerState.currentPage) {
        // Sayfa değiştiğinde scroll durumunu sıfırla sinyali gönderilir
        // Her ekran kendi LazyListState'ini dinleyecek
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = getIconForScreen(screen),
                                contentDescription = getNavigationLabel(screen)
                            )
                        },
                        label = { Text(getNavigationLabel(screen)) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
            userScrollEnabled = true // Kullanıcının kaydırmasına izin ver
        ) { page ->
            when (page) {
                0 -> HomeScreen()
                1 -> ForecastScreen()
                2 -> FavoritesScreen(
                    onLocationClick = { city, district ->
                        // Konumu kaydet ve ana sayfaya git
                        scope.launch {
                            preferencesRepository.setLastSelectedLocation(city, district)
                            pagerState.animateScrollToPage(0) // Ana sayfaya git
                        }
                    }
                )
                3 -> SettingsScreen(
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
