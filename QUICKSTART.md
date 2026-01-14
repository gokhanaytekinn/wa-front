# Quick Start Guide - Weather App

Get the Weather App up and running in minutes!

## Prerequisites

Before you begin, ensure you have:

- ✅ Android Studio Hedgehog (2023.1.1) or later
- ✅ JDK 17 or later installed
- ✅ Android SDK 34
- ✅ Git

## Step 1: Clone the Repository

```bash
git clone https://github.com/gokhanaytekinn/wa-front.git
cd wa-front
```

## Step 2: Open in Android Studio

1. Launch Android Studio
2. Select **"Open an Existing Project"**
3. Navigate to the `wa-front` folder
4. Click **"OK"**
5. Wait for Gradle sync to complete (this may take a few minutes)

## Step 3: Configure Backend API

⚠️ **Important**: The app needs a backend API to function.

Edit `app/src/main/java/com/weatherapp/di/NetworkModule.kt`:

```kotlin
private const val BASE_URL = "https://your-backend-api.com/api/v1/"
```

Replace with your actual backend API URL.

### Don't have a backend yet?

You can:
1. Create a mock backend (see `API_SPEC.md` for requirements)
2. Use a mock interceptor for testing
3. Set up a simple Node.js/Express backend

## Step 4: Run the App

### Option A: Using Android Studio

1. Connect an Android device or start an emulator
2. Click the green **"Run"** button (▶️)
3. Select your device
4. Wait for the app to install and launch

### Option B: Using Command Line

```bash
# Connect device or start emulator first
./gradlew installDebug
```

## Step 5: Explore the App

### Home Screen
- Search for a city (e.g., "Istanbul")
- View weather from multiple sources
- Tap cards to expand and see details
- Add to favorites with the heart icon

### Forecast Screen
- View 5-day weather forecast
- Tap days to see hourly forecasts

### Favorites Screen
- Manage your favorite locations
- Quick access to saved cities

### Settings Screen
- Change language (English/Turkish)
- Toggle temperature unit (°C/°F)
- Switch theme (Light/Dark/System)

## Common Issues

### Build Errors

**Problem**: Gradle sync fails
```bash
# Solution
./gradlew clean
./gradlew build --refresh-dependencies
```

**Problem**: SDK not found
```
# Solution: Update local.properties
sdk.dir=/path/to/your/Android/sdk
```

### Runtime Errors

**Problem**: App crashes on start
- Check if BASE_URL is configured
- Verify Android version (min API 24)
- Check Logcat for error messages

**Problem**: No data loads
- Verify backend API is running
- Check network connection
- Review API endpoint responses

## Testing Without Backend

To test the UI without a backend:

1. Mock the WeatherRepository
2. Return fake data from the repository
3. See `DEVELOPER.md` for mock implementation guide

Example:
```kotlin
// In Hilt module, replace real repository with mock
@Provides
fun provideWeatherRepository(): WeatherRepository {
    return MockWeatherRepository() // Returns fake data
}
```

## Next Steps

### For Users
- Configure your backend API
- Customize the app (colors, strings)
- Build and test all features

### For Developers
- Read `DEVELOPER.md` for architecture details
- Check `API_SPEC.md` for backend requirements
- See `CONTRIBUTING.md` to contribute

### For Backend Developers
- Implement the 3 required endpoints:
  1. `GET /weather/current`
  2. `GET /weather/forecast`
  3. `GET /location/search`
- Follow the specification in `API_SPEC.md`
- Test with the Android app

## Quick Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumentation tests (device needed)
./gradlew connectedAndroidTest

# Check code style
./gradlew lint

# Clean build
./gradlew clean build
```

## Project Structure Overview

```
wa-front/
├── app/src/main/java/com/weatherapp/
│   ├── data/           # Data layer (API, models, repositories)
│   ├── di/             # Dependency injection
│   ├── ui/             # UI layer (screens, components)
│   └── util/           # Utilities
├── docs/               # Documentation
└── build.gradle.kts    # Build configuration
```

## Configuration Files

### Important Files to Check

1. **NetworkModule.kt** - API URL configuration
2. **AndroidManifest.xml** - App permissions and config
3. **build.gradle.kts** - Dependencies and build config
4. **strings.xml** - Localizable strings

## Environment Setup

### Recommended Android Studio Plugins

- Kotlin
- Android
- Compose Multiplatform IDE Support

### Recommended Settings

```
Settings → Editor → Code Style → Kotlin
✓ Use default Kotlin style guide
```

## Troubleshooting

### Issue: "Unresolved reference: R"
```bash
# Solution
Build → Clean Project
Build → Rebuild Project
```

### Issue: Compose preview not working
```
File → Invalidate Caches / Restart
```

### Issue: Hilt errors
- Check @HiltAndroidApp in Application class
- Verify kapt is applied in build.gradle
- Sync Gradle files

## Development Workflow

```bash
# 1. Create feature branch
git checkout -b feature/my-feature

# 2. Make changes
# 3. Test locally
./gradlew test

# 4. Commit changes
git commit -m "feat: add my feature"

# 5. Push and create PR
git push origin feature/my-feature
```

## Resources

- 📖 [README.md](README.md) - Full documentation
- 🔧 [DEVELOPER.md](DEVELOPER.md) - Technical guide
- 🌐 [API_SPEC.md](API_SPEC.md) - API specification
- 🤝 [CONTRIBUTING.md](CONTRIBUTING.md) - How to contribute

## Support

Need help?

1. Check documentation files
2. Search existing issues on GitHub
3. Create a new issue with details
4. Ask in GitHub Discussions

## What's Next?

After setup:

1. ✅ App is running
2. 📱 Test all screens
3. 🔌 Connect to backend
4. 🧪 Run tests
5. 🎨 Customize if needed
6. 🚀 Deploy!

---

**Time to Complete**: ~15-30 minutes (depending on download speeds)

**Difficulty**: Beginner-friendly

**Support**: Open an issue if you need help!

Happy coding! 🎉
