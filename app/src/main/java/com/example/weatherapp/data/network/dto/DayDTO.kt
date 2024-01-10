package com.example.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class DayDTO(
    @SerializedName("date_epoch") val date: Long,
    @SerializedName("day") val dayWeatherDTO: DayWeatherDTO
)
