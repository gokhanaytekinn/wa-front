# Contributing to Weather App

Öncelikle bu projeye katkıda bulunmayı düşündüğünüz için teşekkür ederiz! 🎉

## Katkı Yapmadan Önce

- Proje README'sini ve DEVELOPER.md'yi okuyun
- Mevcut issue'ları kontrol edin
- Code of Conduct'ı okuyun

## Katkı Türleri

### 🐛 Bug Reports

Bug report açarken şunları ekleyin:

1. **Başlık**: Kısa ve açıklayıcı
2. **Açıklama**: Hatanın detaylı açıklaması
3. **Adımlar**: Hatayı yeniden oluşturma adımları
4. **Beklenen Davranış**: Ne olması gerektiği
5. **Gerçekleşen Davranış**: Ne olduğu
6. **Ekran Görüntüleri**: Varsa
7. **Ortam**:
   - Android sürümü
   - Cihaz modeli
   - Uygulama sürümü

**Örnek:**
```markdown
## Bug: Ana ekranda arama çalışmıyor

### Adımlar
1. Uygulamayı aç
2. Ana ekrandaki arama kutusuna "Istanbul" yaz
3. Sonuç görünmüyor

### Beklenen
Arama sonuçları görünmeli

### Gerçekleşen
Hiçbir sonuç görünmüyor

### Ortam
- Android 13
- Samsung Galaxy S21
- App v1.0.0
```

### ✨ Feature Requests

Özellik isteğinde bulunurken:

1. **Başlık**: Özelliğin kısa açıklaması
2. **Problem**: Çözmek istediğiniz problem
3. **Çözüm**: Önerdiğiniz çözüm
4. **Alternatifler**: Düşündüğünüz alternatifler
5. **Ek Bilgi**: Mockup, screenshot vb.

### 🔧 Pull Requests

1. Issue oluşturun veya mevcut bir issue'ya atıfta bulunun
2. Fork yapın ve yeni branch oluşturun
3. Kod yazın ve test edin
4. Commit convention'ına uyun
5. Pull request açın

## Development Setup

### Gereksinimler

- Android Studio Hedgehog veya üstü
- JDK 17+
- Git

### Kurulum

```bash
# 1. Repository'yi fork edin

# 2. Clone yapın
git clone https://github.com/YOUR_USERNAME/wa-front.git
cd wa-front

# 3. Upstream ekleyin
git remote add upstream https://github.com/gokhanaytekinn/wa-front.git

# 4. Branch oluşturun
git checkout -b feature/amazing-feature

# 5. Android Studio'da açın
```

## Branch Naming

Branch isimleri şu formatta olmalı:

- `feature/description` - Yeni özellik
- `fix/description` - Bug fix
- `docs/description` - Documentation
- `refactor/description` - Code refactoring
- `test/description` - Test ekleme/düzeltme

**Örnekler:**
- `feature/add-weather-map`
- `fix/search-crash`
- `docs/api-specification`

## Commit Convention

Conventional Commits standardını kullanıyoruz:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: Yeni özellik
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Kod formatı (logic değişikliği yok)
- `refactor`: Refactoring
- `test`: Test ekleme/düzeltme
- `chore`: Build/tool değişiklikleri

### Örnekler

```bash
feat(home): add weather map integration

- Integrate Google Maps
- Show weather overlay
- Add location picker

Closes #123
```

```bash
fix(forecast): fix crash on null data

When forecast data is null, app was crashing.
Added null check and error handling.

Fixes #456
```

## Code Style

### Kotlin Style Guide

Android'in [Kotlin style guide](https://developer.android.com/kotlin/style-guide)'ını takip ediyoruz.

**Önemli Noktalar:**

```kotlin
// İsimlendirme
class WeatherViewModel // PascalCase
fun loadWeatherData() // camelCase
val weatherData // camelCase
const val BASE_URL // UPPER_SNAKE_CASE

// Fonksiyonlar
fun loadData(
    city: String,
    district: String? = null
) {
    // kod
}

// Data classes
data class Weather(
    val temperature: Double,
    val humidity: Int
)

// Null safety
val city: String? = null
city?.let { /* kullan */ }
```

### Compose Guidelines

```kotlin
// Composable isimlendirme
@Composable
fun WeatherCard() { }

// Preview
@Preview(showBackground = true)
@Composable
fun WeatherCardPreview() {
    WeatherAppTheme {
        WeatherCard()
    }
}

// State hoisting
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) { }
```

