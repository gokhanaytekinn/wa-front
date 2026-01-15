# wa-core Integration Documentation

## Overview

This document describes the integration of the location search endpoint from the `wa-core` backend repository to the `wa-front` Android application.

## Changes Made

### 1. API Version Update

**File**: `app/src/main/java/com/weatherapp/di/NetworkModule.kt`

**Change**: Updated BASE_URL to include `/v1/` API version prefix

```kotlin
// Before
private const val BASE_URL = "http://localhost:8080/api/"

// After
private const val BASE_URL = "http://localhost:8080/api/v1/"
```

**Reason**: The wa-core backend uses versioned API endpoints following the `/api/v1/` pattern. This ensures proper routing to the correct API version.

### 2. Endpoint Alignment

All endpoints now resolve correctly to match wa-core API specification:

- **Current Weather**: `GET http://localhost:8080/api/v1/weather/current`
- **Weather Forecast**: `GET http://localhost:8080/api/v1/weather/forecast`
- **Location Search**: `GET http://localhost:8080/api/v1/location/search`

## Location Search Integration

### API Endpoint

```
GET /api/v1/location/search?q={query}
```

### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| q | string | Yes | Search query (minimum 2 characters) |

### Response Format

The location search endpoint returns a JSON array of location objects:

```json
[
  {
    "city": "Ankara",
    "district": null,
    "country": "Turkey",
    "latitude": 39.9334,
    "longitude": 32.8597
  },
  {
    "city": "Ankara",
    "district": "Çankaya",
    "country": "Turkey",
    "latitude": 39.9180,
    "longitude": 32.8633
  }
]
```

### Data Model

**File**: `app/src/main/java/com/weatherapp/data/model/WeatherModels.kt`

```kotlin
data class LocationSearchResult(
    @SerializedName("city")
    val city: String,
    
    @SerializedName("district")
    val district: String? = null,
    
    @SerializedName("country")
    val country: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)
```

### Service Interface

**File**: `app/src/main/java/com/weatherapp/data/api/WeatherApiService.kt`

```kotlin
@GET("location/search")
suspend fun searchLocations(
    @Query("q") query: String
): Response<List<LocationSearchResult>>
```

### Repository Implementation

**File**: `app/src/main/java/com/weatherapp/data/repository/WeatherRepository.kt`

The repository handles the location search with proper error handling:

```kotlin
fun searchLocations(query: String): Flow<Resource<List<LocationSearchResult>>> = flow {
    if (query.isBlank()) {
        emit(Resource.Success(emptyList()))
        return@flow
    }
    
    emit(Resource.Loading())
    try {
        val response = apiService.searchLocations(query)
        if (response.isSuccessful && response.body() != null) {
            emit(Resource.Success(response.body()!!))
        } else {
            emit(Resource.Error(message = "Konum araması başarısız"))
        }
    } catch (e: Exception) {
        emit(Resource.Error(message = e.localizedMessage ?: "Bilinmeyen bir hata oluştu"))
    }
}
```

### ViewModel Integration

**File**: `app/src/main/java/com/weatherapp/ui/screens/home/HomeViewModel.kt`

The HomeViewModel uses the location search with debouncing (500ms) to avoid excessive API calls:

```kotlin
@OptIn(FlowPreview::class)
private fun setupSearchQueryListener() {
    _searchQuery
        .debounce(500) // 500ms wait before search
        .distinctUntilChanged()
        .onEach { query ->
            if (query.isNotBlank()) {
                searchLocations(query)
            } else {
                _uiState.update { it.copy(searchResults = emptyList()) }
            }
        }
        .launchIn(viewModelScope)
}
```

### UI Implementation

**File**: `app/src/main/java/com/weatherapp/ui/screens/home/HomeScreen.kt`

The UI displays search results in a dropdown with autocomplete functionality:

- Real-time search as user types
- Loading indicator during search
- Clickable location results
- Display format: "District, City" or just "City"

## Compatibility Verification

### Data Model Compatibility ✅

The `LocationSearchResult` data model is fully compatible with wa-core API:

- ✅ All fields match the API specification
- ✅ Proper JSON serialization with `@SerializedName` annotations
- ✅ Nullable district field for city-only results
- ✅ Helper method `getDisplayName()` for UI display

### API Path Compatibility ✅

- ✅ Base URL includes `/v1/` version prefix
- ✅ Endpoint path matches wa-core specification
- ✅ Query parameter name matches (`q`)

### Response Format Compatibility ✅

- ✅ Handles array of location objects
- ✅ Handles empty array for no results
- ✅ Proper error handling for API failures

## Testing Recommendations

### Unit Tests

