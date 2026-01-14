# Developer Documentation - Weather App

## Proje Hakkında

Bu belge, Weather App projesinin teknik detaylarını ve geliştirici kılavuzunu içermektedir.

## Mimari

### MVVM (Model-View-ViewModel) Pattern

Proje, modern Android geliştirme best practice'lerine uygun olarak MVVM mimarisi kullanmaktadır:

```
┌─────────────┐
│    View     │ (Jetpack Compose UI)
│  (Screen)   │
└──────┬──────┘
       │ observes StateFlow
       │
┌──────▼──────┐
│  ViewModel  │ (Business Logic & State Management)
└──────┬──────┘
       │ uses
       │
┌──────▼──────┐
│ Repository  │ (Data Layer Abstraction)
└──────┬──────┘
       │ uses
       │
┌──────▼──────┐     ┌──────────────┐
│ API Service │ ────│  DataStore   │
│  (Retrofit) │     │ (Preferences)│
└─────────────┘     └──────────────┘
```

### Paket Yapısı

```
com.weatherapp
├── data
│   ├── api           # Retrofit API interface'leri
│   ├── model         # Data transfer objects (DTO)
│   └── repository    # Repository implementasyonları
├── di                # Hilt Dependency Injection modülleri
├── ui
│   ├── components    # Yeniden kullanılabilir UI bileşenleri
│   ├── navigation    # Navigation yapısı
│   ├── screens       # Her ekran için package
│   │   ├── home
│   │   ├── forecast
│   │   ├── favorites
│   │   └── settings
│   └── theme         # Tema, renkler, tipografi
└── util              # Yardımcı sınıflar ve uzantılar
```

## Teknolojiler ve Kütüphaneler

### Core Dependencies

#### Jetpack Compose
- **Versiyon**: BOM 2023.10.01
- **Kullanım**: Declarative UI toolkit
- **Neden**: Modern, reactive UI geliştirme

#### Kotlin Coroutines & Flow
- **Versiyon**: 1.7.3
- **Kullanım**: Asenkron işlemler ve reactive data streams
- **Neden**: Type-safe, structured concurrency

#### Hilt
- **Versiyon**: 2.48
- **Kullanım**: Dependency Injection
- **Neden**: Android için optimize edilmiş DI framework

#### Retrofit
- **Versiyon**: 2.9.0
- **Kullanım**: REST API iletişimi
- **Neden**: Type-safe HTTP client

#### DataStore
- **Versiyon**: 1.0.0
- **Kullanım**: Kullanıcı tercihleri storage
- **Neden**: SharedPreferences'ın modern alternatifi

### Test Dependencies

- **JUnit 4**: Unit test framework
- **Mockito**: Mocking library
- **Turbine**: Flow testing utilities
- **Espresso**: UI testing

## Veri Akışı

### 1. API'den Veri Çekme

```kotlin
// 1. ViewModel API çağrısı başlatır
viewModel.loadWeatherData("Istanbul", null)

// 2. Repository Flow döndürür
weatherRepository.getCurrentWeather(city, district)
    .collect { resource ->
        when (resource) {
            is Resource.Loading -> { /* Loading state */ }
            is Resource.Success -> { /* Update UI state */ }
            is Resource.Error -> { /* Show error */ }
        }
    }

// 3. API Service çağrısı yapılır
apiService.getCurrentWeather(city, district)

// 4. Response parse edilir ve UI'a iletilir
```

### 2. State Management

```kotlin
// ViewModel içinde StateFlow kullanımı
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

// UI state güncelleme
_uiState.update { it.copy(isLoading = true) }

// Compose'da state observe etme
val uiState by viewModel.uiState.collectAsState()
```

## Ekran Yapıları

### Home Screen (Ana Sayfa)

**Sorumluluklar:**
- Güncel hava durumu gösterimi
- Farklı kaynaklardan veri toplama
- Konum arama ve otomatik tamamlama
- Favori ekleme/çıkarma

