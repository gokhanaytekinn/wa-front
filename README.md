# Weather App - Android Frontend

Modern bir hava durumu uygulaması. Jetpack Compose ve Kotlin kullanılarak geliştirilmiştir.

## Özellikler

### 🏠 Ana Sayfa (Güncel Hava Durumu)
- Farklı kaynaklardan gelen hava durumu verilerini accordion kartlar halinde gösterir
- Her kart genişletilebilir ve detaylı hava durumu bilgilerini içerir:
  - Sıcaklık (Celsius/Fahrenheit)
  - Hissedilen sıcaklık
  - Nem oranı
  - Rüzgar hızı
  - Yağış miktarı
  - Basınç
  - Görüş mesafesi
  - UV indeksi
- Şehir/ilçe otomatik tamamlama arama özelliği
- Seçilen konuma göre dinamik veri güncellemesi
- Favori konumlara ekleme/çıkarma

### 📅 Tahmin Ekranı
- 5 günlük hava durumu tahmini
- Her gün için detaylı bilgiler:
  - Maksimum ve minimum sıcaklık
  - Hava durumu koşulu
  - Yağış olasılığı
  - Nem oranı
- Saatlik hava durumu tahminleri (genişletilebilir kartlar)

### ⭐ Favoriler Ekranı
- Favori şehir ve ilçelerin listesi
- Kolay erişim için favori konumları yönetme
- Favorilerden hızlı hava durumu kontrolü

### ⚙️ Ayarlar Ekranı
- **Dil Seçimi**: İngilizce ve Türkçe desteği
- **Sıcaklık Birimi**: Celsius veya Fahrenheit seçimi
- **Tema Seçimi**:
  - Açık Tema
  - Koyu Tema (varsayılan)
  - Sistem Teması (sistem ayarlarına göre otomatik)

### 🎨 UI/UX Özellikleri
- Modern Material Design 3 tasarımı
- Responsive ve estetik arayüz
- Alt navigasyon çubuğu ile kolay gezinme
- Smooth animasyonlar ve geçişler
- Koyu ve açık tema desteği

### 🏗️ Mimari ve Teknolojiler
- **MVVM Mimari Deseni**
- **Jetpack Compose** - Modern UI toolkit
- **Kotlin Coroutines & Flow** - Asenkron programlama
- **Hilt** - Dependency Injection
- **Retrofit** - API iletişimi
- **DataStore** - Kullanıcı tercihleri için local storage
- **Navigation Component** - Ekranlar arası navigasyon
- **Material 3** - Google'ın en yeni design system'i

## Kurulum

### Gereksinimler
- Android Studio Hedgehog (2023.1.1) veya üstü
- JDK 17 veya üstü
- Android SDK 34
- Minimum Android API Level: 24 (Android 7.0)
- Target Android API Level: 34 (Android 14)

### Projeyi Çalıştırma

1. **Repository'yi klonlayın:**
```bash
git clone https://github.com/gokhanaytekinn/wa-front.git
cd wa-front
```

2. **Android Studio'da açın:**
   - Android Studio'yu açın
   - "Open an Existing Project" seçeneğini seçin
   - Klonladığınız projeyi seçin

3. **Gradle senkronizasyonu:**
   - Android Studio otomatik olarak Gradle senkronizasyonunu başlatacaktır
   - İndirme işleminin tamamlanmasını bekleyin

4. **Backend API URL'ini yapılandırın:**
   - `app/src/main/java/com/weatherapp/di/NetworkModule.kt` dosyasını açın
   - `BASE_URL` değişkenini kendi backend API adresinizle değiştirin:
   ```kotlin
   private const val BASE_URL = "https://your-backend-api.com/api/v1/"
   ```

5. **Uygulamayı çalıştırın:**
   - Bir Android cihaz bağlayın veya emulator başlatın
   - Android Studio'da Run (▶) butonuna tıklayın
   - Veya terminal'den: `./gradlew installDebug`

### Backend API Gereksinimleri

Uygulama aşağıdaki endpoint'leri beklemektedir:

#### 1. Güncel Hava Durumu
```
GET /weather/current?city={city}&district={district}
```

