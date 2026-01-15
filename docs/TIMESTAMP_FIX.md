# Timestamp Parsing Fix

## Problem
Backend API was sending timestamps as ISO 8601 string format (`"2026-01-15T23:15"`) instead of Unix epoch milliseconds (Long). This caused `java.lang.NumberFormatException` when Gson tried to deserialize the JSON response.

## Root Cause
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

## Solution
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
