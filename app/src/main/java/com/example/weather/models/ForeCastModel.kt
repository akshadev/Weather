package com.example.weather.models

data class ForeCastModel(
    val base: String? = null,
    val clouds: Clouds? = null,
    val cod: Int? = null,
    val coordinates: Coord? = null,
    val dt: Int? = null,
    val id: Int? = null,
    val main: Main? = null,
    val name: String? = null,
    val sys: Sys? = null,
    val visibility: Int? = null,
    val weather: List<Weather>? = null,
    val wind: Wind? = null
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class Main(
    val humidity: Int? = null,
    val pressure: Int? = null,
    val temp: Double? = null,
    val temp_max: Double? = null,
    val temp_min: Double? = null
)

data class Sys(
    val country: String,
    val id: Int,
    val message: Double,
    val sunrise: Int,
    val sunset: Int,
    val type: Int
)

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)

data class Wind(
    val deg: Int,
    val speed: Double
)

data class Clouds(
    val all: Int
)
