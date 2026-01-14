package com.weatherapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kullanıcı tercihlerini yönetmek için DataStore repository
 * Dil, tema, sıcaklık birimi ve favori konumlar gibi ayarları saklar
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferenceKeys {
        val LANGUAGE = stringPreferencesKey("language")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val THEME = stringPreferencesKey("theme")
        val FAVORITE_LOCATIONS = stringSetPreferencesKey("favorite_locations")
        val LAST_SELECTED_CITY = stringPreferencesKey("last_selected_city")
        val LAST_SELECTED_DISTRICT = stringPreferencesKey("last_selected_district")
    }
    
    /**
     * Dil tercihini getirir (varsayılan: "en")
     */
    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.LANGUAGE] ?: "en"
    }
    
    /**
     * Sıcaklık birimi tercihini getirir (varsayılan: "celsius")
     */
    val temperatureUnit: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.TEMPERATURE_UNIT] ?: "celsius"
    }
    
    /**
     * Tema tercihini getirir (varsayılan: "dark")
     */
    val theme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.THEME] ?: "dark"
    }
    
    /**
     * Favori konumları getirir
     */
    val favoriteLocations: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.FAVORITE_LOCATIONS] ?: emptySet()
    }
    
    /**
     * Son seçilen şehri getirir
     */
    val lastSelectedCity: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.LAST_SELECTED_CITY]
    }
    
    /**
     * Son seçilen ilçeyi getirir
     */
    val lastSelectedDistrict: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.LAST_SELECTED_DISTRICT]
    }
    
    /**
     * Dil tercihini ayarlar
     */
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LANGUAGE] = language
        }
    }
    
    /**
     * Sıcaklık birimi tercihini ayarlar
     */
    suspend fun setTemperatureUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.TEMPERATURE_UNIT] = unit
        }
    }
    
    /**
     * Tema tercihini ayarlar
     */
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME] = theme
        }
    }
    
    /**
     * Favori konum ekler
     */
    suspend fun addFavoriteLocation(location: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[PreferenceKeys.FAVORITE_LOCATIONS] ?: emptySet()
            preferences[PreferenceKeys.FAVORITE_LOCATIONS] = currentFavorites + location
        }
    }
    
    /**
     * Favori konum siler
     */
    suspend fun removeFavoriteLocation(location: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[PreferenceKeys.FAVORITE_LOCATIONS] ?: emptySet()
            preferences[PreferenceKeys.FAVORITE_LOCATIONS] = currentFavorites - location
        }
    }
    
    /**
     * Son seçilen konumu ayarlar
     */
    suspend fun setLastSelectedLocation(city: String, district: String?) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LAST_SELECTED_CITY] = city
            if (district != null) {
                preferences[PreferenceKeys.LAST_SELECTED_DISTRICT] = district
            } else {
                preferences.remove(PreferenceKeys.LAST_SELECTED_DISTRICT)
            }
        }
    }
}
