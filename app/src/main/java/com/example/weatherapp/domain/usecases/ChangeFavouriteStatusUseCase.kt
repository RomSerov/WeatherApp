package com.example.weatherapp.domain.usecases

import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.repository.FavouriteRepository
import javax.inject.Inject

class ChangeFavouriteStatusUseCase @Inject constructor(
    private val repository: FavouriteRepository
) {

    suspend fun addToFavourite(city: City) = repository.addToFavourite(city = city)

    suspend fun removeFromFavourite(cityId: Int) = repository.removeFromFavourite(cityId = cityId)
}