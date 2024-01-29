package com.example.weather.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.weather.response.Response
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

object LocationUtils {
    /*
        The 'getAddressFromLocation' function, given a 'Context', latitude, and longitude,
        utilizes the Geocoder API to fetch the location address corresponding to the provided latitude and longitude.

        Improvement scope: Geocoder.GeocodeListener should have been used
     */
    fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        var cityName: String? = null

        try {
            geocoder.getFromLocation(latitude, longitude, 1)
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                // Extract the city name
                cityName = address.locality
                // You can also use address.subLocality, address.adminArea, etc. based on your requirements
            }
        } catch (e: IOException) {
            cityName = null
            e.printStackTrace()
        }

        return cityName
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        val hasFineLocationPermission: Boolean =
            checkLocationPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission: Boolean =
            checkLocationPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return hasCoarseLocationPermission && hasFineLocationPermission
    }

    private fun checkLocationPermission(context: Context, permission: String): Boolean {
        val permissionStatus = ContextCompat.checkSelfPermission(context, permission)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun promptUserToEnableLocation(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    fun fetchUserLocation(context: Context): Flow<Response<UserLocation>> = callbackFlow {
        send(Response.Loading())
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val userLocation = UserLocation(it.latitude, it.longitude)
                        launch {
                            send(Response.Success(userLocation))
                        }
                    }
                }.addOnFailureListener {
                    launch {
                        send(Response.Error("Missing Premissions"))
                    }
                }
        } else {
            send(Response.Error("Missing Premissions"))
        }

        awaitClose { }
    }
}