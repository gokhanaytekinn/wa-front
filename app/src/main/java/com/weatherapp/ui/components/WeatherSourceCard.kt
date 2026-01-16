package com.weatherapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherapp.R
import com.weatherapp.data.model.WeatherSource
import com.weatherapp.data.model.CurrentWeather
import kotlin.math.roundToInt

/**
 * Hava durumu kaynağı kartı - Accordion tarzında genişletilebilir
 * Her hava durumu servisinin verilerini gösterir
 */
@Composable
fun WeatherSourceCard(
    source: WeatherSource,
    temperatureUnit: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // Bugünün tahminini bul (varsa)
    val todayForecast = source.forecast?.firstOrNull()
    
    Card(
        modifier = modifier.clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Başlık satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.sourceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTemperature(source.current.temperature, temperatureUnit),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = source.current.condition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Genişletilmiş detaylar
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    WeatherDetailGrid(
                        weather = source.current,
                        temperatureUnit = temperatureUnit,
                        todayForecast = todayForecast
                    )
                }
            }
        }
    }
}

/**
 * Hava durumu detay ızgarası
 * Hissedilen sıcaklık, min-max sıcaklık, nem, rüzgar hızı ve yağış bilgilerini gösterir
 */
@Composable
fun WeatherDetailGrid(
    weather: CurrentWeather,
    temperatureUnit: String,
    modifier: Modifier = Modifier,
    todayForecast: com.weatherapp.data.model.ForecastDay? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hissedilen sıcaklık ve nem
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailItem(
                icon = Icons.Default.Thermostat,
                label = stringResource(R.string.feels_like),
                value = formatTemperature(weather.feelsLike, temperatureUnit),
                modifier = Modifier.weight(1f)
            )
            WeatherDetailItem(
                icon = Icons.Default.Water,
                label = stringResource(R.string.humidity),
                value = if (weather.humidity > 0) "${weather.humidity}%" else stringResource(R.string.data_not_available),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Min - Max sıcaklık (bugünün tahmini varsa)
        todayForecast?.let { forecast ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.ArrowDownward,
                    label = stringResource(R.string.min_temp),
                    value = formatTemperature(forecast.day.minTemp, temperatureUnit),
                    modifier = Modifier.weight(1f)
                )
                WeatherDetailItem(
                    icon = Icons.Default.ArrowUpward,
                    label = stringResource(R.string.max_temp),
                    value = formatTemperature(forecast.day.maxTemp, temperatureUnit),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Rüzgar hızı ve yağış
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailItem(
                icon = Icons.Default.Air,
                label = stringResource(R.string.wind_speed),
                value = if (weather.windSpeed > 0) "${weather.windSpeed} ${stringResource(R.string.wind_speed_unit_metric)}" else stringResource(R.string.data_not_available),
                modifier = Modifier.weight(1f)
            )
            WeatherDetailItem(
                icon = Icons.Default.Cloud,
                label = stringResource(R.string.precipitation),
                value = if (weather.precipitation > 0) "${weather.precipitation} ${stringResource(R.string.precipitation_unit)}" else stringResource(R.string.data_not_available),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Hava durumu detay öğesi
 */
@Composable
fun WeatherDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Sıcaklığı formatlar (Celsius veya Fahrenheit)
 */
fun formatTemperature(celsius: Double, unit: String): String {
    return if (unit == "fahrenheit") {
        val fahrenheit = (celsius * 9/5) + 32
        "${fahrenheit.roundToInt()}°F"
    } else {
        "${celsius.roundToInt()}°C"
    }
}
