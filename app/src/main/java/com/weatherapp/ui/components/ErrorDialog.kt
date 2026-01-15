package com.weatherapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherapp.R
import com.weatherapp.data.model.ApiErrorResponse

/**
 * API hatalarını göstermek için dialog bileşeni
 * Backend'den dönen hata detaylarını (message, status, path) popup içinde gösterir
 */
@Composable
fun ErrorDialog(
    errorResponse: ApiErrorResponse?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(R.string.error_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hata mesajı
                if (errorResponse != null) {
                    // Mesaj
                    ErrorDetailRow(
                        label = stringResource(R.string.error_message),
                        value = errorResponse.message
                    )
                    
                    // Durum kodu
                    ErrorDetailRow(
                        label = stringResource(R.string.error_status),
                        value = errorResponse.status.toString()
                    )
                    
                    // Path (varsa)
                    errorResponse.path?.let { path ->
                        ErrorDetailRow(
                            label = stringResource(R.string.error_path),
                            value = path
                        )
                    }
                } else if (errorMessage != null) {
                    // Basit hata mesajı
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
        modifier = modifier
    )
}

/**
 * Hata detay satırı
 */
@Composable
private fun ErrorDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
