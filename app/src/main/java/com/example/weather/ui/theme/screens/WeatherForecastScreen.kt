package com.example.weather.ui.theme.screens

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.weather.R
import com.example.weather.models.ForeCastModel
import com.example.weather.models.Main
import com.example.weather.openAppSettings
import com.example.weather.utils.MetricConverter
import com.example.weather.viewmodels.WeatherForeCastViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherForecastScreen(
    viewModel: WeatherForeCastViewModel = hiltViewModel(),
    city: String? = null,
    navigateToLocationSelection: () -> Unit
) {
    val context = LocalContext.current
    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsToRequest.forEach { permission ->
                viewModel.onPermissionResult(
                    permission = permission, isGranted = perms[permission] == true
                )
            }
        })

    val state = viewModel.forecastScreenState.collectAsState().value
    val dialogQueue = viewModel.visiblePermissionQueue

    dialogQueue.reversed().forEach { permission ->
        PermissionDialog(
            permissionTextProvider = when (permission) {
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION -> {
                    LocationPermissionTextProvider()
                }

                else -> return@forEach
            }, isPermanentlyDeclined = !ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity, permission
            ), onDismiss = viewModel::dismissDialog, onOkClick = {
                viewModel.dismissDialog()
                multiplePermissionResultLauncher.launch(permissionsToRequest)

            }, onGoToAppSettingsClick = (context)::openAppSettings
        )
    }


    LaunchedEffect(key1 = Unit) {
        if (city == null) {
            multiplePermissionResultLauncher.launch(permissionsToRequest)
        } else {
            viewModel.fetchLocationForCity(city)
        }
    }

    Box {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.bg),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CurrentLocationContainer(state.currentLocationForecast, navigateToLocationSelection)
            SearchedLocationContainer(state.lastSearchLocationForecast, navigateToLocationSelection)
        }
    }

}

/*
    The 'CurrentLocationContainer' composable is designed to display the forecast of the user's current location.
    If permission is granted, forecast info of current location wil be shown; otherwise, a static text is shown.
 */
@Composable
fun CurrentLocationContainer(
    currentLocationForecast: ForeCastModel?, navigateToLocationSelection: () -> Unit
) {
    if (currentLocationForecast == null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)

        ) {
            Text(text = "Location Permissions are needed")
        }
    } else {
        WeatherDetailsScreen(currentLocationForecast, navigateToLocationSelection, false)
    }
}

/*
        The 'SearchedLocationContainer' composable is employed to showcase the forecast of the user's searched location.
        If the user has any searched location, the forecast for that city is displayed; otherwise, a button is shown to navigate to the search screen.
 */
@Composable
fun SearchedLocationContainer(
    lastSearchLocationForecast: ForeCastModel?, navigateToLocationSelection: () -> Unit
) {
    if (lastSearchLocationForecast == null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)

        ) {
            Button(onClick = {
                navigateToLocationSelection()
            }) {
                Text(text = "Search Location Manually")
            }
        }
    } else {
        WeatherDetailsScreen(
            lastSearchLocationForecast, navigateToLocationSelection, isSearchedLoc = true
        )
    }
}

@Composable
fun WeatherDetailsScreen(
    forecast: ForeCastModel,
    navigateToLocationSelection: () -> Unit,
    isSearchedLoc: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .clickable {
            if (isSearchedLoc) {
                navigateToLocationSelection()
            }
        }
        .padding(16.dp)
        .fillMaxWidth()
        .background(Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
        .padding(16.dp)

    ) {
        Text(
            text = if (!isSearchedLoc) "Current Location" else "Last Search",
            color = Color.Cyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        forecast.weather?.firstOrNull()?.icon?.let {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://openweathermap.org/img/wn/${it}@2x.png").crossfade(true).build(),
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(80.dp)

            )
        }
        Text(
            text = forecast.name ?: "",

            style = TextStyle(
                fontSize = 24.sp, color = Color.White, fontFamily = FontFamily.Monospace
            )
        )
        val temparature =
            forecast.main?.temp?.let { String.format("%.1f", MetricConverter.kelvinToFahrenheit(it)) }
        Text(
            text = " Temp: $temparature°C", color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Blue.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            forecast.main?.humidity?.let {
                ForeCastInformation("Humidity", "$it%")
            }

            forecast.main?.pressure?.let {
                ForeCastInformation("Pressure", "$it")
            }

            forecast.main?.temp_max?.let {
                ForeCastInformation("High", "${String.format("%.1f", MetricConverter.kelvinToFahrenheit(it))}°C")
            }

            forecast.main?.temp_min?.let {
                ForeCastInformation("Low", "${String.format("%.1f", MetricConverter.kelvinToFahrenheit(it))}°C")
            }
        }
    }
}

@Composable
fun ForeCastInformation(title: String, value: String) {
    Column(Modifier.padding(8.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.sun),
            contentDescription = "sun icon",
            tint = Color.White
        )
        Text(
            text = title, color = Color.White, fontSize = 8.sp
        )
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
fun DetailsScreen() {
    WeatherDetailsScreen(
        ForeCastModel(name = "Location", main = Main(temp = 10.0)), {}, true
    )

}