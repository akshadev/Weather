package com.example.weather.models

// {"coord":{"lon":10.99,"lat":44.34},"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01n"}],"base":"stations","main":{"temp":277.18,"feels_like":277.18,"temp_min":269.56,"temp_max":280.8,"pressure":1037,"humidity":57,"sea_level":1037,"grnd_level":946},"visibility":10000,"wind":{"speed":0.87,"deg":265,"gust":0.81},"clouds":{"all":0},"dt":1706493153,"sys":{"type":2,"id":2075663,"country":"IT","sunrise":1706510295,"sunset":1706545177},"timezone":3600,"id":3163858,"name":"Zocca","cod":200}
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
