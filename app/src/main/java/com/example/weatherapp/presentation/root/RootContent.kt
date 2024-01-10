package com.example.weatherapp.presentation.root

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.example.weatherapp.presentation.details.DetailsContent
import com.example.weatherapp.presentation.favourite.FavoriteContent
import com.example.weatherapp.presentation.search.SearchContent
import com.example.weatherapp.presentation.ui.theme.WeatherAppTheme

@Composable
fun RootContent(
    component: RootComponent
) {
    WeatherAppTheme {
        Children(
            stack = component.stack,
            content = {
                when (val instance = it.instance) {
                    is RootComponent.Child.Details -> {
                        DetailsContent(component = instance.component)
                    }

                    is RootComponent.Child.Favourite -> {
                        FavoriteContent(component = instance.component)
                    }

                    is RootComponent.Child.Search -> {
                        SearchContent(component = instance.component)
                    }
                }
            }
        )
    }
}