### Comments

- Public API'ler için KDoc kullanın
- Türkçe yorum yazın
- Kompleks logic'i açıklayın

```kotlin
/**
 * Hava durumu verilerini yükler
 * 
 * @param city Şehir adı
 * @param district İlçe adı (opsiyonel)
 * @return Flow<Resource<WeatherData>>
 */
fun loadWeather(city: String, district: String?): Flow<Resource<WeatherData>>
```

## Testing

### Test Yazma

Her yeni özellik için test yazın:

```kotlin
// Unit test
@Test
fun `feature should work correctly`() = runTest {
    // Given
    val expected = "result"
    
    // When
    val actual = feature.execute()
    
    // Then
    assertEquals(expected, actual)
}

// UI test
@Test
fun testButtonClick() {
    composeTestRule.setContent {
        MyScreen()
    }
    
    composeTestRule
        .onNodeWithText("Click Me")
        .performClick()
    
    composeTestRule
        .onNodeWithText("Clicked!")
        .assertIsDisplayed()
}
```

### Test Çalıştırma

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# Test coverage
./gradlew jacocoTestReport
```

## Pull Request Process

### 1. Kod Yazma

```bash
# Branch'inizde çalışın
git checkout -b feature/my-feature

# Değişiklik yapın
# Testler ekleyin
# Commit yapın
git commit -m "feat(scope): description"
```

### 2. Sync & Test

```bash
# Upstream'den güncellemeleri çekin
git fetch upstream
git rebase upstream/main

# Testleri çalıştırın
./gradlew test
./gradlew connectedAndroidTest

# Lint kontrolü
./gradlew lint
```

### 3. Push & PR

```bash
# Push yapın
git push origin feature/my-feature

# GitHub'da Pull Request açın
```

### 4. PR Checklist

PR açarken şunları kontrol edin:

- [ ] Branch güncel mi?
- [ ] Testler geçiyor mu?
- [ ] Lint hataları yok mu?
- [ ] Documentation güncel mi?
- [ ] Commit messages düzgün mü?
- [ ] Breaking change var mı?

### 5. PR Template

```markdown
## Description
Kısa açıklama

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Nasıl test edildi?

## Screenshots
(Varsa)

## Checklist
- [ ] Kodları test ettim
- [ ] Documentation güncelledim
- [ ] Commits convention'a uygun

## Related Issues
Closes #123
```

## Code Review

### Review Süreci

1. Maintainer PR'ı inceler
2. Gerekirse değişiklik talep eder
3. Değişiklikler yapılır
4. Onaylanır ve merge edilir

### Review Kriterleri

- Kod style guide'a uygun mu?
- Testler yeterli mi?
- Documentation var mı?
- Performance etkilenmiş mi?
- Security sorunu var mı?

## Release Process

### Versioning

Semantic Versioning kullanıyoruz: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes
- **MINOR**: Yeni özellikler (backward compatible)
- **PATCH**: Bug fixes

### Release Checklist

- [ ] Tüm testler geçiyor
- [ ] Documentation güncel
- [ ] CHANGELOG.md güncellendi
- [ ] Version number artırıldı
- [ ] Git tag oluşturuldu

## Getting Help

### İletişim Kanalları

- **GitHub Issues**: Bug reports, feature requests
- **GitHub Discussions**: Genel sorular, tartışmalar
- **Pull Requests**: Kod review, feedback

### Sorular

Takıldığınız yerde:

1. README ve DEVELOPER.md'yi kontrol edin
2. Existing issues'ları arayın
3. GitHub Discussions'da sorun
4. Yeni issue açın

## Code of Conduct

### Davranış Kuralları

- Saygılı olun
- Yapıcı geri bildirim verin
- Hoşgörülü olun
- Açık fikirli olun

### Kabul Edilmeyen Davranışlar

- Hakaret ve kişisel saldırılar
- Trolling veya inflammatory comments
- Harassment
- Spam

## Recognition

Katkıda bulunanlar README'de ve GitHub Contributors sayfasında listelenecektir.

## License

Katkılarınız MIT lisansı altında lisanslanacaktır.

## Questions?

Herhangi bir sorunuz varsa issue açmaktan çekinmeyin!

Mutlu kodlamalar! 🚀
