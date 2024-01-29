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
            /*
                On initial routing, the city will be null. When navigating back from the search screen,
                the value is assigned to 'city'.

                 The city name is currently used to fetch the weather forecast for the specified city from the OpenWeatherMap API.
                 However, there is room for improvement. Instead of using the city name alone, consider passing the complete city object,
                 which includes latitude and longitude. This approach provides better control and allows for more accurate location identification.
            */
            val city = it.savedStateHandle.get<String>("city")
            WeatherForecastScreen(city = city) {
                navController.navigate(Route.SearchLocation.path)
            }
        }

        composable(route = Route.SearchLocation.path) {
            SearchLocationScreen { city ->
                /*
                When a city name is selected, it is stored in the savedStateHandle before navigating back to the previous screen,
                which is the WeatherForecastScreen and ensures that the WeatherForecastScreen can access
                        the chosen city data upon returning.
                */
                navController.previousBackStackEntry?.savedStateHandle?.set("city", city)
                navController.popBackStack()
            }
        }
    }
}