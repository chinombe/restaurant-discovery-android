package com.chinombe.restaurants.domain.repository

import com.chinombe.restaurants.domain.model.Restaurant
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {
    fun observeRestaurants(): Flow<List<Restaurant>>

    fun observeRestaurant(id: String): Flow<Restaurant?>

    suspend fun refreshRestaurants(): Result<Unit>

    suspend fun setFavorite(id: String, favorite: Boolean)
}
