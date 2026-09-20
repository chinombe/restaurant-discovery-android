package com.chinombe.restaurants.domain.model

data class Restaurant(
    val id: String,
    val name: String,
    val cuisines: List<String> = emptyList(),
    val rating: Double? = null,
    val deliveryFeeCents: Int? = null,
    val etaMinutes: Int? = null,
    val isOpen: Boolean = false,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false,
)

fun filterRestaurants(items: List<Restaurant>, query: String, openOnly: Boolean) = items.filter {
    it.name.contains(query.trim(), ignoreCase = true) && (!openOnly || it.isOpen)
}
