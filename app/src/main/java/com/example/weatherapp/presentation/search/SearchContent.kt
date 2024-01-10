package com.example.weatherapp.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.weatherapp.R
import com.example.weatherapp.domain.entity.City

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    component: SearchComponent
) {
    val state by component.model.collectAsState()

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    SearchBar(
        modifier = Modifier.focusRequester(focusRequester),
        placeholder = {
            Text(text = stringResource(id = R.string.description_search))
        },
        query = state.searchQuery,
        onQueryChange = {
            component.changeSearchQuery(query = it)
        },
        onSearch = {
            component.onClickSearch()
        },
        active = true,
        onActiveChange = {

        },
        leadingIcon = {
            IconButton(
                onClick = {
                    component.onClickBack()
                },
                content = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    component.onClickSearch()
                },
                content = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                }
            )
        },
        content = {
            when (val searchState = state.searchStare) {
                SearchStore.State.SearchStare.Empty -> {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.empty_search_text)
                    )
                }

                SearchStore.State.SearchStare.Error -> {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.something_went_wrong)
                    )
                }

                SearchStore.State.SearchStare.Initial -> {

                }

                SearchStore.State.SearchStare.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                        content = {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    )
                }

                is SearchStore.State.SearchStare.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp),
                        content = {
                            items(
                                items = searchState.cities,
                                key = { it.id },
                                itemContent = { item ->
                                    CityCard(
                                        city = item,
                                        onCityClick = { city ->
                                            component.onClickCity(city = city)
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun CityCard(
    city: City,
    onCityClick: (City) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCityClick(city) }
                    .padding(
                        vertical = 8.dp,
                        horizontal = 16.dp
                    ),
                content = {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = city.country
                    )
                }
            )
        }
    )
}