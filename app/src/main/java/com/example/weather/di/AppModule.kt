package com.example.weather.di

import android.content.Context
import androidx.compose.ui.unit.Constraints
import com.example.weather.repository.ApiInterface
import com.example.weather.repository.WeatherRepository
import com.example.weather.repository.WeatherRepositoryImplementation
import com.example.weather.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object AppModule {
    @Singleton
    @Provides
    fun getWeatherRepository(weatherAPI: ApiInterface): WeatherRepository =
        WeatherRepositoryImplementation(weatherAPI)

    @Singleton
    @Provides
    fun provideWeatherAPIRetrofitClient(): ApiInterface {
        return Retrofit.Builder().baseUrl(Constants.baseUrl)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiInterface::class.java)
    }
}