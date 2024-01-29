package com.example.weather.repository

import com.example.weather.models.ForeCastModel
import com.example.weather.response.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeatherRepositoryImplementation @Inject constructor(private val weatherApi: ApiInterface) :
    WeatherRepository {
    override suspend fun getWeatherForeCast(cityName: String): Flow<Response<ForeCastModel>> = flow {
        emit(Response.Loading())
        try {
            val response = weatherApi.getForecast(cityName = cityName)
            emit(Response.Success(response))
        } catch (e: Exception) {
            emit(Response.Error("Something Went Wrong"))
        }
    }
}