package com.example.weather.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import com.example.weather.location.UserLocation
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

open class UserLocationDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user_location_preferences"
    )


    val userLocationFlow: Flow<UserLocation?> = context.dataStore.data
        .map { preferences ->
            val latitude = preferences[KEY_LATITUDE] ?: 0.0
            val longitude = preferences[KEY_LONGITUDE] ?: 0.0
            val cityName = preferences[KEY_CITY_NAME] ?: ""

            UserLocation(latitude, longitude, cityName)
        }

    suspend fun saveUserLocation(userLocation: UserLocation) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LATITUDE] = userLocation.latitude
            preferences[KEY_LONGITUDE] = userLocation.longitude
            preferences[KEY_CITY_NAME] = userLocation.cityName
        }
    }

    companion object {
        private val KEY_LATITUDE = doublePreferencesKey("latitude")
        private val KEY_LONGITUDE = doublePreferencesKey("longitude")
        private val KEY_CITY_NAME = stringPreferencesKey("city_name")
    }
}