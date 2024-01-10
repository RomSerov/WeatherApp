package com.example.weatherapp.data.mapper

import com.example.weatherapp.data.network.dto.CityDTO
import com.example.weatherapp.domain.entity.City

fun CityDTO.toEntity(): City = City(id, name, country)

fun List<CityDTO>.toEntities(): List<City> = map { it.toEntity() }