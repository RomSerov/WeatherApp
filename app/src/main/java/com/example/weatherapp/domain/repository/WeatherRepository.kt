package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.entity.Forecast
import com.example.weatherapp.domain.entity.Weather

interface WeatherRepository {

   suspend fun getWeather(cityId: Int): Weather

   suspend fun getForecast(cityId: Int): Forecast
}