package com.example.weather.ui.theme.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.weather.navigation.Route
import com.example.weather.navigation.Navigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryView() {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(navController.currentDestination?.route) }
    var showBackIcon by remember {
        mutableStateOf(false)
    }
    var showAddIcon by remember {
        mutableStateOf(true)
    }
    DisposableEffect(navController) {

        val listener = NavController.OnDestinationChangedListener { _, _, _ ->
            currentRoute = navController.currentDestination?.route
            showBackIcon = when (currentRoute) {
                Route.WeatherForecastScreen.path -> false
                else -> true
            }
            showAddIcon = !showBackIcon
        }
        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = "Weather",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (showBackIcon) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back Arrow Button"
                            )
                        }
                    }
                },
                actions = {
                    if (showAddIcon) {
                        IconButton(onClick = { // TODO: add routing to search location
                            }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Location Button"
                            )
                        }
                    }
                }
            )
        }
    ) {
        Navigation(navController = navController, modifier = Modifier.padding(it))
    }
}