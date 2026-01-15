package com.weatherapp.util

import com.weatherapp.data.model.ApiErrorResponse

/**
 * API çağrılarının sonucunu temsil eden sealed class
 * Başarılı, hata ve yükleniyor durumlarını yönetir
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val errorResponse: ApiErrorResponse? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(
        message: String, 
        data: T? = null,
        errorResponse: ApiErrorResponse? = null
    ) : Resource<T>(data, message, errorResponse)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
