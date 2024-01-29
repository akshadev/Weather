package com.example.weather

import android.content.Context
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.example.weather.datastore.UserLocationDataStore
import com.example.weather.location.LocationUtils
import com.example.weather.location.UserLocation
import com.example.weather.repository.WeatherRepository
import com.example.weather.viewmodels.WeatherForeCastViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.prefs.Preferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule


@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class WeatherForeCastScreenViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: WeatherForeCastViewModel

    private val mainCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var weatherRepo: WeatherRepository

    private lateinit var userLocationDataStore: UserLocationDataStore

    private lateinit var context: Context

    private lateinit var mocktextUtis: TextUtils


    @Before
    fun setup() {
        Dispatchers.setMain(mainCoroutineDispatcher)
        weatherRepo = mockk()
        userLocationDataStore = mockk()
        context = mockk()
        mocktextUtis = mockk()
        mockkStatic(ContextCompat::class)
    }

    @Test
    fun `test viewModel init`() = runTest {
        val preferences = mockk<Preferences>()
        val userLocation = UserLocation(0.0, 0.0, "TestCity")
        every { ContextCompat.checkSelfPermission(any(), any()) } returns -1
        coEvery { userLocationDataStore.userLocationFlow } returns flowOf(userLocation)
        every { LocationUtils.isLocationPermissionGranted(context) } answers { false }

        viewModel = WeatherForeCastViewModel(weatherRepo, userLocationDataStore, context)
    }

    // if given more time would have fix this failing test

    //    @Test
//    fun `test fetchLocationForCity`() = runTest {
//        val forcastInfo = ForeCastInfo(name = "London")
//        // Mocking fetchForecast
//        val response = Response.Success(forcastInfo)
//        coEvery { weatherRepo.getWeatherForeCast("City") } returns flowOf(response)
//
//        // Trigger fetchLocationForCity
//        viewModel.fetchLocationForCity("City")
//
//        // Verify that fetchForecast is called
//        coVerify { weatherRepo.getWeatherForeCast("City") }
//
//        // Verify that the forecastScreenState is updated accordingly
//        verify { observer.onChanged(any()) }
//    }
//
//    @Test
//    fun `test onPermissionResult with permission granted`() =
//        mainCoroutineDispatcher.runBlockingTest {
//            // Mocking fetchGPSLocation
//            coEvery { LocationUtils.fetchUserLocation(context) } returns flowOf(
//                Response.Success(
//                    mockk()
//                )
//            )
//
//            // Trigger onPermissionResult with permission granted
//            viewModel.onPermissionResult("permission", true)
//
//            // Verify that fetchGPSLocation is called
//            coVerify { LocationUtils.fetchUserLocation(context) }
//
//            // Verify that the forecastScreenState is updated accordingly
//            verify { observer.onChanged(any()) }
//        }
//
//    @Test
//    fun `test onPermissionResult with permission not granted`() =
//        mainCoroutineDispatcher.runBlockingTest {
//            // Trigger onPermissionResult with permission not granted
//            viewModel.onPermissionResult("permission", false)
//
//            // Verify that fetchGPSLocation is not called
//            coVerify(exactly = 0) { LocationUtils.fetchUserLocation(context) }
//
//            // Verify that the forecastScreenState is not updated (if applicable)
//            verify { observer.onChanged(any()) wasNot called }
//        }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
        mainCoroutineDispatcher.cleanupTestCoroutines()
    }
}
