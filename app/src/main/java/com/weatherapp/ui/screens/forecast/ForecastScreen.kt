package com.weatherapp.ui.screens.forecast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weatherapp.R
import com.weatherapp.data.model.ForecastDay
import com.weatherapp.data.model.HourlyWeather
import com.weatherapp.ui.components.ErrorDialog
import com.weatherapp.ui.components.formatTemperature
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Tahmin ekranı
 * 5 günlük hava durumu tahminini gösterir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.five_day_forecast)) }
            )
        }
    ) { paddingValues ->
        val forecastData = uiState.forecastData
        val hasValidData = forecastData != null && (!forecastData.forecasts.isNullOrEmpty() || !forecastData.sources.isNullOrEmpty())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Arama kutusu
            ForecastSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                searchResults = uiState.searchResults,
                onLocationSelected = { viewModel.selectLocation(it) },
                isSearching = uiState.isSearching,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Ana içerik
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    hasValidData -> {
                        ForecastContent(
                            forecastData = forecastData!!,
                            temperatureUnit = uiState.temperatureUnit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        EmptyStateView(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                
                // Hata dialogu - error olduğunda göster
                if (uiState.error != null) {
                    ErrorDialog(
                        errorResponse = uiState.errorResponse,
                        errorMessage = uiState.error,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}

/**
 * Tahmin içeriği
 */
@Composable
fun ForecastContent(
    forecastData: com.weatherapp.data.model.ForecastResponse,
    temperatureUnit: String,
    modifier: Modifier = Modifier
) {
    // Öncelikle sources'dan ilk kaynağı, yoksa direkt forecasts'ı kullan
    val forecasts = forecastData.sources?.firstOrNull()?.forecasts 
        ?: forecastData.forecasts 
        ?: emptyList()
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Konum bilgisi
        item {
            Text(
                text = buildString {
                    append(forecastData.city)
                    forecastData.district?.let { append(", $it") }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Her gün için kart
        items(forecasts) { forecast ->
            SimpleForecastCard(
                forecast = forecast,
                temperatureUnit = temperatureUnit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Basit günlük tahmin kartı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleForecastCard(
    forecast: com.weatherapp.data.model.SimpleForecast,
    temperatureUnit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Gün özeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatDate(forecast.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = forecast.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${stringResource(R.string.high)}: ${formatTemperature(forecast.maxTemperature, temperatureUnit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${stringResource(R.string.low)}: ${formatTemperature(forecast.minTemperature, temperatureUnit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "${stringResource(R.string.precipitation)}: ${forecast.precipitationChance}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Günlük tahmin kartı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayForecastCard(
    forecastDay: ForecastDay,
    temperatureUnit: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Gün özeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatDate(forecastDay.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = forecastDay.day.condition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${stringResource(R.string.high)}: ${formatTemperature(forecastDay.day.maxTemp, temperatureUnit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${stringResource(R.string.low)}: ${formatTemperature(forecastDay.day.minTemp, temperatureUnit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "${stringResource(R.string.precipitation)}: ${forecastDay.day.precipitationChance}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Saatlik tahminler (varsa)
            val hourlyForecasts = forecastDay.hourly
            if (isExpanded && !hourlyForecasts.isNullOrEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                Text(
                    text = stringResource(R.string.hourly_forecast),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(hourlyForecasts) { hourly ->
                        HourlyForecastItem(
                            hourly = hourly,
                            temperatureUnit = temperatureUnit
                        )
                    }
                }
            }
        }
    }
}

/**
 * Saatlik tahmin öğesi
 */
@Composable
fun HourlyForecastItem(
    hourly: HourlyWeather,
    temperatureUnit: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .width(80.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formatTime(hourly.time),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = getWeatherIcon(hourly.condition),
                contentDescription = hourly.condition,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = formatTemperature(hourly.temperature, temperatureUnit),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${hourly.precipitationChance}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Tarihi formatlar
 */
fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val dayOfWeek = date.dayOfWeek.toString()
        val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
        "${dayOfWeek.substring(0, 3)} ${date.format(formatter)}"
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Saati formatlar
 */
fun formatTime(timeString: String): String {
    return try {
        // "HH:mm" formatını kullan
        timeString.substring(0, 5)
    } catch (e: Exception) {
        timeString
    }
}

/**
 * Hava durumu durumuna göre ikon döndürür
 */
fun getWeatherIcon(condition: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        condition.contains("clear", ignoreCase = true) -> Icons.Default.WbSunny
        condition.contains("cloud", ignoreCase = true) -> Icons.Default.Cloud
        condition.contains("rain", ignoreCase = true) -> Icons.Default.Umbrella
        condition.contains("snow", ignoreCase = true) -> Icons.Default.AcUnit
        condition.contains("storm", ignoreCase = true) -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
    }
}

/**
 * Hata görünümü
 */
@Composable
fun ErrorView(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * Boş durum görünümü
 */
@Composable
fun EmptyStateView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.search_location),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Arama kutusu composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<com.weatherapp.data.model.LocationSearchResult>,
    onLocationSelected: (com.weatherapp.data.model.LocationSearchResult) -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_location)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
        )
        
        // Arama sonuçları
        androidx.compose.animation.AnimatedVisibility(
            visible = searchResults.isNotEmpty() || isSearching,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(searchResults) { result ->
                            ListItem(
                                headlineContent = { Text(result.getDisplayName()) },
                                supportingContent = { Text(result.country) },
                                leadingContent = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                },
                                modifier = Modifier.clickable {
                                    onLocationSelected(result)
                                }
                            )
                            if (result != searchResults.last()) {
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}
