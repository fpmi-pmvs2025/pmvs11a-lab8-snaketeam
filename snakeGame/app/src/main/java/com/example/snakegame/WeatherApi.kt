package com.example.snakegame

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val current: CurrentWeather
)

data class CurrentWeather(
    val temp_c: Float,
    val condition: WeatherCondition
)

data class WeatherCondition(
    val text: String,
    val code: Int
)

interface WeatherApiService {
    @GET("current.json")
    fun getCurrentWeather(
        @Query("q") city: String,
        @Query("key") apiKey: String = "5f6696d556544ec0ab0165849251703"
    ): Call<WeatherResponse>
}