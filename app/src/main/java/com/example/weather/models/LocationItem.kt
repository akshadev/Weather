package com.example.weather.models

data class LocationItem(
    val id: Int,
    val name: String,
    val state: String,
    val country: String,
    val coordinates: Coord
)
