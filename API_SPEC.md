# API Specification - Weather App Backend

Bu belge, Weather App frontend'inin beklediği backend API spesifikasyonlarını içerir.

## Base URL

```
https://api.weatherapp.example.com/api/v1/
```

## Authentication

Şu anda authentication gerekmiyor. Gelecek versiyonlarda API key veya token-based auth eklenebilir.

## Endpoints

### 1. Get Current Weather

Belirtilen konum için güncel hava durumu verilerini getirir.

**Endpoint:** `GET /weather/current`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| city | string | Yes | Şehir adı |
| district | string | No | İlçe adı |

**Request Example:**
```http
GET /weather/current?city=Istanbul&district=Kadikoy
```

**Success Response (200 OK):**
```json
{
  "location": {
    "city": "Istanbul",
    "district": "Kadikoy",
    "country": "Turkey",
    "latitude": 41.0082,
    "longitude": 28.9784
  },
  "sources": [
    {
      "source_name": "OpenWeather",
      "current": {
        "temperature": 20.5,
        "feels_like": 19.0,
        "humidity": 65,
        "wind_speed": 15.0,
        "precipitation": 0.0,
        "pressure": 1013,
        "visibility": 10.0,
        "uv_index": 5,
        "condition": "Clear",
        "icon": "01d"
      }
    },
    {
      "source_name": "WeatherAPI",
      "current": {
        "temperature": 21.0,
        "feels_like": 19.5,
        "humidity": 63,
        "wind_speed": 14.5,
        "precipitation": 0.0,
        "pressure": 1014,
        "visibility": 10.0,
        "uv_index": 5,
        "condition": "Clear",
        "icon": "01d"
      }
    }
  ],
  "timestamp": 1640000000000
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Location not found",
  "message": "The specified location could not be found"
}
```

### 2. Get Weather Forecast

Belirtilen konum için hava durumu tahminini getirir.

**Endpoint:** `GET /weather/forecast`

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| city | string | Yes | - | Şehir adı |
| district | string | No | null | İlçe adı |
| days | integer | No | 5 | Tahmin gün sayısı (1-7) |

**Request Example:**
```http
GET /weather/forecast?city=Istanbul&days=5
```

**Success Response (200 OK):**
```json
{
  "location": {
    "city": "Istanbul",
    "district": null,
    "country": "Turkey",
    "latitude": 41.0082,
    "longitude": 28.9784
  },
  "sources": [
    {
      "source_name": "OpenWeather",
      "current": {
        "temperature": 20.5,
        "feels_like": 19.0,
        "humidity": 65,
        "wind_speed": 15.0,
        "precipitation": 0.0,
        "pressure": 1013,
        "visibility": 10.0,
        "uv_index": 5,
        "condition": "Clear",
        "icon": "01d"
      },
      "forecast": [
        {
          "date": "2024-01-15",
          "day": {
            "max_temp": 22.0,
            "min_temp": 15.0,
            "avg_temp": 18.5,
            "condition": "Partly Cloudy",
            "icon": "02d",
            "precipitation_chance": 20,
            "humidity": 60
          },
          "hourly": [
            {
              "time": "00:00",
              "temperature": 16.0,
              "condition": "Clear",
              "icon": "01n",
              "precipitation_chance": 10
            },
            {
              "time": "03:00",
              "temperature": 15.5,
              "condition": "Clear",
              "icon": "01n",
              "precipitation_chance": 10
            },
            {
              "time": "06:00",
              "temperature": 15.0,
              "condition": "Partly Cloudy",
              "icon": "02d",
              "precipitation_chance": 15
            },
            {
              "time": "09:00",
              "temperature": 17.0,
              "condition": "Partly Cloudy",
              "icon": "02d",
              "precipitation_chance": 15
            },
            {
              "time": "12:00",
              "temperature": 20.0,
              "condition": "Partly Cloudy",
              "icon": "02d",
              "precipitation_chance": 20
            },
            {
              "time": "15:00",
              "temperature": 22.0,
              "condition": "Partly Cloudy",
              "icon": "02d",
              "precipitation_chance": 20
            },
            {
              "time": "18:00",
              "temperature": 19.0,
              "condition": "Cloudy",
              "icon": "03d",
              "precipitation_chance": 25
            },
            {
              "time": "21:00",
              "temperature": 17.0,
              "condition": "Cloudy",
              "icon": "03n",
              "precipitation_chance": 25
            }
          ]
        },
        {
          "date": "2024-01-16",
          "day": {
            "max_temp": 20.0,
            "min_temp": 14.0,
            "avg_temp": 17.0,
            "condition": "Rainy",
            "icon": "10d",
            "precipitation_chance": 70,
            "humidity": 75
          },
          "hourly": []
        }
      ]
    }
  ],
  "timestamp": 1640000000000
}
```

### 3. Search Locations

Konum araması yapar ve eşleşen şehir/ilçeleri döndürür (autocomplete için).

**Endpoint:** `GET /location/search`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| q | string | Yes | Arama sorgusu (minimum 2 karakter) |

**Request Example:**
```http
GET /location/search?q=Anka
```

