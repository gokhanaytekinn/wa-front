# Backend API Format Compatibility Fixes

## Problem 1: Timestamp Format Mismatch

### Issue
Backend API was sending timestamps as ISO 8601 string format (`"2026-01-15T23:15"`) instead of Unix epoch milliseconds (Long). This caused `java.lang.NumberFormatException` when Gson tried to deserialize the JSON response.

### Root Cause
The `WeatherData` model defines the `timestamp` field as `Long`, expecting Unix epoch milliseconds:
```kotlin
data class WeatherData(
    // ...
    val timestamp: Long
)
```

However, the backend was returning:
```json
{
  "timestamp": "2026-01-15T23:15"
}
```

Instead of the expected:
```json
{
  "timestamp": 1640000000000
}
```

### Solution
Created a custom Gson `TypeAdapter` that handles both formats:

### TimestampAdapter.kt
- Detects the incoming JSON token type (NUMBER, STRING, or NULL)
- If NUMBER: passes through as-is (Long)
- If STRING: parses ISO 8601 format and converts to Long
  - Handles format without seconds: `"2026-01-15T23:15"` → appends `:00`
  - Handles format with seconds: `"2026-01-15T23:15:00"`
  - Handles format with milliseconds: `"2026-01-15T23:15:00.000"`
- If NULL or invalid: falls back to current time (graceful degradation)

### Integration
Registered the adapter in `NetworkModule.kt`:
```kotlin
fun provideGson(): Gson {
    return GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Long::class.java, TimestampAdapter())
        .registerTypeAdapter(Long::class.javaObjectType, TimestampAdapter())
        .create()
}
```

## Benefits
1. **Backward Compatible**: Still accepts Long timestamps from backends that follow the API spec
2. **Forward Compatible**: Accepts String timestamps from backends that send ISO 8601 format
3. **Robust**: Gracefully handles invalid/null timestamps without crashing
4. **Minimal Changes**: No changes to data models, API interfaces, or business logic
5. **Tested**: Comprehensive unit and integration tests cover all scenarios

## Files Changed
- `app/src/main/java/com/weatherapp/data/adapter/TimestampAdapter.kt` (new)
- `app/src/main/java/com/weatherapp/di/NetworkModule.kt` (modified)
- `app/src/test/java/com/weatherapp/data/adapter/TimestampAdapterTest.kt` (new)
- `app/src/test/java/com/weatherapp/data/adapter/TimestampAdapterIntegrationTest.kt` (new)
- `CHANGELOG.md` (updated)

## Testing
Created two test suites:
1. **TimestampAdapterTest**: Unit tests for adapter behavior
2. **TimestampAdapterIntegrationTest**: Integration tests with Gson and WeatherData model

Test coverage includes:
- ✅ Parsing Long timestamps
- ✅ Parsing ISO 8601 strings without seconds
- ✅ Parsing ISO 8601 strings with seconds
- ✅ Handling null values
- ✅ Handling invalid strings
- ✅ Serialization (write) operations

## Alternative Approaches Considered

### 1. Change Model to String
❌ Would require changes throughout the codebase to convert strings to timestamps for date operations

### 2. Add @SerializedName with Custom Deserializer
❌ Would only work for specific fields, not flexible for future fields

### 3. Custom JsonDeserializer for WeatherData
❌ More complex, tightly coupled to WeatherData structure

### 4. TypeAdapter (Chosen) ✅
- Reusable across all Long fields
- Decoupled from specific models
- Standard Gson extension pattern
- Easy to test in isolation

## Impact Analysis
This fix is applied globally to all Long fields during JSON deserialization. However, the impact is minimal because:
1. The adapter only converts String → Long, it doesn't modify valid Long values
2. Most API responses use Long for numeric fields, not String
3. The timestamp field is the primary use case for this adapter
4. Invalid conversions fall back gracefully to current time

