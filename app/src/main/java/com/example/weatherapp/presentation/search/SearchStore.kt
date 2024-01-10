package com.example.weatherapp.presentation.search

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.usecases.ChangeFavouriteStatusUseCase
import com.example.weatherapp.domain.usecases.SearchCityUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SearchStore : Store<SearchStore.Intent, SearchStore.State, SearchStore.Label> {

    sealed interface Intent {
        data class ChangeSearchQuery(val query: String) : Intent
        data object ClickBack : Intent
        data object ClickSearch : Intent
        data class ClickCity(val city: City) : Intent
    }

    data class State(
        val searchQuery: String,
        val searchStare: SearchStare
    ) {

        sealed interface SearchStare {
            data object Initial : SearchStare
            data object Error : SearchStare
            data object Loading : SearchStare
            data object Empty : SearchStare
            data class Success(val cities: List<City>) : SearchStare
        }
    }

    sealed interface Label {
        data object ClickBack : Label
        data object SaveToFavourite : Label
        data class OpenForecast(val city: City) : Label
    }
}

class SearchStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val searchCityUseCase: SearchCityUseCase,
    private val changeFavouriteStatusUseCase: ChangeFavouriteStatusUseCase
) {
    fun create(openSearch: OpenSearch): SearchStore =
        object : SearchStore,
            Store<SearchStore.Intent, SearchStore.State, SearchStore.Label> by storeFactory.create(
                name = "SearchStore",
                initialState = SearchStore.State(
                    searchQuery = "",
                    searchStare = SearchStore.State.SearchStare.Initial
                ),
                executorFactory = { ExecutorImpl(openSearch = openSearch) },
                reducer = ReducerImpl
            ) {}

    private sealed interface Msg {
        data class ChangeSearchQuery(val query: String) : Msg
        data object SearchLoading : Msg
        data object SearchError : Msg
        data class SearchSuccess(val cities: List<City>) : Msg
    }

    private inner class ExecutorImpl(
        private val openSearch: OpenSearch
    ) : CoroutineExecutor<SearchStore.Intent, Nothing, SearchStore.State, Msg, SearchStore.Label>() {

        private var searchJob: Job? = null

        override fun executeIntent(intent: SearchStore.Intent, getState: () -> SearchStore.State) {
            when (intent) {
                is SearchStore.Intent.ChangeSearchQuery -> {
                    dispatch(Msg.ChangeSearchQuery(query = intent.query))
                }

                SearchStore.Intent.ClickBack -> {
                    publish(SearchStore.Label.ClickBack)
                }

                is SearchStore.Intent.ClickCity -> {
                    when (openSearch) {
                        OpenSearch.FROM_ADD -> {
                            scope.launch {
                                changeFavouriteStatusUseCase.addToFavourite(city = intent.city)
                                publish(SearchStore.Label.SaveToFavourite)
                            }
                        }

                        OpenSearch.FROM_MAIN -> {
                            publish(SearchStore.Label.OpenForecast(city = intent.city))
                        }
                    }
                }

                SearchStore.Intent.ClickSearch -> {
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        dispatch(Msg.SearchLoading)
                        try {
                            val cities = searchCityUseCase(query = getState().searchQuery)
                            dispatch(Msg.SearchSuccess(cities = cities))
                        } catch (e: Exception) {
                            dispatch(Msg.SearchError)
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<SearchStore.State, Msg> {
        override fun SearchStore.State.reduce(msg: Msg): SearchStore.State = when (msg) {

            is Msg.ChangeSearchQuery -> {
                copy(searchQuery = msg.query)
            }

            Msg.SearchError -> {
                copy(searchStare = SearchStore.State.SearchStare.Error)
            }

            Msg.SearchLoading -> {
                copy(searchStare = SearchStore.State.SearchStare.Loading)
            }

            is Msg.SearchSuccess -> {
                val searchState = if (msg.cities.isEmpty()) {
                    SearchStore.State.SearchStare.Empty
                } else {
                    SearchStore.State.SearchStare.Success(cities = msg.cities)
                }
                copy(searchStare = searchState)
            }
        }
    }
}