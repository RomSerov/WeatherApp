package com.example.weatherapp.data.network

import com.example.weatherapp.data.network.dto.CityDTO
import com.example.weatherapp.data.network.dto.WeatherCurrentDTO
import com.example.weatherapp.data.network.dto.WeatherForecastDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("current.json")
    suspend fun loadCurrentWeather(
        @Query("q") query: String
    ): WeatherCurrentDTO

    @GET("forecast.json")
    suspend fun loadForecast(
        @Query("q") query: String,
        @Query("days") daysCount: Int = 4
    ): WeatherForecastDTO

    @GET("search.json")
    suspend fun searchCity(
        @Query("q") query: String,
    ): List<CityDTO>
}