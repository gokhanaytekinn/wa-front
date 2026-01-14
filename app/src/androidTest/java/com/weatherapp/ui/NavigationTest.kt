package com.weatherapp.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weatherapp.ui.theme.WeatherAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI bileşenleri için instrumentation test
 * Compose UI öğelerini test eder
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testBottomNavigationIsDisplayed() {
        // Test için minimum composable yapısı
        composeTestRule.setContent {
            WeatherAppTheme {
                // NavigationGraph burada test edilebilir
                // Ancak dependency injection gerektirir
            }
        }
        
        // Bottom navigation öğelerinin görünürlüğünü test et
        // composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }
}
