package com.example.weatherapp.presentation.root

import android.os.Parcelable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.presentation.details.DetailsComponentImpl
import com.example.weatherapp.presentation.favourite.FavouriteComponentImpl
import com.example.weatherapp.presentation.search.OpenSearch
import com.example.weatherapp.presentation.search.SearchComponentImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.parcelize.Parcelize

class RootComponentImpl @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    private val searchComponentFactory: SearchComponentImpl.Factory,
    private val detailsComponentFactory: DetailsComponentImpl.Factory,
    private val favouriteComponentFactory: FavouriteComponentImpl.Factory,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        initialConfiguration = Config.Favourite,
        handleBackButton = true,
        childFactory = ::child
    )

    private fun child(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child {
        return when (config) {
            is Config.Details -> {
                val component = detailsComponentFactory.create(
                    city = config.city,
                    componentContext = componentContext,
                    onBackClicked = {
                        navigation.pop()
                    }
                )
                RootComponent.Child.Details(component = component)
            }

            Config.Favourite -> {
                val component = favouriteComponentFactory.create(
                    componentContext = componentContext,
                    onAddToFavouriteClicked = {
                        navigation.push(Config.Search(openSearch = OpenSearch.FROM_ADD))
                    },
                    onCityItemClicked = {
                        navigation.push(Config.Details(city = it))
                    },
                    onSearchClicked = {
                        navigation.push(Config.Search(openSearch = OpenSearch.FROM_MAIN))
                    }
                )
                RootComponent.Child.Favourite(component = component)
            }

            is Config.Search -> {
                val component = searchComponentFactory.create(
                    componentContext = componentContext,
                    openSearch = config.openSearch,
                    onBackClicked = {
                        navigation.pop()
                    },
                    onCitySavedToFavourite = {
                        navigation.pop()
                    },
                    onForecastForCityRequested = {
                        navigation.push(Config.Details(city = it))
                    }
                )
                RootComponent.Child.Search(component = component)
            }
        }
    }

    sealed interface Config : Parcelable {

        @Parcelize
        data object Favourite : Config

        @Parcelize
        data class Search(val openSearch: OpenSearch) : Config

        @Parcelize
        data class Details(val city: City) : Config
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): RootComponentImpl
    }
}