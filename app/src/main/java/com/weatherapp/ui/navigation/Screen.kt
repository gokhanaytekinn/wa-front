package com.weatherapp.ui.navigation

/**
 * Uygulama navigasyon rotaları
 * Her ekran için bir rota tanımlar
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Forecast : Screen("forecast")
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
}

/**
 * Alt navigasyon çubuğu öğeleri
 */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Forecast,
    Screen.Favorites,
    Screen.Settings
)
