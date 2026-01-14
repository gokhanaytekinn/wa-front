# Deployment Guide - Weather App

Complete guide for deploying the Weather App to production and Google Play Store.

## Pre-Deployment Checklist

Before deploying to production, ensure:

- [ ] Backend API is deployed and accessible
- [ ] API endpoints tested and working
- [ ] All features tested on multiple devices
- [ ] Unit tests passing
- [ ] UI tests passing
- [ ] Lint checks passing
- [ ] No hardcoded API keys or secrets
- [ ] ProGuard/R8 rules configured
- [ ] App icons and splash screen created
- [ ] Privacy policy created
- [ ] Terms of service created

## Environment Configuration

### 1. Production API Configuration

Update `app/src/main/java/com/weatherapp/di/NetworkModule.kt`:

```kotlin
private const val BASE_URL = "https://api.weatherapp.production.com/api/v1/"
```

### 2. Build Variants

Create different configurations for staging and production.

Edit `app/build.gradle.kts`:

```kotlin
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
            matchingFallbacks += listOf("release")
        }
    }
}
```

## Signing Configuration

### 1. Generate Signing Key

```bash
# Generate keystore
keytool -genkey -v -keystore weather-app-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias weather-app-key

# You'll be prompted for:
# - Keystore password
# - Key password
# - Your name, organization, etc.
```

**Important:** Store the keystore file and passwords securely!

### 2. Configure Signing

Create `keystore.properties` in project root (add to .gitignore):

```properties
storeFile=/path/to/weather-app-release.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=weather-app-key
keyPassword=YOUR_KEY_PASSWORD
```

Update `app/build.gradle.kts`:

```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
}
```

## Build Release APK

### Command Line

```bash
# Build signed release APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Android Studio

1. Build → Generate Signed Bundle / APK
2. Select **APK**
3. Choose existing keystore or create new
4. Select **release** build variant
5. Choose both V1 and V2 signature versions
6. Click **Finish**

## Build App Bundle (Recommended)

Google Play Store prefers App Bundles for smaller downloads:

```bash
# Build signed app bundle
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

## Testing Release Build

### Install on Device