**Key Components:**
- `HomeViewModel`: State management
- `HomeScreen`: Ana composable
- `WeatherSourceCard`: Accordion card component
- `SearchBar`: Arama bileşeni

### Forecast Screen (Tahmin)

**Sorumluluklar:**
- 5 günlük tahmin gösterimi
- Günlük ve saatlik detaylar
- Expandable forecast cards

**Key Components:**
- `ForecastViewModel`: Tahmin state yönetimi
- `ForecastScreen`: Tahmin listesi
- `DayForecastCard`: Günlük tahmin kartı
- `HourlyForecastItem`: Saatlik tahmin öğesi

### Favorites Screen (Favoriler)

**Sorumluluklar:**
- Favori konumları listeleme
- Favori ekleme/silme
- Hızlı erişim

**Key Components:**
- `FavoritesViewModel`: Favori yönetimi
- `FavoritesScreen`: Favori listesi
- `FavoriteLocationCard`: Favori kartı

### Settings Screen (Ayarlar)

**Sorumluluklar:**
- Uygulama ayarları
- Dil değiştirme
- Tema değiştirme
- Birim değiştirme

**Key Components:**
- `SettingsViewModel`: Ayar yönetimi
- `SettingsScreen`: Ayar listesi
- `LanguageSetting`, `ThemeSetting`, etc.

## Tema Sistemi

### Color Scheme

Uygulama Material Design 3 color system kullanır:

**Light Theme:**
- Primary: #1976D2 (Blue)
- Secondary: #0288D1 (Light Blue)
- Background: #FAFAFA
- Surface: #FFFFFF

**Dark Theme:**
- Primary: #64B5F6 (Light Blue)
- Secondary: #4FC3F7 (Cyan)
- Background: #121212
- Surface: #1E1E1E

### Dynamic Theming

```kotlin
// Tema değişimi
when (themeState) {
    "light" -> isDarkTheme = false
    "dark" -> isDarkTheme = true
    "system" -> isDarkTheme = isSystemInDarkTheme()
}
```

## Testing

### Unit Tests

Unit testler `app/src/test` dizininde bulunur.

**Örnek ViewModel Test:**
```kotlin
@Test
fun `loadWeatherData should update ui state with success`() = runTest {
    // Given
    val mockData = createMockWeatherData()
    whenever(repository.getCurrentWeather(any())).thenReturn(flowOf(Resource.Success(mockData)))
    
    // When
    viewModel.loadWeatherData("Istanbul", null)
    advanceUntilIdle()
    
    // Then
    viewModel.uiState.test {
        val state = awaitItem()
        assert(state.weatherData != null)
    }
}
```

### Instrumentation Tests

UI testleri `app/src/androidTest` dizininde bulunur.

```bash
# Tüm unit testleri çalıştır
./gradlew test

# Tüm instrumentation testleri çalıştır
./gradlew connectedAndroidTest
```

## Backend API Integration

### API Endpoints

1. **Güncel Hava Durumu**
   ```
   GET /weather/current?city={city}&district={district}
   ```

2. **Tahmin**
   ```
   GET /weather/forecast?city={city}&district={district}&days=5
   ```

3. **Konum Arama**
   ```
   GET /location/search?q={query}
   ```

### API URL Değiştirme

`NetworkModule.kt` dosyasında BASE_URL'yi değiştirin:

```kotlin
private const val BASE_URL = "https://your-api.com/api/v1/"
```

### Mock Data ile Test

Geliştirme aşamasında mock data kullanmak için:

1. `WeatherRepository` interface'i oluşturun
2. `MockWeatherRepository` implementasyonu yapın
3. Hilt modülünde binding değiştirin

## Localization (Yerelleştirme)

### Dil Desteği

- İngilizce (default): `res/values/strings.xml`
- Türkçe: `res/values-tr/strings.xml`

### Yeni Dil Ekleme

1. `res/values-{lang}/strings.xml` oluştur
2. Tüm string resource'ları çevir
3. `SettingsViewModel` ve `PreferencesRepository`'ye dil seçeneği ekle

