package com.example.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class CityDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String
)
