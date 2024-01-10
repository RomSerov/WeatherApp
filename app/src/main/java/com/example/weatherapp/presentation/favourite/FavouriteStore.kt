package com.example.weatherapp.presentation.favourite

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.usecases.GetCurrentWeatherUseCase
import com.example.weatherapp.domain.usecases.GetFavouriteCitiesUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

interface FavouriteStore :
    Store<FavouriteStore.Intent, FavouriteStore.State, FavouriteStore.Label> {

    sealed interface Intent {
        data object ClickSearch : Intent
        data object ClickToFavourite : Intent
        data class CityItemClicked(val city: City) : Intent
    }

    data class State(
        val cityItems: List<CityItem>
    ) {

        data class CityItem(
            val city: City,
            val weatherState: WeatherState
        )

        sealed interface WeatherState {
            data object Initial : WeatherState
            data object Loading : WeatherState
            data object Error : WeatherState
            data class Success(
                val tempC: Float,
                val iconUrl: String
            ) : WeatherState
        }
    }

    sealed interface Label {
        data object ClickSearch : Label
        data object ClickToFavourite : Label
        data class CityItemClicked(val city: City) : Label
    }
}

class FavoriteStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getFavouriteCitiesUseCase: GetFavouriteCitiesUseCase,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase
) {
    fun create(): FavouriteStore =
        object : FavouriteStore,
            Store<FavouriteStore.Intent, FavouriteStore.State, FavouriteStore.Label> by storeFactory.create(
                name = "FavouriteStore",
                initialState = FavouriteStore.State(cityItems = listOf()),
                bootstrapper = BootstrapperImpl(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl
            ) {}

    private sealed interface Action {
        data class FavouriteCitiesSuccess(val cities: List<City>) : Action
    }

    private sealed interface Msg {
        data class FavouriteCitiesSuccess(val cities: List<City>) : Msg
        data class WeatherSuccess(
            val cityId: Int,
            val tempC: Float,
            val conditionIconUrl: String
        ) : Msg

        data class WeatherError(val cityId: Int) : Msg
        data class WeatherLoading(val cityId: Int) : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                getFavouriteCitiesUseCase().collect {
                    dispatch(Action.FavouriteCitiesSuccess(cities = it))
                }
            }
        }
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<FavouriteStore.Intent, Action, FavouriteStore.State, Msg, FavouriteStore.Label>() {
        override fun executeIntent(
            intent: FavouriteStore.Intent,
            getState: () -> FavouriteStore.State
        ) {
            when (intent) {
                is FavouriteStore.Intent.CityItemClicked -> {
                    publish(FavouriteStore.Label.CityItemClicked(city = intent.city))
                }

                FavouriteStore.Intent.ClickSearch -> {
                    publish(FavouriteStore.Label.ClickSearch)
                }

                FavouriteStore.Intent.ClickToFavourite -> {
                    publish(FavouriteStore.Label.ClickToFavourite)
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> FavouriteStore.State) {
            when (action) {
                is Action.FavouriteCitiesSuccess -> {
                    val cities = action.cities
                    dispatch(Msg.FavouriteCitiesSuccess(cities = cities))
                    cities.forEach { city ->
                        scope.launch {
                            loadWeatherForCity(city = city)
                        }
                    }
                }
            }
        }

        private suspend fun loadWeatherForCity(city: City) {
            dispatch(Msg.WeatherLoading(cityId = city.id))
            try {
                val weather = getCurrentWeatherUseCase(cityId = city.id)
                dispatch(
                    Msg.WeatherSuccess(
                        cityId = city.id,
                        tempC = weather.tempC,
                        conditionIconUrl = weather.conditionUrl
                    )
                )
            } catch (e: Exception) {
                dispatch(Msg.WeatherError(cityId = city.id))
            }
        }
    }

    private object ReducerImpl : Reducer<FavouriteStore.State, Msg> {
        override fun FavouriteStore.State.reduce(msg: Msg): FavouriteStore.State = when (msg) {
            is Msg.FavouriteCitiesSuccess -> {
                copy(
                    cityItems = msg.cities.map {
                        FavouriteStore.State.CityItem(
                            city = it,
                            weatherState = FavouriteStore.State.WeatherState.Initial
                        )
                    }
                )
            }

            is Msg.WeatherError -> {
                copy(
                    cityItems = cityItems.map {
                        if (it.city.id == msg.cityId) {
                            it.copy(weatherState = FavouriteStore.State.WeatherState.Error)
                        } else {
                            it
                        }
                    }
                )
            }

            is Msg.WeatherLoading -> {
                copy(
                    cityItems = cityItems.map {
                        if (it.city.id == msg.cityId) {
                            it.copy(weatherState = FavouriteStore.State.WeatherState.Loading)
                        } else {
                            it
                        }
                    }
                )
            }

            is Msg.WeatherSuccess -> {
                copy(
                    cityItems = cityItems.map {
                        if (it.city.id == msg.cityId) {
                            it.copy(
                                weatherState = FavouriteStore.State.WeatherState.Success(
                                    tempC = msg.tempC,
                                    iconUrl = msg.conditionIconUrl
                                )
                            )
                        } else {
                            it
                        }
                    }
                )
            }
        }
    }
}