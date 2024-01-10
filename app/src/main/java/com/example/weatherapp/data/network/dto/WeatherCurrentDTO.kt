package com.example.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class WeatherCurrentDTO(
    @SerializedName("current") val current: WeatherDTO
)