### Kullanım

```kotlin
// Compose'da
Text(text = stringResource(R.string.app_name))

// ViewModel'da (context gerektirir)
context.getString(R.string.app_name)
```

## Performance Optimization

### Image Loading
Şu anda hava durumu ikonları için placeholder kullanılıyor. Gerçek implementasyonda:
- Coil veya Glide kullanın
- Image caching yapın
- Placeholder gösterin

### Database Caching
Gelecek versiyonlarda Room database eklenebilir:
- Offline support
- Data caching
- Favorite locations storage

### Memory Management
- ViewModels lifecycle-aware
- StateFlow memory-efficient
- Proper coroutine scope yönetimi

## Build Variants

### Debug Build
```bash
./gradlew assembleDebug
```
- Logging enabled
- Debug symbols included
- ProGuard disabled

### Release Build
```bash
./gradlew assembleRelease
```
- Logging disabled
- Code obfuscation (ProGuard)
- Optimized

## Common Issues & Solutions

### 1. Build Hatası: "Unresolved reference"
**Çözüm:**
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### 2. Hilt Dependency Injection Hatası
**Çözüm:**
- `@HiltAndroidApp` annotation'ın Application class'ında olduğunu kontrol et
- `kapt` plugin'inin eklendiğini kontrol et

### 3. Compose Preview Çalışmıyor
**Çözüm:**
- Android Studio'yu güncelleyin
- Invalidate Caches / Restart yapın

## Code Style

### Naming Conventions

- **Classes**: PascalCase (e.g., `WeatherViewModel`)
- **Functions**: camelCase (e.g., `loadWeatherData`)
- **Variables**: camelCase (e.g., `weatherData`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `BASE_URL`)

### Kotlin Conventions

```kotlin
// İyi ✅
val weatherData: WeatherData? = null
data class Location(val city: String)

// Kötü ❌
var weather_data: WeatherData? = null
data class location(val City: String)
```

### Comments

Tüm public fonksiyonlar ve sınıflar Türkçe KDoc ile dokümante edilmelidir:

```kotlin
/**
 * Hava durumu verilerini yükler
 * @param city Şehir adı
 * @param district İlçe adı (opsiyonel)
 */
fun loadWeatherData(city: String, district: String? = null)
```

## Future Improvements

### Planlanan Özellikler
- [ ] Offline support (Room database)
- [ ] Widget support
- [ ] Push notifications
- [ ] Multiple location comparison
- [ ] Weather maps integration
- [ ] Historical data view
- [ ] Weather alerts

### Teknik İyileştirmeler
- [ ] Pagination for forecast
- [ ] Image caching
- [ ] Better error handling
- [ ] Analytics integration
- [ ] Crashlytics integration
- [ ] CI/CD pipeline

## Contributing

### Development Workflow

1. Feature branch oluştur
2. Kod yaz ve test et
3. Commit message convention'ına uy:
   - `feat:` yeni özellik
   - `fix:` bug fix
   - `docs:` documentation
   - `style:` formatting
   - `refactor:` code refactoring
   - `test:` test ekleme/düzeltme
4. Pull request aç
5. Code review bekle

### Pre-commit Checklist

- [ ] Kod derlenebiliyor mu?
- [ ] Testler başarılı mı?
- [ ] Lint hataları var mı?
- [ ] Yeni kod dokümante edilmiş mi?
- [ ] Breaking change var mı?

## Resources

### Official Documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Hilt](https://dagger.dev/hilt/)
- [Material Design 3](https://m3.material.io/)

### Community
- [Stack Overflow](https://stackoverflow.com/questions/tagged/android)
- [Kotlin Slack](https://kotlinlang.slack.com/)
- [Android Developers](https://www.youtube.com/c/AndroidDevelopers)

## Contact

Teknik sorular için GitHub Issues kullanın veya proje sahibiyle iletişime geçin.
