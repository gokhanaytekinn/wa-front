package com.weatherapp.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weatherapp.R
import com.weatherapp.data.model.WeatherSource
import com.weatherapp.ui.components.ErrorDialog
import com.weatherapp.ui.components.WeatherSourceCard
import com.weatherapp.ui.components.formatTemperature

// Constants
private const val CARD_BACKGROUND_ALPHA = 0.5f

/**
 * Ana sayfa composable
 * Güncel hava durumu bilgilerini farklı kaynaklardan gösterir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.current_weather)) },
                actions = {
                    IconButton(onClick = { 
                        uiState.selectedCity?.let { city ->
                            viewModel.loadWeatherData(city, uiState.selectedDistrict)
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.add_favorite)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Arama kutusu
            SearchBar(
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
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.weatherData != null -> {
                    WeatherContent(
                        weatherData = uiState.weatherData!!,
                        temperatureUnit = uiState.temperatureUnit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    EmptyStateView(
                        modifier = Modifier.fillMaxSize()
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

/**
 * Arama kutusu composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
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
            shape = RoundedCornerShape(28.dp)
        )
        
        // Arama sonuçları
        AnimatedVisibility(
            visible = searchResults.isNotEmpty() || isSearching,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
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

/**
 * Hava durumu içeriği
 */
@Composable
fun WeatherContent(
    weatherData: com.weatherapp.data.model.WeatherData,
    temperatureUnit: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Konum bilgisi
        weatherData.location?.let { location ->
            item {
                LocationHeader(
                    location = location,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Ortalama hava durumu kartı
        weatherData.sources?.let { sources ->
            if (sources.isNotEmpty()) {
                item {
                    AverageWeatherCard(
                        sources = sources,
                        temperatureUnit = temperatureUnit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Her kaynak için kart
        weatherData.sources?.let { sources ->
            items(sources) { source ->
                WeatherSourceCard(
                    source = source,
                    temperatureUnit = temperatureUnit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Konum başlığı
 */
@Composable
fun LocationHeader(
    location: com.weatherapp.data.model.Location,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = if (location.district != null) "${location.district}, ${location.city}" else location.city,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = location.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
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
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
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
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.search_location),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Ortalama hava durumu kartı
 * Tüm kaynaklardan gelen verilerin ortalamasını gösterir
 */
@Composable
fun AverageWeatherCard(
    sources: List<WeatherSource>,
    temperatureUnit: String,
    modifier: Modifier = Modifier
) {
    // Ortalamaları tek bir iterasyonda hesapla
    val averages = remember(sources) {
        if (sources.isEmpty()) {
            AverageWeatherData(0.0, 0.0, 0, 0.0, 0.0)
        } else {
            val count = sources.size.toDouble()
            var sumTemp = 0.0
            var sumFeels = 0.0
            var sumHumidity = 0
            var sumWind = 0.0
            var sumPrecip = 0.0
            
            sources.forEach { source ->
                sumTemp += source.current.temperature
                sumFeels += source.current.feelsLike
                sumHumidity += source.current.humidity
                sumWind += source.current.windSpeed
                sumPrecip += source.current.precipitation
            }
            
            AverageWeatherData(
                temperature = sumTemp / count,
                feelsLike = sumFeels / count,
                humidity = (sumHumidity / count).toInt(),
                windSpeed = sumWind / count,
                precipitation = sumPrecip / count
            )
        }
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Başlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.average_weather),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = stringResource(R.string.all_sources),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = formatTemperature(averages.temperature, temperatureUnit),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detay ızgarası
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AverageDetailItem(
                    icon = Icons.Default.Thermostat,
                    label = stringResource(R.string.feels_like),
                    value = formatTemperature(averages.feelsLike, temperatureUnit),
                    modifier = Modifier.weight(1f)
                )
                AverageDetailItem(
                    icon = Icons.Default.Water,
                    label = stringResource(R.string.humidity),
                    value = "${averages.humidity}%",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AverageDetailItem(
                    icon = Icons.Default.Air,
                    label = stringResource(R.string.wind_speed),
                    value = String.format("%.1f %s", averages.windSpeed, stringResource(R.string.wind_speed_unit_metric)),
                    modifier = Modifier.weight(1f)
                )
                AverageDetailItem(
                    icon = Icons.Default.Cloud,
                    label = stringResource(R.string.precipitation),
                    value = String.format("%.1f %s", averages.precipitation, stringResource(R.string.precipitation_unit)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Ortalama hava durumu verisi için data class
 */
private data class AverageWeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val precipitation: Double
)

/**
 * Ortalama hava durumu detay öğesi
 */
@Composable
fun AverageDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = CARD_BACKGROUND_ALPHA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
