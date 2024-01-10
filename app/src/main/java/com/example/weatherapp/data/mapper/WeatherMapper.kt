package com.example.weatherapp.data.mapper

import com.example.weatherapp.data.network.dto.WeatherCurrentDTO
import com.example.weatherapp.data.network.dto.WeatherDTO
import com.example.weatherapp.data.network.dto.WeatherForecastDTO
import com.example.weatherapp.domain.entity.Forecast
import com.example.weatherapp.domain.entity.Weather
import java.util.Calendar
import java.util.Date

private fun Long.toCalendar() = Calendar.getInstance().apply {
    time = Date(this@toCalendar * 1000)
}

private fun String.correctImageUrl() = "https:$this".replace(
    oldValue = "64x64",
    newValue = "128x128"
)

fun WeatherDTO.toEntity(): Weather = Weather(
    tempC = tempC,
    conditionText = conditionDTO.text,
    conditionUrl = conditionDTO.iconUrl.correctImageUrl(),
    date = date.toCalendar()
)

fun WeatherCurrentDTO.toEntity(): Weather = current.toEntity()

fun WeatherForecastDTO.toEntity(): Forecast = Forecast(
    currentWeather = current.toEntity(),
    upcoming = forecastDTO.forecastDay
        .drop(1)
        .map { dayDTO ->
        val dayWeatherDTO = dayDTO.dayWeatherDTO
        Weather(
            tempC = dayWeatherDTO.tempC,
            conditionText = dayWeatherDTO.conditionDTO.text,
            conditionUrl = dayWeatherDTO.conditionDTO.iconUrl.correctImageUrl(),
            date = dayDTO.date.toCalendar()
        )
    }
)