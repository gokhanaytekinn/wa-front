package com.weatherapp.ui.screens.home

import app.cash.turbine.test
import com.weatherapp.data.model.*
import com.weatherapp.data.repository.PreferencesRepository
import com.weatherapp.data.repository.WeatherRepository
import com.weatherapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * HomeViewModel için unit test sınıfı
 * ViewModel davranışlarını test eder
 */
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
        
        // Mock preferences repository default değerler
        whenever(preferencesRepository.lastSelectedCity).thenReturn(flowOf(null))
        whenever(preferencesRepository.lastSelectedDistrict).thenReturn(flowOf(null))
        whenever(preferencesRepository.temperatureUnit).thenReturn(flowOf("celsius"))
        whenever(preferencesRepository.favoriteLocations).thenReturn(flowOf(emptySet()))
        
        viewModel = HomeViewModel(weatherRepository, preferencesRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadWeatherData should update ui state with success`() = runTest {
        // Given
        val mockWeatherData = createMockWeatherData()
        val flow = flow {
            emit(Resource.Loading())
            emit(Resource.Success(mockWeatherData))
        }
        whenever(weatherRepository.getCurrentWeather(any(), any())).thenReturn(flow)
        
        // When
        viewModel.loadWeatherData("Istanbul", null)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.weatherData != null)
            assert(!state.isLoading)
            assert(state.error == null)
        }
    }
    
    @Test
    fun `loadWeatherData should update ui state with error`() = runTest {
        // Given
        val errorMessage = "Network error"
        val flow = flow {
            emit(Resource.Loading())
            emit(Resource.Error<WeatherData>(errorMessage))
        }
        whenever(weatherRepository.getCurrentWeather(any(), any())).thenReturn(flow)
        
        // When
        viewModel.loadWeatherData("Istanbul", null)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assert(!state.isLoading)
            assert(state.error == errorMessage)
        }
    }
    
    @Test
    fun `updateSearchQuery should update search query state`() = runTest {
        // Given
        val query = "Ankara"
        
        // When
        viewModel.updateSearchQuery(query)
        advanceUntilIdle()
        
        // Then
        viewModel.searchQuery.test {
            val searchQuery = awaitItem()
            assert(searchQuery == query)
        }
    }
    
    private fun createMockWeatherData(): WeatherData {
        return WeatherData(
            location = Location(
                city = "Istanbul",
                district = null,
                country = "Turkey",
                latitude = 41.0082,
                longitude = 28.9784
            ),
            sources = listOf(
                WeatherSource(
                    sourceName = "OpenWeather",
                    current = CurrentWeather(
                        temperature = 20.0,
                        feelsLike = 18.0,
                        humidity = 65,
                        windSpeed = 15.0,
                        precipitation = 0.0,
                        pressure = 1013,
                        visibility = 10.0,
                        uvIndex = 5,
                        condition = "Clear",
                        icon = null
                    ),
                    forecast = null
                )
            ),
            timestamp = System.currentTimeMillis()
        )
    }
}
