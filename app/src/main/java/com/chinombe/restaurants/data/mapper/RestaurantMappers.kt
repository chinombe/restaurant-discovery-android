package com.chinombe.restaurants.data.mapper

import com.chinombe.restaurants.data.local.RestaurantEntity
import com.chinombe.restaurants.data.local.RestaurantWithFavorite
import com.chinombe.restaurants.data.remote.RestaurantDto
import com.chinombe.restaurants.domain.model.Restaurant

fun RestaurantDto.toEntity(): RestaurantEntity {
    require(id.isNotBlank() && name.isNotBlank()) { "Restaurant identity is required" }

    return RestaurantEntity(
        id = id.trim(),
        name = name.trim(),
        cuisines = cuisines.orEmpty().filter { it.isNotBlank() },
        rating = rating?.takeIf { it.isFinite() && it in 0.0..5.0 },
        deliveryFeeCents = deliveryFeeCents?.takeIf { it >= 0 },
        etaMinutes = etaMinutes?.takeIf { it > 0 },
        isOpen = isOpen ?: false,
        imageUrl = imageUrl?.takeIf { it.startsWith("https://") },
    )
}

fun RestaurantWithFavorite.toDomain(): Restaurant {
    return Restaurant(
        id = restaurant.id,
        name = restaurant.name,
        cuisines = restaurant.cuisines,
        rating = restaurant.rating,
        deliveryFeeCents = restaurant.deliveryFeeCents,
        etaMinutes = restaurant.etaMinutes,
        isOpen = restaurant.isOpen,
        imageUrl = restaurant.imageUrl,
        isFavorite = isFavorite,
    )
}
