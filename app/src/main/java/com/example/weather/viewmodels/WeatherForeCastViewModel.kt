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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update

@HiltViewModel
class WeatherForeCastViewModel @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val userLocationDataStore: UserLocationDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private var _locationPermissionEvent = MutableSharedFlow<Boolean>()
    val locationPermissionEvent: Flow<Boolean> get() = _locationPermissionEvent.asSharedFlow()

    /*
    'visiblePermissionQueue' is used to keep track of denied permissions. When a new entry is added to this list,
    an alert is triggered to inform the user about the importance of the denied permission for the functionality of the app.
    */
    val visiblePermissionQueue = mutableStateListOf<String>()

    /*
    'ForecastScreenState' class is to manage the screen state, containing the following attributes:
   - 'isLoading': Used to determine whether a loader should be displayed. Currently not being utilized.
   - 'currentLocationForecast': Stores the forecast information of the current location, fetched from the fused API.
   - 'lastSearchLocationForecast': Holds the forecast information of the user-searched city.
   - 'error': Provides information about any error that occurred, facilitating error reporting.
    */
    private val _forecastScreenState = MutableStateFlow(WeatherForeCastState())
    val forecastScreenState = _forecastScreenState.asStateFlow()
    private val _userLocation = MutableLiveData<UserLocation?>()
    val userLocation: MutableLiveData<UserLocation?> get() = _userLocation

    init {
        startLocationFetch()
    }

    /*
     'startLocationFetch' method is responsible for retrieving the forecast information of the last searched city
      from the 'userLocationDataStore'. This data store is updated each time the user selects a city in the search screen,
      ensuring that the most recent city information is available for display.

     Additionally, If location permissions are granted, the user's current location is fetched using FusedLocationProviderClient.
     */
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

    /*
      If permission is granted, location fetching is initiated.
      Else, a rejection permission is added to 'visiblePermissionQueue', triggering an alert with information on the permission's importance.
     */
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

    /*
       This 'fetchForecast' function initiates an API call to OpenWeatherAPI to fetch forecast information based on the provided city name.
       Upon success, if the city was manually searched by the user, its details are stored in the data store for default forecast retrieval on the next app launch.
    */
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