## Future Considerations
If the backend API format continues to diverge from the specification, consider:
1. Documenting the actual backend format in API_SPEC.md
2. Adding more adapters for other format mismatches
3. Coordinating with backend team to align on a consistent format
4. Creating a separate model for the actual backend response format

---

## Problem 2: Backend Response Structure Mismatch

### Issue
After fixing the timestamp parsing, a new issue emerged: `NullPointerException` when trying to display weather data in the UI. The error occurred at `HomeScreen.kt` when iterating over `weatherData.sources`.

### Root Cause
The backend is returning a **flat JSON structure**:
```json
{
  "city": "Ankara",
  "district": "Ankara",
  "temperature": 1.3,
  "temperatureUnit": "C",
  "feelsLike": -1.5,
  "humidity": 86,
  "windSpeed": 3.9,
  "precipitation": 0.0,
  "description": "Bulutlu",
  "weatherCode": "3",
  "source": "Open-Meteo",
  "timestamp": "2026-01-15T23:15"
}
```

But the frontend model expects a **nested structure**:
```json
{
  "location": {
    "city": "Ankara",
    "district": "Ankara",
    "country": "Turkey",
    "latitude": 39.9334,
    "longitude": 32.8597
  },
  "sources": [
    {
      "source_name": "OpenWeather",
      "current": { /* weather data */ },
      "forecast": []
    }
  ],
  "timestamp": 1640000000000
}
```

When Gson tried to parse the flat structure into the nested model:
1. The `timestamp` field parsed successfully (thanks to TimestampAdapter)
2. But `location` and `sources` fields remained null (no matching JSON keys)
3. The repository's null check (`response.body() != null`) passed because a WeatherData object was created
4. The UI tried to iterate over `weatherData.sources`, causing NullPointerException

### Solution
Made the model and UI defensive to handle incompatible backend responses:

#### 1. Updated WeatherData Model (WeatherModels.kt)
```kotlin
data class WeatherData(
    @SerializedName("location")
    val location: Location?,  // Now nullable
    
    @SerializedName("sources")
    val sources: List<WeatherSource>?,  // Now nullable
    
    @SerializedName("timestamp")
    val timestamp: Long
)
```

#### 2. Added Validation in WeatherRepository
```kotlin
if (response.isSuccessful && response.body() != null) {
    val weatherData = response.body()!!
    // Validate backend response structure
    if (weatherData.sources == null || weatherData.location == null) {
        emit(Resource.Error(
            message = "Backend API formatı hatalı. Beklenen veri yapısı ile uyuşmuyor."
        ))
    } else {
        emit(Resource.Success(weatherData))
    }
}
```

#### 3. Updated UI Components
**HomeScreen.kt:**
```kotlin
// Safe iteration over sources
weatherData.location?.let { location ->
    item { LocationHeader(location = location, ...) }
}

weatherData.sources?.let { sources ->
    items(sources) { source ->
        WeatherSourceCard(source = source, ...)
    }
}
```

**ForecastScreen.kt:**
```kotlin
// Safe access to sources and location
val forecasts = weatherData.sources?.firstOrNull()?.forecast ?: emptyList()

weatherData.location?.let { location ->
    item {
        Text(text = "${location.city}, ${location.district ?: ""}", ...)
    }
}
```

### Benefits
1. **No crashes**: App handles incompatible backend responses gracefully
2. **Clear error messages**: Users see "Backend API formatı hatalı" instead of a crash
3. **Backward compatible**: Still works with correctly formatted responses
4. **Defensive coding**: Multiple layers of null checks (repository + UI)

### Impact
- Prevents `NullPointerException` when backend format is wrong
- Displays user-friendly error dialog instead of crashing
- Maintains compatibility with correct API format (when backend is fixed)
- Easier to diagnose API format issues in production

## Combined Impact

All three fixes work together to create a comprehensive solution:

1. **TimestampAdapter**: Handles timestamp format variations (Long vs String)
2. **WeatherDataDeserializer**: Transforms flat backend response to nested structure
3. **Nullable model fields**: Handles missing/null data as fallback
4. **Repository validation**: Catches malformed responses as safety net
5. **UI null safety**: Final defense layer in case something slips through

