# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- Fixed timestamp parsing error when backend sends ISO 8601 string format instead of Unix epoch milliseconds
  - Added flexible TimestampAdapter that handles both Long (1640000000000) and String ("2026-01-15T23:15") timestamp formats
  - Prevents `java.lang.NumberFormatException` when backend format doesn't match API specification
- Fixed NullPointerException when backend response structure doesn't match expected format
  - Made `location` and `sources` fields nullable in WeatherData model
  - Added validation in WeatherRepository to check for null fields before returning success
  - Updated UI components to handle null location and sources gracefully
  - Displays clear error message when backend API format is incompatible

## [1.0.0] - 2026-01-14

### Added
- Initial release of Weather App frontend
- Home screen with current weather from multiple sources
  - Expandable accordion cards for each weather source
  - City/district autocomplete search functionality
  - Favorite location management
- Forecast screen with 5-day weather forecast
  - Daily weather summaries
  - Hourly weather forecasts (expandable)
- Favorites screen to manage favorite locations
  - Add/remove favorite locations
  - Quick access to favorite weather
- Settings screen with comprehensive options
  - Language selection (English/Turkish)
  - Temperature unit selection (Celsius/Fahrenheit)
  - Theme selection (Light/Dark/System)
- Complete MVVM architecture implementation
- Dependency Injection with Hilt
- State management with StateFlow
- Repository pattern for data layer
- Retrofit integration for API calls
- DataStore for user preferences
- Bottom navigation for easy screen switching
- Dark and light theme support with system sync
- Full localization support (English and Turkish)
- Comprehensive unit tests for ViewModels
- Basic instrumentation tests for UI components
- Extensive documentation
  - README with setup and usage instructions
  - DEVELOPER documentation with technical details
  - API_SPEC with backend API requirements
  - CONTRIBUTING guidelines for contributors
  - IMPLEMENTATION summary

### Technical Details
- Built with Jetpack Compose
- Kotlin 1.9.20
- Min SDK 24 (Android 7.0)
- Target SDK 34 (Android 14)
- Material Design 3
- MVVM architecture pattern
- Clean Architecture principles
- Kotlin Coroutines and Flow for async operations

### Dependencies
- Compose BOM 2023.10.01
- Hilt 2.48
- Retrofit 2.9.0
- Navigation Compose 2.7.6
- DataStore 1.0.0
- Testing libraries (JUnit, Mockito, Turbine, Espresso)

## [0.1.0] - 2026-01-14

### Added
- Initial project setup
- Basic project structure
- Gradle configuration

---

## Version History

### Version 1.0.0 (2026-01-14)
Complete frontend implementation with all core features, comprehensive documentation, and testing infrastructure.

### Version 0.1.0 (2026-01-14)
Initial project setup and configuration.

---

## Notes

### Breaking Changes
None in this release as it's the initial version.

### Deprecations
None in this release.

### Security Updates
None in this release.

### Known Issues
1. Backend API integration not tested (requires actual backend)
2. Weather icons are placeholders
3. No offline support yet
4. No local data caching

### Migration Guide
Not applicable for initial release.

---

For more details about each version, see the git commit history and pull requests.
