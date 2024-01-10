package com.example.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class WeatherForecastDTO(
    @SerializedName("current") val current: WeatherDTO,
    @SerializedName("forecast") val forecastDTO: ForecastDTO
)
