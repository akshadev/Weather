package com.example.weather.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weather.models.LocationItem
import com.example.weather.viewmodels.SearchLocationViewModel
import com.example.weather.viewmodels.States.SearchState

@Composable
fun SearchLocationScreen(
    viewModel: SearchLocationViewModel = hiltViewModel(),
    onCitySelection: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    val onSearchTextChanged: (String) -> Unit = {
        searchText = it
        viewModel.filterLocations(searchText)
    }

    val onSearchActionClick: () -> Unit = {
        viewModel.filterLocations(searchText)
    }

    val usLocations by viewModel.filteredLocations.collectAsState()
    val screenState by viewModel.screenState.collectAsState()

    when (screenState) {
        SearchState.SUCCESS -> {
            ShowList(
                usLocations,
                onCitySelection,
                searchText = searchText,
                onSearchTextChanged = onSearchTextChanged,
                onSearchActionClick
            )
        }

        SearchState.LOADING, SearchState.ERROR -> {
            LoadingScreen()
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ShowList(
    usLocations: List<LocationItem>,
    onCitySelection: (String) -> Unit,
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    onSearchActionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = searchText,
            onValueChange = onSearchTextChanged,
            placeholder = { androidx.compose.material.Text("Search") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearchActionClick() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // LazyColumn for displaying search results or other content
        usLocations.isNotEmpty().let {
            LazyColumn {
                items(usLocations.size) { index ->
                    Text(
                        text = usLocations[index].name,
                        modifier = Modifier
                            .clickable {
                                onCitySelection(usLocations[index].name)
                            }
                            .fillMaxWidth()
                            .padding(16.dp)

                    )
                }
            }
        }
    }
}