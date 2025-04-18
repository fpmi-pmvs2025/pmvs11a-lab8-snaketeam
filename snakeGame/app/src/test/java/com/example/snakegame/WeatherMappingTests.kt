package com.example.snakegame

import junit.framework.TestCase.assertEquals
import org.junit.Test

class WeatherMappingTests {
    private val settingsFragment = SettingsFragment()
    @Test
    fun testWeatherMapping() {
        assertEquals("sunny", settingsFragment.mapWeatherCode(1000))
        assertEquals("cloudy", settingsFragment.mapWeatherCode(1003))
        assertEquals("rain", settingsFragment.mapWeatherCode(1063))
        assertEquals("snow", settingsFragment.mapWeatherCode(1066))
        assertEquals("fog", settingsFragment.mapWeatherCode(1030))
        assertEquals("cloudy", settingsFragment.mapWeatherCode(9999)) // Unknown code
    }
}