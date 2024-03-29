package com.example.weather.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.models.LocationItem
import com.example.weather.viewmodels.States.SearchState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchLocationViewModel @Inject constructor(@ApplicationContext context: Context) :
    ViewModel() {
    /**
     * Temporarily, all city names with the country name 'US' are filtered and stored locally in the 'raw' folder.
     */
    private val rawResourceId = R.raw.cities
    private val _usLocations = MutableStateFlow<List<LocationItem>>(emptyList())
    val usLocations = _usLocations

    private val _filteredLocations = MutableStateFlow<List<LocationItem>>(emptyList())
    val filteredLocations: StateFlow<List<LocationItem>> get() = _filteredLocations

    private var filteringJob: Job? = null

    private val _screenState = MutableStateFlow(SearchState.LOADING)
    val screenState = _screenState

    init {
        fetchData(context)
    }

    override fun onCleared() {
        super.onCleared()
        filteringJob?.cancel()
    }

    fun filterLocations(searchText: String) {
        filteringJob?.cancel()
        filteringJob = viewModelScope.launch {
            val filteredList = _usLocations.value.filter {
                it.name.contains(searchText, ignoreCase = true)
            }
            _filteredLocations.value = filteredList
        }
    }

    /*
        A local JSON file is utilized for filtering US cities. Upon successful file reading, '_usLocations' is updated,
        and the search bar becomes visible on the screen, enabling users to start searching.

       Scope for improvement: Implementation of data caching will help to skip file reading on the next ViewModel initialization.
     */
    private fun fetchData(context: Context) {
        viewModelScope.launch {
            _screenState.value = SearchState.LOADING

            try {
                withContext(Dispatchers.IO) {
                    val jsonString =
                        context.resources.openRawResource(rawResourceId).bufferedReader()
                            .use { it.readText() }
                    val locationItems: List<LocationItem> =
                        Gson().fromJson(
                            jsonString,
                            object : TypeToken<List<LocationItem>>() {}.type
                        )

                    val usLocations = locationItems.filter { it.country == "US" }

                    withContext(Dispatchers.Main) {
                        _usLocations.value = usLocations
                        _screenState.value = SearchState.SUCCESS
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _screenState.value = SearchState.ERROR
                }
            }
        }
    }
}