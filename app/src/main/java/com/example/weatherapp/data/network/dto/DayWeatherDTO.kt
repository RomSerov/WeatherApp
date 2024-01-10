package com.example.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class DayWeatherDTO(
    @SerializedName("avgtemp_c") val tempC: Float,
    @SerializedName("condition") val conditionDTO: ConditionDTO
)
