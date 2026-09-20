package com.chinombe.restaurants.data.remote

import android.content.Context
import androidx.core.content.edit
import com.chinombe.restaurants.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface DemoControls {
    val failure: kotlinx.coroutines.flow.StateFlow<Boolean>

    fun setFailure(enabled: Boolean)
}

@Singleton
class DemoSettings @Inject constructor(@ApplicationContext context: Context) : DemoControls {
    private val preferences = context.getSharedPreferences("demo", Context.MODE_PRIVATE)
    private val mutableFailure =
        MutableStateFlow(BuildConfig.DEBUG && preferences.getBoolean("failure", false))
    override val failure = mutableFailure.asStateFlow()

    override fun setFailure(enabled: Boolean) {
        mutableFailure.value = BuildConfig.DEBUG && enabled
        preferences.edit { putBoolean("failure", mutableFailure.value) }
    }
}

class MockRestaurantApi
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val settings: DemoSettings,
) : RestaurantApi {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getRestaurants(): List<RestaurantDto> =
        withContext(Dispatchers.IO) {
            // Leave enough time to see the loading state during the demo.
            delay(900)
            if (settings.failure.value) throw IOException("Simulated source failure")
            context.assets.open("restaurants.json").bufferedReader().use {
                json.decodeFromString<List<RestaurantDto>>(it.readText())
            }
        }
}
