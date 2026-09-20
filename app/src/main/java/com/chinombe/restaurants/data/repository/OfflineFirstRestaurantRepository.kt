package com.chinombe.restaurants.data.repository

import com.chinombe.restaurants.data.local.FavoriteRestaurantEntity
import com.chinombe.restaurants.data.local.RestaurantDao
import com.chinombe.restaurants.data.mapper.toDomain
import com.chinombe.restaurants.data.mapper.toEntity
import com.chinombe.restaurants.data.remote.RestaurantApi
import com.chinombe.restaurants.domain.repository.RestaurantRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OfflineFirstRestaurantRepository
@Inject
constructor(private val api: RestaurantApi, private val dao: RestaurantDao) : RestaurantRepository {
    private val refreshMutex = Mutex()

    override fun observeRestaurants() = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeRestaurant(id: String) = dao.observeById(id).map { it?.toDomain() }

    override suspend fun refreshRestaurants(): Result<Unit> = refreshMutex.withLock {
        try {
            // Check the whole response before touching the saved list.
            val rows = api.getRestaurants().map { it.toEntity() }
            require(rows.map { it.id }.distinct().size == rows.size) { "Duplicate restaurant IDs" }
            dao.replaceRestaurants(rows)
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            // Cancellation is not a failed request.
            throw cancelled
        } catch (failure: Exception) {
            Result.failure(failure)
        }
    }

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        if (favorite) dao.addFavorite(FavoriteRestaurantEntity(id)) else dao.removeFavorite(id)
    }
}
