# Testing Guide - Weather App

Comprehensive testing guide for the Weather App project.

## Table of Contents

1. [Testing Strategy](#testing-strategy)
2. [Unit Testing](#unit-testing)
3. [Integration Testing](#integration-testing)
4. [UI Testing](#ui-testing)
5. [Manual Testing](#manual-testing)
6. [Performance Testing](#performance-testing)
7. [Accessibility Testing](#accessibility-testing)

## Testing Strategy

### Testing Pyramid

```
       /\
      /  \    E2E Tests (Few)
     /____\
    /      \  Integration Tests (Some)
   /________\
  /          \ Unit Tests (Many)
 /____________\
```

**Ratio:** 70% Unit, 20% Integration, 10% E2E/UI

### Coverage Goals

- **Unit Tests**: 80% code coverage
- **Integration Tests**: Critical paths covered
- **UI Tests**: Main user flows covered

## Unit Testing

### Running Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew :app:testDebugUnitTest

# Run with coverage
./gradlew jacocoTestReport

# View coverage report
open app/build/reports/jacoco/test/html/index.html
```

### Writing Unit Tests

#### ViewModel Tests

Example: `HomeViewModelTest.kt`

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        weatherRepository = mock()
        preferencesRepository = mock()
        
        // Setup default mocks
        whenever(preferencesRepository.lastSelectedCity).thenReturn(flowOf(null))
        whenever(preferencesRepository.temperatureUnit).thenReturn(flowOf("celsius"))
        
        viewModel = HomeViewModel(weatherRepository, preferencesRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadWeatherData should emit loading then success`() = runTest {
        // Given
        val mockData = createMockWeatherData()
        whenever(weatherRepository.getCurrentWeather(any(), any()))
            .thenReturn(flowOf(Resource.Success(mockData)))
        
        // When
        viewModel.loadWeatherData("Istanbul", null)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.weatherData != null)
            assertFalse(state.isLoading)
        }
    }
    
    @Test
    fun `loadWeatherData should emit error on failure`() = runTest {
        // Given
        val errorMsg = "Network error"
        whenever(weatherRepository.getCurrentWeather(any(), any()))
            .thenReturn(flowOf(Resource.Error(errorMsg)))
        
        // When
        viewModel.loadWeatherData("Istanbul", null)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMsg, state.error)
        }
    }
}
```

#### Repository Tests

Example: `WeatherRepositoryTest.kt`

```kotlin
class WeatherRepositoryTest {
    
    private lateinit var apiService: WeatherApiService
    private lateinit var repository: WeatherRepository
    
    @Before
    fun setup() {
        apiService = mock()
        repository = WeatherRepository(apiService)
    }
    
    @Test
    fun `getCurrentWeather returns success`() = runTest {
        // Given
        val mockResponse = Response.success(createMockWeatherData())
        whenever(apiService.getCurrentWeather(any(), any()))
            .thenReturn(mockResponse)
        
        // When
        val result = repository.getCurrentWeather("Istanbul", null)
        
        // Then
        result.test {
            assertTrue(awaitItem() is Resource.Loading)
            assertTrue(awaitItem() is Resource.Success)
            awaitComplete()
        }
    }
}
```

### Test Coverage

Check coverage:

```bash
./gradlew jacocoTestReport
```

View report at: `app/build/reports/jacoco/test/html/index.html`

### Mocking Guidelines

**Use Mockito for:**
- Repository dependencies
- API services
- DataStore/SharedPreferences

**Example:**
```kotlin
val mockRepository = mock<WeatherRepository>()
whenever(mockRepository.getCurrentWeather(any())).thenReturn(flow {
    emit(Resource.Success(mockData))
})
```

## Integration Testing

### API Integration Tests

Test actual API calls (requires test server):

```kotlin
@Test
fun `API returns valid weather data`() = runTest {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://test-api.weatherapp.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService = retrofit.create(WeatherApiService::class.java)
    val response = apiService.getCurrentWeather("Istanbul", null)
    
    assertTrue(response.isSuccessful)
    assertNotNull(response.body())
}
```

### Database Integration Tests

If using Room database:

```kotlin
@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {
    
    private lateinit var database: WeatherDatabase
    private lateinit var dao: FavoriteDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.favoriteDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveFavorite() = runTest {
        // Given
        val favorite = Favorite("Istanbul", "Kadikoy")
        
        // When
        dao.insert(favorite)
        val favorites = dao.getAllFavorites().first()
        
        // Then
        assertTrue(favorites.contains(favorite))
    }
}
```

## UI Testing

### Running UI Tests

**Prerequisites:**
- Connected device or running emulator
- USB debugging enabled

```bash
# Run all UI tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.weatherapp.ui.HomeScreenTest
```

### Compose UI Tests

Example: `HomeScreenTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun homeScreen_displaysSearchBar() {
        composeTestRule.setContent {
            WeatherAppTheme {
                HomeScreen()
            }
        }
        
        composeTestRule
            .onNodeWithText(getString(R.string.search_location))
            .assertIsDisplayed()
    }
    
    @Test
    fun searchBar_acceptsInput() {
        composeTestRule.setContent {
            WeatherAppTheme {
                HomeScreen()
            }
        }
        
        composeTestRule
            .onNodeWithText(getString(R.string.search_location))
            .performTextInput("Istanbul")
        
        composeTestRule
            .onNodeWithText("Istanbul")
            .assertIsDisplayed()
    }
    
    @Test
    fun weatherCard_canBeExpanded() {
        // Setup mock data
        val mockViewModel = mockViewModel()
        
        composeTestRule.setContent {
            WeatherAppTheme {
                HomeScreen(viewModel = mockViewModel)
            }
        }
        
        // Click to expand
        composeTestRule
            .onNodeWithText("OpenWeather")
            .performClick()
        
        // Verify expanded content
        composeTestRule
            .onNodeWithText(getString(R.string.humidity))
            .assertIsDisplayed()
    }
}
```

### Navigation Tests

```kotlin
@Test
fun bottomNavigation_navigatesToForecast() {
    composeTestRule.setContent {
        WeatherAppTheme {
            NavigationGraph(preferencesRepository)
        }
    }
    
    composeTestRule
        .onNodeWithText(getString(R.string.nav_forecast))
        .performClick()
    
    composeTestRule
        .onNodeWithText(getString(R.string.five_day_forecast))
        .assertIsDisplayed()
}
```

### Semantic Testing

Test accessibility:

```kotlin
@Test
fun homeScreen_hasProperSemantics() {
    composeTestRule.setContent {
        WeatherAppTheme {
            HomeScreen()
        }
    }
    
    composeTestRule
        .onNode(hasContentDescription("Search"))
        .assertIsDisplayed()
}
```

## Manual Testing

### Test Scenarios

#### Home Screen
- [ ] Search displays autocomplete results
- [ ] Selecting location loads weather
- [ ] Weather cards expand/collapse
- [ ] Favorite button adds/removes location
- [ ] Refresh button updates data
- [ ] Error handling displays properly

#### Forecast Screen
- [ ] 5-day forecast displays correctly
- [ ] Days can be expanded for hourly data
- [ ] Date and time format correct
- [ ] Temperature units match settings
- [ ] Icons display correctly

#### Favorites Screen
- [ ] Favorites list displays saved locations
- [ ] Tapping favorite navigates to weather
- [ ] Delete removes favorite
- [ ] Empty state shows when no favorites
- [ ] Confirmation dialog for delete works

#### Settings Screen
- [ ] Language switch updates UI immediately
- [ ] Temperature unit updates all screens
- [ ] Theme changes apply correctly
- [ ] System theme sync works
- [ ] Settings persist after app restart

#### Cross-Screen Tests
- [ ] Bottom navigation works from all screens
- [ ] Back button navigation works correctly
- [ ] State preserved on configuration change
- [ ] Deep linking works (if implemented)

### Device Testing

Test on:
- **Different screen sizes:**
  - Small phone (< 5")
  - Medium phone (5-6")
  - Large phone (> 6")
  - Tablet (7" and 10")

- **Different Android versions:**
  - API 24 (Android 7.0)
  - API 28 (Android 9.0)
  - API 31 (Android 12.0)
  - API 34 (Android 14.0)

- **Different manufacturers:**
  - Samsung
  - Google Pixel
  - Xiaomi
  - OnePlus

### Edge Cases

- [ ] No internet connection
- [ ] Slow network connection
- [ ] API timeout
- [ ] Invalid API response
- [ ] App in background for long time
- [ ] Low memory conditions
- [ ] Rapid screen rotations
- [ ] Rapid button clicks
- [ ] Empty search results
- [ ] Very long location names

## Performance Testing

### Memory Leaks

Use LeakCanary:

```kotlin
// Add to app/build.gradle.kts
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
```

### CPU Profiling

In Android Studio:
1. Run app with profiler
2. Navigate through all screens
3. Check CPU usage
4. Look for spikes or high usage

### Frame Rate

Test for 60 FPS:
1. Enable GPU rendering profile
2. Settings → Developer Options → Profile GPU Rendering
3. Monitor for frames exceeding 16ms

### App Startup Time

```bash
# Measure cold start
adb shell am start -W com.weatherapp/.MainActivity

# Look for TotalTime in output
```

Target: < 2 seconds cold start

### Memory Usage

```bash
# Check memory usage
adb shell dumpsys meminfo com.weatherapp
```

Monitor for:
- Java heap usage
- Native heap usage
- Graphics memory

## Accessibility Testing

### TalkBack Testing

1. Enable TalkBack: Settings → Accessibility → TalkBack
2. Navigate through app using TalkBack
3. Verify all elements have descriptions
4. Verify navigation is logical

### Content Descriptions

Ensure all interactive elements have contentDescription:

```kotlin
Icon(
    Icons.Default.Search,
    contentDescription = stringResource(R.string.search)
)
```

### Touch Target Size

Minimum 48dp x 48dp for all interactive elements.

### Color Contrast

- Text contrast ratio: at least 4.5:1
- Large text: at least 3:1
- Use Material Design color system

### Testing Tools

- **Accessibility Scanner**: Scan for issues
- **TalkBack**: Screen reader testing
- **Switch Access**: Switch control testing

## Test Reports

### Generate Reports

```bash
# Unit test report
./gradlew test
open app/build/reports/tests/testDebugUnitTest/index.html

# Coverage report
./gradlew jacocoTestReport
open app/build/reports/jacoco/test/html/index.html

# Lint report
./gradlew lint
open app/build/reports/lint-results.html
```

### CI/CD Integration

In GitHub Actions:

```yaml
- name: Run tests
  run: ./gradlew test

- name: Upload test results
  uses: actions/upload-artifact@v3
  with:
    name: test-results
    path: app/build/reports/tests/

- name: Upload coverage
  uses: codecov/codecov-action@v3
  with:
    files: app/build/reports/jacoco/test/jacocoTestReport.xml
```

## Best Practices

### Unit Testing
- Test one thing per test
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)
- Mock external dependencies
- Test edge cases

### UI Testing
- Use semantic queries
- Avoid brittle selectors
- Test user flows, not implementation
- Keep tests independent
- Use test tags for organization

### Test Organization
```
app/src/test/java/
├── viewmodel/
│   ├── HomeViewModelTest.kt
│   ├── ForecastViewModelTest.kt
│   └── SettingsViewModelTest.kt
├── repository/
│   ├── WeatherRepositoryTest.kt
│   └── PreferencesRepositoryTest.kt
└── util/
    └── ResourceTest.kt
```

## Troubleshooting

### Tests Failing
```bash
# Clean and rebuild
./gradlew clean test

# Run with stacktrace
./gradlew test --stacktrace
```

### UI Tests Timeout
- Increase timeout in test
- Check device/emulator performance
- Use `composeTestRule.waitUntil()`

### Flaky Tests
- Add appropriate waits
- Mock time-dependent code
- Use idling resources
- Check for race conditions

## Resources

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Testing Best Practices](https://developer.android.com/training/testing/fundamentals/test-doubles)
- [Mockito Documentation](https://site.mockito.org/)

## Conclusion

Regular testing ensures:
- Code quality
- Feature correctness
- Performance optimization
- User satisfaction

Follow this guide to maintain high test coverage and quality standards.
