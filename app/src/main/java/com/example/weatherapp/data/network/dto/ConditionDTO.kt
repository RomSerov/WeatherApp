package com.example.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class ConditionDTO(
    @SerializedName("text") val text: String,
    @SerializedName("icon") val iconUrl: String
)
