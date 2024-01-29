package com.example.weather.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weather.ui.theme.screens.SearchLocationScreen
import com.example.weather.ui.theme.screens.WeatherForecastScreen

@Composable
fun Navigation(navController: NavHostController, modifier: Modifier) {

    NavHost(
        navController = navController,
        startDestination = Route.WeatherForecastScreen.path,
        modifier = modifier
    ) {

        composable(
            route = Route.WeatherForecastScreen.path
        ) {

            val city = it.savedStateHandle.get<String>("city")
            WeatherForecastScreen()
        }
    }

}