Yanıt formatı:
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
    }
  ],
  "timestamp": 1640000000000
}
```

#### 2. Hava Durumu Tahmini
```
GET /weather/forecast?city={city}&district={district}&days=5
```

Yanıt formatı:
```json
{
  "location": { ... },
  "sources": [
    {
      "source_name": "OpenWeather",
      "current": { ... },
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
            }
          ]
        }
      ]
    }
  ],
  "timestamp": 1640000000000
}
```

#### 3. Konum Arama
```
GET /location/search?q={query}
```

Yanıt formatı:
```json
[
  {
    "city": "Istanbul",
    "district": "Kadikoy",
    "country": "Turkey",
    "latitude": 41.0082,
    "longitude": 28.9784
  }
]
```

## Test Etme

### Unit Testler
```bash
./gradlew test
```

### Instrumentation Testler
```bash
./gradlew connectedAndroidTest
```

### Test Kapsamı
- ViewModel testleri
- Repository testleri
- UI component testleri

## Build İşlemleri

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

APK dosyası şurada oluşturulur: `app/build/outputs/apk/`

## Proje Yapısı

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/weatherapp/
│   │   │   ├── data/
│   │   │   │   ├── api/          # Retrofit API servisleri
│   │   │   │   ├── model/        # Data modelleri
│   │   │   │   └── repository/   # Repository sınıfları
│   │   │   ├── di/               # Dependency Injection modülleri
│   │   │   ├── ui/
│   │   │   │   ├── components/   # Reusable UI bileşenleri
│   │   │   │   ├── navigation/   # Navigation yapısı
│   │   │   │   ├── screens/      # Ekran composable'ları
│   │   │   │   │   ├── home/
│   │   │   │   │   ├── forecast/
│   │   │   │   │   ├── favorites/
│   │   │   │   │   └── settings/
│   │   │   │   └── theme/        # Tema ve stil tanımları
│   │   │   ├── util/             # Utility sınıfları
│   │   │   ├── MainActivity.kt
│   │   │   └── WeatherApplication.kt
│   │   └── res/
│   │       ├── values/           # String kaynakları (EN)
│   │       ├── values-tr/        # String kaynakları (TR)
│   │       └── ...
│   ├── test/                     # Unit testler
│   └── androidTest/              # Instrumentation testler
└── build.gradle.kts
```

## Kod Standartları

- Tüm kod Türkçe yorumlarla dokümante edilmiştir
- MVVM mimari prensiplerine uygun
- Single Responsibility Principle
- Dependency Injection kullanımı
- Clean Code prensipleri

## Katkıda Bulunma

1. Fork edin
2. Feature branch oluşturun (`git checkout -b feature/AmazingFeature`)
3. Değişikliklerinizi commit edin (`git commit -m 'feat: Add some AmazingFeature'`)
4. Branch'inizi push edin (`git push origin feature/AmazingFeature`)
5. Pull Request açın

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## İletişim

Proje Sahibi - [@gokhanaytekinn](https://github.com/gokhanaytekinn)

Proje Linki: [https://github.com/gokhanaytekinn/wa-front](https://github.com/gokhanaytekinn/wa-front)

## Ekran Görüntüleri

_Not: Ekran görüntüleri projenin ilk derlemesinden sonra eklenecektir._

## Bilinen Sorunlar ve Çözümler

### Backend API Bağlantısı
Eğer backend API'niz henüz hazır değilse, mock data ile test edebilirsiniz:
1. NetworkModule.kt içindeki BASE_URL'yi değiştirin
2. Veya mock WeatherRepository implementasyonu oluşturun

### Build Hataları
Eğer build sırasında hata alırsanız:
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

## Sık Sorulan Sorular

**S: Uygulama offline çalışıyor mu?**  
C: Şu anda hayır, ancak gelecek versiyonlarda cache mekanizması eklenecektir.

**S: Başka hava durumu API'leri eklenebilir mi?**  
C: Evet, WeatherApiService'e yeni endpoint'ler ekleyerek kolayca genişletilebilir.

**S: Tema tercihlerini nasıl değiştirebilirim?**  
C: Ayarlar ekranından tema seçeneğini değiştirebilirsiniz. Tercihler otomatik kaydedilir.