**Success Response (200 OK):**
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
  },
  {
    "city": "Ankara",
    "district": "Keçiören",
    "country": "Turkey",
    "latitude": 39.9680,
    "longitude": 32.8628
  }
]
```

**Empty Response (200 OK):**
```json
[]
```

## Data Models

### Location
```typescript
{
  city: string,           // Şehir adı
  district?: string,      // İlçe adı (opsiyonel)
  country: string,        // Ülke adı
  latitude: number,       // Enlem
  longitude: number       // Boylam
}
```

### CurrentWeather
```typescript
{
  temperature: number,        // Sıcaklık (Celsius)
  feels_like: number,         // Hissedilen sıcaklık (Celsius)
  humidity: number,           // Nem (%)
  wind_speed: number,         // Rüzgar hızı (km/h)
  precipitation: number,      // Yağış (mm)
  pressure: number,           // Basınç (hPa)
  visibility: number,         // Görüş mesafesi (km)
  uv_index: number,           // UV indeksi
  condition: string,          // Hava durumu açıklaması
  icon?: string              // Hava durumu ikonu kodu (opsiyonel)
}
```

### WeatherSource
```typescript
{
  source_name: string,              // Kaynak adı
  current: CurrentWeather,          // Güncel hava durumu
  forecast?: ForecastDay[]          // Tahmin (opsiyonel)
}
```

### ForecastDay
```typescript
{
  date: string,                     // Tarih (YYYY-MM-DD formatında)
  day: DayWeather,                  // Günlük özet
  hourly?: HourlyWeather[]          // Saatlik tahmin (opsiyonel)
}
```

### DayWeather
```typescript
{
  max_temp: number,                 // Maksimum sıcaklık (Celsius)
  min_temp: number,                 // Minimum sıcaklık (Celsius)
  avg_temp: number,                 // Ortalama sıcaklık (Celsius)
  condition: string,                // Hava durumu açıklaması
  icon?: string,                    // Hava durumu ikonu (opsiyonel)
  precipitation_chance: number,     // Yağış olasılığı (%)
  humidity: number                  // Nem (%)
}
```

### HourlyWeather
```typescript
{
  time: string,                     // Saat (HH:mm formatında)
  temperature: number,              // Sıcaklık (Celsius)
  condition: string,                // Hava durumu açıklaması
  icon?: string,                    // Hava durumu ikonu (opsiyonel)
  precipitation_chance: number      // Yağış olasılığı (%)
}
```

## Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request - Invalid parameters |
| 404 | Not Found - Location not found |
| 500 | Internal Server Error |
| 503 | Service Unavailable - External API down |

## Error Response Format

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "timestamp": 1640000000000
}
```

## Rate Limiting

API rate limit bilgileri response header'larında döndürülür:

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640000000
```

## Weather Condition Codes

Standart hava durumu condition string'leri:

- `Clear` - Açık hava
- `Partly Cloudy` - Parçalı bulutlu
- `Cloudy` - Bulutlu
- `Overcast` - Kapalı
- `Rainy` - Yağmurlu
- `Light Rain` - Hafif yağmur
- `Heavy Rain` - Şiddetli yağmur
- `Snowy` - Karlı
- `Light Snow` - Hafif kar
- `Heavy Snow` - Şiddetli kar
- `Thunderstorm` - Gök gürültülü fırtına
- `Foggy` - Sisli
- `Windy` - Rüzgarlı

## Icon Codes

OpenWeather API icon code standardı kullanılır:

- `01d` / `01n` - Clear sky (gündüz/gece)
- `02d` / `02n` - Few clouds
- `03d` / `03n` - Scattered clouds
- `04d` / `04n` - Broken clouds
- `09d` / `09n` - Shower rain
- `10d` / `10n` - Rain
- `11d` / `11n` - Thunderstorm
- `13d` / `13n` - Snow
- `50d` / `50n` - Mist

## Notes

1. Tüm sıcaklık değerleri Celsius cinsindendir
2. Timestamp değerleri Unix epoch (milliseconds) formatındadır
3. Tarih string'leri ISO 8601 formatındadır (YYYY-MM-DD)
4. Saat string'leri 24-saat formatındadır (HH:mm)
5. Koordinatlar WGS84 datum kullanır

## Testing

Test için örnek curl komutları:

```bash
# Güncel hava durumu
curl "https://api.weatherapp.example.com/api/v1/weather/current?city=Istanbul"

# 5 günlük tahmin
curl "https://api.weatherapp.example.com/api/v1/weather/forecast?city=Istanbul&days=5"

# Konum arama
curl "https://api.weatherapp.example.com/api/v1/location/search?q=Anka"
```

## Implementation Checklist

Backend implementasyonunda dikkat edilmesi gerekenler:

- [ ] CORS ayarları yapılandırılmış
- [ ] Rate limiting implement edilmiş
- [ ] Error handling tutarlı
- [ ] Response time < 2 saniye
- [ ] Logging yapılandırılmış
- [ ] Health check endpoint'i var
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Unit testler yazılmış
- [ ] Integration testler yazılmış
