package com.example.weather.repository


import com.example.weather.models.ForeCastModel
import com.example.weather.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    //https://api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}
    @GET("/data/2.5/weather")
    suspend fun getForecast(
        @Query("q") cityName: String = "New York",
        @Query("appid") apiKey: String = Constants.API_KEY
    ): ForeCastModel
}