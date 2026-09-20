package com.chinombe.restaurants.data.remote

interface RestaurantApi {
    suspend fun getRestaurants(): List<RestaurantDto>
}