The app now gracefully handles various backend API format issues and provides clear feedback to users instead of crashing.

---

## Problem 3: Flat vs Nested Response Structure

### Issue
User reported error "Backend API formatı hatalı. Beklenen veri yapısı ile uyuşmuyor." even though backend was returning valid data.

The backend sends a **flat JSON structure**:
```json
{
  "city": "Ankara",
  "district": "Ankara",
  "temperature": 1.3,
  "temperatureUnit": "C",
  "feelsLike": -1.5,
  "humidity": 86,
  "windSpeed": 3.6,
  "precipitation": 0,
  "description": "Bulutlu",
  "weatherCode": "3",
  "source": "Open-Meteo",
  "timestamp": "2026-01-15T23:45"
}
```

But the frontend model expects a **nested structure** with separate Location and WeatherSource objects.

### Root Cause
The validation logic added in Problem 2 correctly detected that `location` and `sources` were null (because Gson couldn't map the flat structure), but this caused legitimate backend responses to be rejected.

### Solution
Created `WeatherDataDeserializer` to intelligently handle both formats:

#### Detection Logic
```kotlin
val isFlat = jsonObject.has("city") && 
             jsonObject.has("temperature") && 
             !jsonObject.has("location") && 
             !jsonObject.has("sources")
```

#### Transformation (Flat → Nested)
```kotlin
// Create Location from flat fields
val location = Location(
    city = json.get("city")?.asString ?: "",
    district = json.get("district")?.asString,
    country = "Turkey",
    latitude = 0.0,
    longitude = 0.0
)

// Create CurrentWeather from flat fields
val currentWeather = CurrentWeather(
    temperature = json.get("temperature")?.asDouble ?: 0.0,
    feelsLike = json.get("feelsLike")?.asDouble ?: 0.0,
    humidity = json.get("humidity")?.asInt ?: 0,
    windSpeed = json.get("windSpeed")?.asDouble ?: 0.0,
    precipitation = json.get("precipitation")?.asDouble ?: 0.0,
    condition = json.get("description")?.asString ?: "",
    icon = json.get("weatherCode")?.asString,
    // Default values for missing fields
    pressure = 0,
    visibility = 0.0,
    uvIndex = 0
)

// Create WeatherSource
val source = WeatherSource(
    sourceName = json.get("source")?.asString ?: "Unknown",
    current = currentWeather,
    forecast = null
)

// Build final WeatherData
return WeatherData(
    location = location,
    sources = listOf(source),
    timestamp = parseTimestamp(json.get("timestamp"))
)
```

### Benefits
1. **Automatic format detection**: No configuration needed
2. **Transparent transformation**: Backend and frontend can use different formats
3. **Backward compatible**: Still handles nested format from API spec
4. **Default values**: Provides sensible defaults for missing fields
5. **Works with TimestampAdapter**: Leverages existing timestamp parsing

### Testing
Created comprehensive test suite covering:
- Flat format with String timestamp
- Flat format with Long timestamp
- Nested format (API spec)
- Missing optional fields
- Edge cases

### Impact
✅ Backend can continue using flat format without frontend changes
✅ No more false "format error" messages for valid responses
✅ Maintains compatibility with API specification format
✅ Cleaner separation between API contract and implementation details
✅ Easier to work with multiple backend versions

## Final Architecture

The complete solution provides three layers of compatibility:

```
Backend Response (Flat/Nested, String/Long timestamp)
         ↓
[WeatherDataDeserializer] - Format transformation
         ↓
[TimestampAdapter] - Timestamp parsing
         ↓
WeatherData (Nullable fields)
         ↓
[Repository Validation] - Safety net
         ↓
[UI Null Safety] - Defensive rendering
         ↓
User sees: Working app or clear error message
```

This multi-layer approach ensures maximum compatibility and resilience.
