package com.example.weather.utils

object MetricConverter {
    fun kelvinToFahrenheit(kelvin: Double): Double {
        val celsius = kelvin - 273.15
        return (celsius * 9/5) + 32
    }
}