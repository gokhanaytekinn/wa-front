package com.weatherapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Ana uygulama sınıfı
 * Hilt dependency injection için gerekli
 */
@HiltAndroidApp
class WeatherApplication : Application()
