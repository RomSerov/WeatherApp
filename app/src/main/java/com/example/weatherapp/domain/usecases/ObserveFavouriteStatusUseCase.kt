package com.example.weatherapp.domain.usecases

import com.example.weatherapp.domain.repository.FavouriteRepository
import javax.inject.Inject

class ObserveFavouriteStatusUseCase @Inject constructor(
    private val repository: FavouriteRepository
) {
    operator fun invoke(cityId: Int) = repository.observeIsFavourite(cityId = cityId)
}