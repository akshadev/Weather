package com.example.weather.viewmodels.States

import com.example.weather.models.ForeCastModel

data class WeatherForeCastState(
    val isLoading: Boolean = false,
    val currentLocationForecast: ForeCastModel? = null,
    val lastSearchLocationForecast: ForeCastModel? = null,
    val error: String? = null
)
