package com.chinombe.restaurants

import com.chinombe.restaurants.data.local.FavoriteRestaurantEntity
import com.chinombe.restaurants.data.local.RestaurantDao
import com.chinombe.restaurants.data.local.RestaurantEntity
import com.chinombe.restaurants.data.local.RestaurantWithFavorite
import com.chinombe.restaurants.data.remote.RestaurantApi
import com.chinombe.restaurants.data.remote.RestaurantDto
import com.chinombe.restaurants.data.repository.OfflineFirstRestaurantRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Test

class RefreshCancellationTest {
    @Test
    fun cancellationIsNotConvertedIntoOfflineFailure() = runTest {
        val api =
            object : RestaurantApi {
                override suspend fun getRestaurants(): List<RestaurantDto> =
                    throw CancellationException("cancelled")
            }
        val dao =
            object : RestaurantDao() {
                override fun observeAll() = flowOf(emptyList<RestaurantWithFavorite>())

                override fun observeById(id: String) = flowOf(null)

                override suspend fun insertAll(items: List<RestaurantEntity>) = Unit

                override suspend fun deleteRestaurants() = Unit

                override suspend fun addFavorite(item: FavoriteRestaurantEntity) = Unit

                override suspend fun removeFavorite(id: String) = Unit
            }
        val repository = OfflineFirstRestaurantRepository(api, dao)
        try {
            repository.refreshRestaurants()
            fail("Expected cancellation")
        } catch (_: CancellationException) {}
    }
}
