package com.example.a123

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("daily") daily: String = "temperature_2m_max,precipitation_sum",
        @Query("timezone") timezone: String = "GMT"
    ): WeatherResponse
}

data class WeatherResponse(
    val daily: DailyWeather
)

data class DailyWeather(
    val temperature_2m_max: List<Float>,
    val precipitation_sum: List<Float>,
    val time: List<String>
)
