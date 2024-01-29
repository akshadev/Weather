package com.example.weather.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.datastore.UserLocationDataStore
import com.example.weather.location.LocationUtils
import com.example.weather.location.LocationUtils.getAddressFromLocation
import com.example.weather.location.UserLocation
import com.example.weather.repository.WeatherRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.weather.response.Response
import com.example.weather.viewmodels.States.WeatherForeCastState
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update


class WeatherForeCastViewModel @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val userLocationDataStore: UserLocationDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private var _locationPermissionEvent = MutableSharedFlow<Boolean>()
    val locationPermissionEvent: Flow<Boolean> get() = _locationPermissionEvent.asSharedFlow()
    val visiblePermissionQueue = mutableStateListOf<String>()
    private val _forecastScreenState = MutableStateFlow(WeatherForeCastState())
    val forecastScreenState = _forecastScreenState.asStateFlow()
    private val _userLocation = MutableLiveData<UserLocation?>()
    val userLocation: MutableLiveData<UserLocation?> get() = _userLocation

    init {
        startLocationFetch()
    }

    private fun startLocationFetch() {

        viewModelScope.launch {
            userLocationDataStore.userLocationFlow.firstOrNull()?.let { userLocation ->
                if (userLocation.cityName.isNotBlank()) fetchForecast(
                    userLocation.cityName, true
                )
            }
            if (LocationUtils.isLocationPermissionGranted(context)) {
                fetchGPSLocation()
            }
        }
    }

    private suspend fun fetchGPSLocation() {
        if (LocationUtils.isLocationPermissionGranted(context)) {
            LocationUtils.fetchUserLocation(context).collectLatest { result ->
                when (result) {
                    is Response.Loading -> {
                        _forecastScreenState.update {
                            it.copy(isLoading = true)
                        }
                    }

                    is Response.Error -> {
                        _forecastScreenState.update {
                            it.copy(
                                isLoading = false,
                                currentLocationForecast = null,
                                error = result.message
                            )
                        }
                    }

                    is Response.Success -> {
                        result.data?.let { userLocation ->
                            val cityName = getAddressFromLocation(
                                context, userLocation.latitude, userLocation.longitude
                            )
                            cityName?.let {
                                UserLocation(
                                    latitude = userLocation.latitude,
                                    longitude = userLocation.longitude,
                                    cityName = it
                                )
                            }?.also {
                                fetchForecast(it.cityName, false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun dismissDialog() {
        visiblePermissionQueue.removeLast()
    }

    fun onPermissionResult(
        permission: String, isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionQueue.contains(permission)) {
            visiblePermissionQueue.add(permission)
        } else {
            visiblePermissionQueue.remove(permission)
            viewModelScope.launch(Dispatchers.IO) {
                fetchGPSLocation()
            }
        }
    }

    private fun fetchForecast(location: String, manualSearch: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepo.getWeatherForeCast(location).collectLatest { response ->
                when (response) {
                    is Response.Loading -> _forecastScreenState.update {
                        it.copy(isLoading = true)
                    }

                    is Response.Success -> _forecastScreenState.update {
                        val lat = response.data?.coordinates?.lat
                        val long = response.data?.coordinates?.lon
                        val name = response.data?.name

                        if (manualSearch) {
                            lat?.let { it1 ->
                                if (long != null) {
                                    if (name != null) {
                                        userLocationDataStore.saveUserLocation(
                                            UserLocation(
                                                it1, long, name
                                            )
                                        )

                                    }
                                }
                            }
                        }
                        it.copy(
                            isLoading = false,
                            currentLocationForecast = if (manualSearch) it.currentLocationForecast else response.data,
                            lastSearchLocationForecast = if (!manualSearch) it.lastSearchLocationForecast else response.data,
                            error = null
                        )
                    }

                    is Response.Error -> _forecastScreenState.update {
                        it.copy(
                            isLoading = false,
                            currentLocationForecast = null,
                            error = response.message
                        )
                    }
                }
            }
        }
    }

    fun fetchLocationForCity(city: String) {
        fetchForecast(city, true)
    }
}