package com.example.weatherapp.presentation.favourite

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

class FavouriteComponentImpl @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    private val storeFactory: FavoriteStoreFactory,
    @Assisted("onCityItemClicked") private val onCityItemClicked: (City) -> Unit,
    @Assisted("onAddToFavouriteClicked") private val onAddToFavouriteClicked: () -> Unit,
    @Assisted("onSearchClicked") private val onSearchClicked: () -> Unit
) : FavouriteComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }
    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when (it) {
                    is FavouriteStore.Label.CityItemClicked -> {
                        onCityItemClicked(it.city)
                    }

                    FavouriteStore.Label.ClickSearch -> {
                        onSearchClicked()
                    }

                    FavouriteStore.Label.ClickToFavourite -> {
                        onAddToFavouriteClicked()
                    }
                }
            }
        }
    }

    override val model: StateFlow<FavouriteStore.State> = store.stateFlow

    override fun onClickSearch() {
        store.accept(intent = FavouriteStore.Intent.ClickSearch)
    }

    override fun onClickAddFavourite() {
        store.accept(intent = FavouriteStore.Intent.ClickToFavourite)
    }

    override fun onClickCityItem(city: City) {
        store.accept(intent = FavouriteStore.Intent.CityItemClicked(city = city))
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onCityItemClicked") onCityItemClicked: (City) -> Unit,
            @Assisted("onAddToFavouriteClicked") onAddToFavouriteClicked: () -> Unit,
            @Assisted("onSearchClicked") onSearchClicked: () -> Unit
        ): FavouriteComponentImpl
    }
}