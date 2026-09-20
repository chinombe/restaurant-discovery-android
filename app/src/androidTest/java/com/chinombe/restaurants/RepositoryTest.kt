package com.chinombe.restaurants

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chinombe.restaurants.data.local.AppDatabase
import com.chinombe.restaurants.data.remote.RestaurantApi
import com.chinombe.restaurants.data.remote.RestaurantDto
import com.chinombe.restaurants.data.repository.OfflineFirstRestaurantRepository
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryTest {
    // TODO: Check that duplicate IDs leave the old cache untouched.

    @Test
    fun cacheAndFavoritesSurviveRefreshFailureAndDatabaseReopen() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "repository-test.db"
        context.deleteDatabase(name)
        var database = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        var fail = false
        var rows = listOf(RestaurantDto("one", "First"))
        val api =
            object : RestaurantApi {
                override suspend fun getRestaurants(): List<RestaurantDto> {
                    if (fail) throw IOException()
                    return rows
                }
            }
        try {
            var repository = OfflineFirstRestaurantRepository(api, database.restaurantDao())
            assertTrue(repository.refreshRestaurants().isSuccess)
            repository.setFavorite("one", true)
            rows = listOf(RestaurantDto("one", "Updated"))
            assertTrue(repository.refreshRestaurants().isSuccess)
            assertTrue(repository.observeRestaurant("one").first()!!.isFavorite)
            fail = true
            assertTrue(repository.refreshRestaurants().isFailure)
            assertEquals("Updated", repository.observeRestaurants().first().single().name)
            database.close()
            database = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            repository = OfflineFirstRestaurantRepository(api, database.restaurantDao())
            assertTrue(repository.observeRestaurants().first().single().isFavorite)
            repository.setFavorite("one", false)
            assertFalse(repository.observeRestaurant("one").first()!!.isFavorite)
            fail = false
            rows = listOf(RestaurantDto("", "Invalid"))
            assertTrue(repository.refreshRestaurants().isFailure)
            assertEquals("Updated", repository.observeRestaurants().first().single().name)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
