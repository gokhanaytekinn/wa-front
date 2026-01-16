package com.weatherapp.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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

/**
 * Ayarlar ekranı
 * Dil, sıcaklık birimi ve tema ayarlarını içerir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isVisible: Boolean = true,
    onThemeChange: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    // Ekran görünür olduğunda scroll'u en üste getir
    LaunchedEffect(isVisible) {
        if (isVisible) {
            listState.scrollToItem(0)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Genel Ayarlar Başlığı
            item {
                Text(
                    text = stringResource(R.string.general_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Dil Ayarı
            item {
                LanguageSetting(
                    currentLanguage = uiState.language,
                    onLanguageChange = { viewModel.setLanguage(it) }
                )
            }
            
            // Görünüm Ayarları Başlığı
            item {
                Text(
                    text = stringResource(R.string.display_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            // Sıcaklık Birimi Ayarı
            item {
                TemperatureUnitSetting(
                    currentUnit = uiState.temperatureUnit,
                    onUnitChange = { viewModel.setTemperatureUnit(it) }
                )
            }
            
            // Tema Ayarı
            item {
                ThemeSetting(
                    currentTheme = uiState.theme,
                    onThemeChange = { 
                        viewModel.setTheme(it)
                        onThemeChange(it)
                    }
                )
            }
        }
    }
}

/**
 * Dil ayarı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSetting(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { showDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = stringResource(R.string.language),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (currentLanguage == "tr") stringResource(R.string.language_turkish) 
                               else stringResource(R.string.language_english),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    RadioButtonOption(
                        text = stringResource(R.string.language_english),
                        selected = currentLanguage == "en",
                        onClick = {
                            onLanguageChange("en")
                            showDialog = false
                        }
                    )
                    RadioButtonOption(
                        text = stringResource(R.string.language_turkish),
                        selected = currentLanguage == "tr",
                        onClick = {
                            onLanguageChange("tr")
                            showDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Sıcaklık birimi ayarı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureUnitSetting(
    currentUnit: String,
    onUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { showDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = stringResource(R.string.temperature_unit),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (currentUnit == "celsius") stringResource(R.string.celsius) 
                               else stringResource(R.string.fahrenheit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.temperature_unit)) },
            text = {
                Column {
                    RadioButtonOption(
                        text = stringResource(R.string.celsius),
                        selected = currentUnit == "celsius",
                        onClick = {
                            onUnitChange("celsius")
                            showDialog = false
                        }
                    )
                    RadioButtonOption(
                        text = stringResource(R.string.fahrenheit),
                        selected = currentUnit == "fahrenheit",
                        onClick = {
                            onUnitChange("fahrenheit")
                            showDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Tema ayarı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSetting(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { showDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = stringResource(R.string.theme),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when (currentTheme) {
                            "light" -> stringResource(R.string.theme_light)
                            "dark" -> stringResource(R.string.theme_dark)
                            else -> stringResource(R.string.theme_system)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.theme)) },
            text = {
                Column {
                    RadioButtonOption(
                        text = stringResource(R.string.theme_light),
                        selected = currentTheme == "light",
                        onClick = {
                            onThemeChange("light")
                            showDialog = false
                        }
                    )
                    RadioButtonOption(
                        text = stringResource(R.string.theme_dark),
                        selected = currentTheme == "dark",
                        onClick = {
                            onThemeChange("dark")
                            showDialog = false
                        }
                    )
                    RadioButtonOption(
                        text = stringResource(R.string.theme_system),
                        selected = currentTheme == "system",
                        onClick = {
                            onThemeChange("system")
                            showDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Radio button seçeneği
 */
@Composable
fun RadioButtonOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
