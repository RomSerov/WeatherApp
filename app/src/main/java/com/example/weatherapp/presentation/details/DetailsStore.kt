package com.example.weatherapp.presentation.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.entity.Forecast
import com.example.weatherapp.domain.usecases.ChangeFavouriteStatusUseCase
import com.example.weatherapp.domain.usecases.GetForecastUseCase
import com.example.weatherapp.domain.usecases.ObserveFavouriteStatusUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

interface DetailsStore : Store<DetailsStore.Intent, DetailsStore.State, DetailsStore.Label> {

    sealed interface Intent {
        data object ClickBack : Intent
        data object ClickChangeFavouriteStatus : Intent
    }

    data class State(
        val city: City,
        val isFavourite: Boolean,
        val forecastState: ForecastState
    ) {

        sealed interface ForecastState {
            data object Initial : ForecastState
            data object Loading : ForecastState
            data object Error : ForecastState
            data class Success(val forecast: Forecast) : ForecastState
        }
    }

    sealed interface Label {
        data object ClickBack : Label
    }
}

class DetailsStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getForecastUseCase: GetForecastUseCase,
    private val changeFavouriteStatusUseCase: ChangeFavouriteStatusUseCase,
    private val observeFavouriteStatusUseCase: ObserveFavouriteStatusUseCase
) {
    fun create(city: City): DetailsStore = object : DetailsStore,
        Store<DetailsStore.Intent, DetailsStore.State, DetailsStore.Label> by storeFactory.create(
            name = "DetailsStore",
            initialState = DetailsStore.State(
                city = city,
                isFavourite = false,
                forecastState = DetailsStore.State.ForecastState.Initial
            ),
            bootstrapper = BootstrapperImpl(city = city),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data class FavouriteStatusChanged(val isFavourite: Boolean) : Action
        data class ForecastSuccess(val forecast: Forecast) : Action
        data object ForecastLoading : Action
        data object ForecastError : Action
    }

    private sealed interface Msg {
        data class FavouriteStatusChanged(val isFavourite: Boolean) : Msg
        data class ForecastSuccess(val forecast: Forecast) : Msg
        data object ForecastLoading : Msg
        data object ForecastError : Msg
    }

    private inner class BootstrapperImpl(
        private val city: City
    ) : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                observeFavouriteStatusUseCase(cityId = city.id).collect {
                    dispatch(Action.FavouriteStatusChanged(isFavourite = it))
                }
            }
            scope.launch {
                dispatch(Action.ForecastLoading)
                try {
                    val forecast = getForecastUseCase(cityId = city.id)
                    dispatch(Action.ForecastSuccess(forecast = forecast))
                } catch (e: Exception) {
                    dispatch(Action.ForecastError)
                }
            }
        }
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<DetailsStore.Intent, Action, DetailsStore.State, Msg, DetailsStore.Label>() {
        override fun executeIntent(
            intent: DetailsStore.Intent,
            getState: () -> DetailsStore.State
        ) {
            when (intent) {
                DetailsStore.Intent.ClickBack -> {
                    publish(DetailsStore.Label.ClickBack)
                }

                DetailsStore.Intent.ClickChangeFavouriteStatus -> {
                    scope.launch {
                        val state = getState()
                        if (state.isFavourite) {
                            changeFavouriteStatusUseCase.removeFromFavourite(cityId = state.city.id)
                        } else {
                            changeFavouriteStatusUseCase.addToFavourite(city = state.city)
                        }
                    }
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> DetailsStore.State) {
            when (action) {
                is Action.FavouriteStatusChanged -> {
                    dispatch(Msg.FavouriteStatusChanged(isFavourite = action.isFavourite))
                }

                Action.ForecastError -> {
                    dispatch(Msg.ForecastError)
                }

                Action.ForecastLoading -> {
                    dispatch(Msg.ForecastLoading)
                }

                is Action.ForecastSuccess -> {
                    dispatch(Msg.ForecastSuccess(forecast = action.forecast))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<DetailsStore.State, Msg> {
        override fun DetailsStore.State.reduce(msg: Msg): DetailsStore.State = when (msg) {
            is Msg.FavouriteStatusChanged -> {
                copy(isFavourite = msg.isFavourite)
            }

            Msg.ForecastError -> {
                copy(forecastState = DetailsStore.State.ForecastState.Error)
            }

            Msg.ForecastLoading -> {
                copy(forecastState = DetailsStore.State.ForecastState.Loading)
            }

            is Msg.ForecastSuccess -> {
                copy(forecastState = DetailsStore.State.ForecastState.Success(forecast = msg.forecast))
            }
        }
    }
}