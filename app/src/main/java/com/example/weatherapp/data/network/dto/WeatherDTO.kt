package com.example.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class WeatherDTO(
    @SerializedName("last_updated_epoch") val date: Long,
    @SerializedName("temp_c") val tempC: Float,
    @SerializedName("condition") val conditionDTO: ConditionDTO
)
