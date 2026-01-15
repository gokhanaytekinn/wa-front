# Build Verification Guide

This document provides step-by-step instructions for building and verifying the Weather App project.

## Prerequisites

Before building the project, ensure you have:

1. **Android Studio Hedgehog (2023.1.1)** or later installed
2. **JDK 17** or later
3. **Android SDK 34** installed via SDK Manager
4. **Git** for version control

## Initial Setup

### 1. Configure Android SDK

Create a `local.properties` file in the project root:

```bash
cp local.properties.template local.properties
```

Edit `local.properties` and set your SDK path:

```properties
sdk.dir=/path/to/your/Android/sdk
```

**Finding your SDK path:**
- **macOS/Linux**: Usually `~/Library/Android/sdk` or `~/Android/Sdk`
- **Windows**: Usually `C:\Users\YourUsername\AppData\Local\Android\Sdk`
- **From Android Studio**: File → Project Structure → SDK Location

### 2. Configure Backend API URL

Edit `app/src/main/java/com/weatherapp/di/NetworkModule.kt`:

```kotlin
private const val BASE_URL = "https://your-backend-api.com/api/v1/"
```

Replace with your actual backend API URL. The URL must include the `/api/v1/` path to match the wa-core API specification.

## Building the Project

### Option 1: Using Android Studio (Recommended)

1. Open Android Studio
2. Select **"Open an Existing Project"**
3. Navigate to the project directory
4. Click **"OK"**
5. Wait for Gradle sync to complete
6. Click **Build → Make Project** or press `Cmd+F9` (Mac) / `Ctrl+F9` (Windows/Linux)

### Option 2: Using Command Line

#### On macOS/Linux:

```bash
# Make gradlew executable (if not already)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean and build
./gradlew clean assembleDebug
```

#### On Windows:

```cmd
# Build debug APK
gradlew.bat assembleDebug

# Build release APK
gradlew.bat assembleRelease

# Clean and build
gradlew.bat clean assembleDebug
```

## Running Tests

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run tests for a specific module
./gradlew :app:test

# Run tests with coverage
./gradlew jacocoTestReport
```

### Instrumentation Tests

**Note:** Requires a connected Android device or running emulator.

```bash
# Run all instrumentation tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.weatherapp.ui.NavigationTest
```

## Code Quality Checks

### Lint Check

```bash
# Run lint checks
./gradlew lint

# View lint report
open app/build/reports/lint-results.html
```

### Code Style Check

```bash
# Check Kotlin code style (if ktlint is configured)
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat
```

## Build Outputs

After a successful build, find your APK files at:

- **Debug**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release**: `app/build/outputs/apk/release/app-release.apk`

## Common Build Issues

### Issue 1: SDK Not Found

**Error:**
```
SDK location not found. Define location with an ANDROID_SDK_ROOT environment variable or by setting the sdk.dir path in your project's local properties file
```

**Solution:**
Create `local.properties` file with correct SDK path (see Initial Setup above).

### Issue 2: Gradle Sync Failed

**Error:**
```
Could not resolve all dependencies
```

**Solution:**
```bash
# Clear Gradle cache
./gradlew clean --refresh-dependencies

# Or in Android Studio: File → Invalidate Caches / Restart
```

### Issue 3: Java Version Mismatch

**Error:**
```
Unsupported Java. Your build is currently configured to use Java XX
```

**Solution:**
- Ensure JDK 17 is installed
- Set JAVA_HOME environment variable
- In Android Studio: File → Project Structure → SDK Location → JDK Location

### Issue 4: Build Tools Missing

**Error:**
```
Failed to find Build Tools revision XX.X.X
```

**Solution:**
- Open Android Studio
- Tools → SDK Manager
- SDK Tools tab → Install required Build Tools version

### Issue 5: Kotlin Compiler Error

**Error:**
```
Kotlin: Unresolved reference
```

**Solution:**
```bash
# Clean and rebuild
./gradlew clean build

# Or in Android Studio: Build → Clean Project, then Build → Rebuild Project
```

## Verification Checklist

Before submitting code, verify:

- [ ] Project builds without errors: `./gradlew assembleDebug`
- [ ] Unit tests pass: `./gradlew test`
- [ ] Lint checks pass: `./gradlew lint`
- [ ] Code is properly formatted
- [ ] All new code has Turkish comments
- [ ] Documentation is updated
- [ ] No hardcoded sensitive data

## Running the App

### On Emulator

1. In Android Studio: Tools → Device Manager
2. Create a new device (recommended: Pixel 6 with API 34)
3. Start the emulator
4. Click **Run** (▶️) button
5. Select your emulator

### On Physical Device

1. Enable Developer Options on your device:
   - Settings → About Phone → Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging
3. Connect device via USB
4. Click **Run** (▶️) button
5. Select your device

## Build Variants

The project has two build variants:

### Debug
- Debugging enabled
- Logging enabled
- No code obfuscation
- Faster build times
- Suitable for development

```bash
./gradlew assembleDebug
```

### Release
- Optimized for production
- ProGuard/R8 enabled
- Code obfuscation
- Smaller APK size
- Suitable for distribution

```bash
./gradlew assembleRelease
```

**Note:** Release builds require signing configuration.

## Gradle Tasks

Useful Gradle tasks:

```bash
# List all available tasks
./gradlew tasks

# List all dependencies
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates

# Generate documentation
./gradlew dokkaHtml

# Build bundle (for Play Store)
./gradlew bundleRelease
```

## Performance Testing

### Build Speed

```bash
# Profile build performance
./gradlew assembleDebug --profile

# View profile report
open build/reports/profile/
```

### APK Size Analysis

```bash
# Build APK
./gradlew assembleRelease

# Analyze with Android Studio
# Build → Analyze APK → Select APK file
```

## Continuous Integration

For CI/CD setup, use these commands:

```bash
# Clean build
./gradlew clean

# Assemble
./gradlew assembleDebug assembleRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint

# Generate reports
./gradlew jacocoTestReport
```

## Troubleshooting Commands

```bash
# Stop Gradle daemon
./gradlew --stop

# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Clear build directory
./gradlew clean

# Refresh dependencies
./gradlew build --refresh-dependencies

# Run with stacktrace
./gradlew assembleDebug --stacktrace

# Run with debug info
./gradlew assembleDebug --debug
```

## Environment Variables

Optional environment variables:

```bash
# Android SDK location
export ANDROID_SDK_ROOT=/path/to/android/sdk

# Java home
export JAVA_HOME=/path/to/jdk17

# Gradle options
export GRADLE_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
```

## Next Steps

After successful build:

1. Test the app on multiple devices
2. Configure backend API integration
3. Run end-to-end tests with real data
4. Prepare for Play Store submission

## Support

If you encounter issues:

1. Check this guide for common solutions
2. Review `README.md` for setup instructions
3. Check `DEVELOPER.md` for technical details
4. Open an issue on GitHub with:
   - Error message
   - Build output
   - Android Studio version
   - OS version
   - Steps to reproduce

## Resources

- [Android Developer Guide](https://developer.android.com/guide)
- [Gradle Build Tool](https://gradle.org/guides/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
