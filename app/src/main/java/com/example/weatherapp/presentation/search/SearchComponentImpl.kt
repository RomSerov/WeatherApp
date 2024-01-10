package com.example.weatherapp.presentation.search

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchComponentImpl @AssistedInject constructor(
    private val storeFactory: SearchStoreFactory,
    @Assisted("openSearch") private val openSearch: OpenSearch,
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("onBackClicked") private val onBackClicked: () -> Unit,
    @Assisted("onCitySavedToFavourite") private val onCitySavedToFavourite: () -> Unit,
    @Assisted("onForecastForCityRequested") private val onForecastForCityRequested: (City) -> Unit
) : SearchComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(openSearch = openSearch) }
    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when (it) {
                    SearchStore.Label.ClickBack -> {
                        onBackClicked()
                    }

                    is SearchStore.Label.OpenForecast -> {
                        onForecastForCityRequested(it.city)
                    }

                    SearchStore.Label.SaveToFavourite -> {
                        onCitySavedToFavourite()
                    }
                }
            }
        }
    }

    override val model: StateFlow<SearchStore.State> = store.stateFlow

    override fun changeSearchQuery(query: String) {
        store.accept(intent = SearchStore.Intent.ChangeSearchQuery(query = query))
    }

    override fun onClickBack() {
        store.accept(intent = SearchStore.Intent.ClickBack)
    }

    override fun onClickSearch() {
        store.accept(intent = SearchStore.Intent.ClickSearch)
    }

    override fun onClickCity(city: City) {
        store.accept(intent = SearchStore.Intent.ClickCity(city = city))
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("openSearch") openSearch: OpenSearch,
            @Assisted("onBackClicked") onBackClicked: () -> Unit,
            @Assisted("onCitySavedToFavourite") onCitySavedToFavourite: () -> Unit,
            @Assisted("onForecastForCityRequested") onForecastForCityRequested: (City) -> Unit
        ): SearchComponentImpl
    }
}