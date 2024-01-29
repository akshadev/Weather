package com.example.weather.repository

import com.example.weather.models.ForeCastModel
import com.example.weather.response.Response
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeatherForeCast(cityName : String): Flow<Response<ForeCastModel>>
}