Test the location search functionality:

```kotlin
@Test
fun `searchLocations should return results for valid query`() = runTest {
    // Given
    val mockResults = listOf(
        LocationSearchResult(
            city = "Ankara",
            district = null,
            country = "Turkey",
            latitude = 39.9334,
            longitude = 32.8597
        )
    )
    val flow = flow {
        emit(Resource.Loading())
        emit(Resource.Success(mockResults))
    }
    whenever(weatherRepository.searchLocations(any())).thenReturn(flow)
    
    // When
    viewModel.updateSearchQuery("Anka")
    advanceUntilIdle()
    
    // Then
    viewModel.uiState.test {
        val state = awaitItem()
        assert(state.searchResults.isNotEmpty())
        assert(state.searchResults[0].city == "Ankara")
    }
}
```

### Integration Tests

To test with actual wa-core backend:

1. Start wa-core backend on `http://localhost:8080`
2. Ensure the `/api/v1/location/search` endpoint is available
3. Run the Android app on emulator
4. Test location search functionality:
   - Type "Anka" in search box
   - Verify autocomplete results appear
   - Click on a location
   - Verify weather data loads for selected location

### Manual Testing Checklist

- [ ] Search with valid city name (e.g., "Istanbul")
- [ ] Search with partial city name (e.g., "Ista")
- [ ] Search with district name (e.g., "Kadıköy")
- [ ] Search with invalid input (e.g., "xyz123")
- [ ] Verify debouncing (results appear after typing stops)
- [ ] Verify loading indicator appears during search
- [ ] Verify empty state when no results
- [ ] Verify error handling for network failures
- [ ] Test with Turkish characters (e.g., "Çankaya", "İzmir")

## Configuration

### Development Environment

For local development with wa-core backend:

```kotlin
private const val BASE_URL = "http://localhost:8080/api/v1/"
```

### Production Environment

For production deployment:

```kotlin
private const val BASE_URL = "https://api.weatherapp.example.com/api/v1/"
```

**Note**: Update the BASE_URL in `NetworkModule.kt` before deploying to production.

## Error Handling

The location search handles the following error scenarios:

1. **Empty Query**: Returns empty list without API call
2. **Network Error**: Shows error message to user
3. **API Error**: Shows error message to user
4. **Invalid Response**: Shows error message to user
5. **No Results**: Shows empty state in UI

## Performance Considerations

### Debouncing

The implementation uses 500ms debouncing to:
- Reduce unnecessary API calls
- Improve app performance
- Reduce backend load
- Provide better user experience

### Caching

Currently, location search results are not cached. Consider adding caching in future versions:
- Cache recent searches
- Cache popular locations
- Use Room database for offline support

## Security Considerations

### HTTPS

The API specification requires HTTPS for production:

```kotlin
// Production
private const val BASE_URL = "https://api.weatherapp.example.com/api/v1/"
```

### Input Validation

The repository validates search queries:
- Rejects blank queries
- Minimum 2 characters recommended in API spec
- Consider adding maximum length validation

## Future Enhancements

1. **Offline Support**: Cache location search results
2. **Recent Searches**: Store and display recent search queries
3. **Popular Locations**: Pre-populate with popular cities
4. **Geolocation**: Auto-detect user's current location
5. **Filtering**: Add country/region filtering options
6. **Sorting**: Sort results by relevance or popularity

## Troubleshooting

### Common Issues

**Issue**: Location search returns empty results
- **Solution**: Verify wa-core backend is running on correct port
- **Solution**: Check network connectivity
- **Solution**: Verify BASE_URL includes `/v1/` path

**Issue**: Search results show incorrect data
- **Solution**: Verify wa-core API response format matches specification
- **Solution**: Check data model serialization annotations

**Issue**: App crashes during location search
- **Solution**: Verify proper error handling in repository
- **Solution**: Check for null safety issues in data model
- **Solution**: Review crash logs for specific error

## References

- [API Specification](API_SPEC.md) - Complete backend API documentation
- [Developer Guide](DEVELOPER.md) - Technical implementation details
- [Build Guide](BUILD_GUIDE.md) - Build and deployment instructions

## Change History

| Date | Version | Changes |
|------|---------|---------|
| 2026-01-15 | 1.0 | Initial integration with wa-core v1 API |
| 2026-01-15 | 1.0 | Updated BASE_URL to include `/v1/` path |
| 2026-01-15 | 1.0 | Verified data model compatibility |
| 2026-01-15 | 1.0 | Documented integration details |

## Contact

For questions or issues related to wa-core integration:
- Open an issue on GitHub
- Review API specification documentation
- Check wa-core repository documentation