```bash
# Install release APK
adb install app/build/outputs/apk/release/app-release.apk

# Or force reinstall
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Test Checklist

- [ ] App installs without errors
- [ ] All screens load correctly
- [ ] Weather data loads from production API
- [ ] Search functionality works
- [ ] Favorites persist across app restarts
- [ ] Theme changes work
- [ ] Language switching works
- [ ] Temperature unit toggle works
- [ ] App doesn't crash on rotation
- [ ] App works offline gracefully
- [ ] No memory leaks (test with LeakCanary)
- [ ] Performance is smooth

## Google Play Store Submission

### 1. Create Google Play Console Account

1. Go to [Google Play Console](https://play.google.com/console)
2. Sign in with Google account
3. Pay one-time $25 registration fee
4. Complete account setup

### 2. Create App Listing

#### Store Listing

**App Details:**
- App name: "Weather App"
- Short description: (80 characters)
  ```
  Modern weather app with multi-source data, forecasts, and customizable themes
  ```
- Full description: (4000 characters)
  ```
  Weather App provides comprehensive weather information from multiple sources.
  
  Features:
  • Current weather from multiple services
  • 5-day weather forecast with hourly details
  • Save favorite locations
  • Multi-language support (English, Turkish)
  • Dark and light themes
  • Temperature in Celsius or Fahrenheit
  
  Get accurate weather updates for your location!
  ```

**Graphics:**
- App icon: 512 x 512 px (PNG)
- Feature graphic: 1024 x 500 px (PNG/JPEG)
- Phone screenshots: At least 2 (max 8)
  - Portrait: 1080 x 1920 px
  - Landscape: 1920 x 1080 px
- 7-inch tablet screenshots: Optional
- 10-inch tablet screenshots: Optional

**Categorization:**
- App category: Weather
- Content rating: Everyone
- Contact details: Your email
- Privacy policy: URL to your policy

### 3. Content Rating

1. Complete questionnaire
2. Answer questions about:
   - Violence
   - Sexual content
   - Profanity
   - Controlled substances
   - User interaction
3. Generate ratings

### 4. App Content

**Privacy Policy:**
Create a privacy policy that covers:
- What data you collect
- How you use the data
- Third-party services
- Data retention
- User rights
- Contact information

Host on a publicly accessible URL.

**Data Safety:**
Declare:
- Location data: Not collected
- Personal info: Not collected
- Photos and videos: Not collected
- Files and docs: Not collected
- Calendar: Not collected
- Contacts: Not collected
- App activity: Not collected
- Web browsing: Not collected
- App info and performance: Collected (crash logs)
- Device or other IDs: Not collected

### 5. Upload Release

1. Go to **Production** → **Create new release**
2. Upload app bundle (.aab file)
3. Add release name: "1.0.0"
4. Add release notes:
   ```
   Initial release of Weather App
   
   Features:
   • Multi-source weather display
   • 5-day forecast
   • Favorite locations
   • Multiple languages (EN, TR)
   • Dark and light themes
   • Temperature unit toggle
   ```
5. Review and rollout

### 6. Pricing & Distribution

- Free or Paid: Free
- Countries: Select all or specific
- Content rating: Apply rating
- Consent for ads: No (if no ads)

### 7. Review & Publish

1. Review all sections (green checkmarks)
2. Click **Submit for review**
3. Wait for Google's review (1-7 days)
4. Address any issues if rejected
5. App goes live after approval

## Post-Launch

### 1. Monitor Metrics

Track in Play Console:
- Installs and uninstalls
- Ratings and reviews
- Crashes and ANRs
- User acquisition
- Revenue (if applicable)

### 2. Crash Reporting

Integrate Firebase Crashlytics:

```kotlin
// Add to app/build.gradle.kts
dependencies {
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.6.1")
}
```

### 3. Analytics

Integrate Firebase Analytics:

```kotlin
dependencies {
    implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
}
```

### 4. Respond to Reviews

- Monitor user reviews daily
- Respond to negative reviews
- Thank users for positive feedback
- Address common issues in updates

### 5. Regular Updates

Schedule regular updates:
- Bug fixes: As needed
- Minor features: Monthly
- Major features: Quarterly

## Version Management

### Semantic Versioning

Follow semver (X.Y.Z):
- X: Major version (breaking changes)
- Y: Minor version (new features)
- Z: Patch version (bug fixes)

### Update Version

Edit `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2  // Increment for each release
    versionName = "1.0.1"
}
```

## Rollout Strategy

### 1. Internal Testing

1. Upload to **Internal testing** track
2. Add internal testers
3. Test thoroughly
4. Fix issues

### 2. Closed Testing

1. Create closed testing release
2. Invite beta testers (via email list)
3. Gather feedback
4. Iterate on issues

### 3. Open Testing

1. Create open testing release
2. Anyone can join and test
3. Get broader feedback
4. Polish based on feedback

### 4. Production

1. Gradual rollout:
   - Day 1: 10% of users
   - Day 2: 25% of users
   - Day 3: 50% of users
   - Day 4: 100% of users
2. Monitor crashes and ratings
3. Halt rollout if issues found
4. Fix and restart

## CI/CD Setup

### GitHub Actions

Create `.github/workflows/android.yml`:

```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'adopt'
        
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Build with Gradle
      run: ./gradlew assembleDebug
      
    - name: Run tests
      run: ./gradlew test
      
    - name: Run lint
      run: ./gradlew lint
```

## Backup & Recovery

### Backup Checklist

- [ ] Keystore file (.jks)
- [ ] Keystore passwords (secure location)
- [ ] Google Play Console credentials
- [ ] Source code (Git repository)
- [ ] Build configurations
- [ ] API keys and secrets
- [ ] Database backups (if applicable)

### Recovery Plan

If keystore is lost:
1. Create new keystore
2. Upload as new app (can't update existing)
3. Users must reinstall

**Prevention:** Store keystore securely!

## Support & Maintenance

### User Support

1. Create support email: support@weatherapp.com
2. Respond within 24-48 hours
3. Track common issues
4. Update FAQ

### Monitoring Tools

- Google Play Console
- Firebase Crashlytics
- Firebase Analytics
- Sentry (optional)
- New Relic (optional)

## Legal Requirements

### Required Documents

1. **Privacy Policy**
   - Data collection practices
   - Third-party services
   - User rights
   - Contact information

2. **Terms of Service**
   - Acceptable use
   - Liability limitations
   - Termination conditions
   - Governing law

3. **Licenses**
   - Open source licenses (if used)
   - Third-party attributions

## Checklist for First Release

- [ ] App thoroughly tested
- [ ] Backend API ready
- [ ] Keystore generated and backed up
- [ ] Release APK/AAB built and tested
- [ ] Screenshots taken (all required sizes)
- [ ] App icon finalized (512x512)
- [ ] Feature graphic created (1024x500)
- [ ] Privacy policy published
- [ ] Terms of service published
- [ ] Play Console account set up
- [ ] Store listing completed
- [ ] Content rating obtained
- [ ] App content declared
- [ ] Pricing set (free)
- [ ] Countries selected
- [ ] Release notes written
- [ ] Submitted for review

## Additional Resources

- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [Android Publishing Guide](https://developer.android.com/studio/publish)
- [Material Design Guidelines](https://material.io/design)
- [App Bundle Format](https://developer.android.com/guide/app-bundle)

## Conclusion

Follow this guide step-by-step for successful deployment. Take your time with each section and test thoroughly before submission.

Good luck with your launch! 🚀
