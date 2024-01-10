package com.example.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class ForecastDTO(
    @SerializedName("forecastday") val forecastDay: List<DayDTO>
)
