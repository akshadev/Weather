package com.example.weather.navigation

sealed class Route(val path: String) {
    object WeatherForecastScreen : Route("Home")
    object SearchLocation : Route("Search")
}