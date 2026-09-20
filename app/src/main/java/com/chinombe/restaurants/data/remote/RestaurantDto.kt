package com.chinombe.restaurants.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantDto(
    val id: String,
    val name: String,
    val cuisines: List<String>? = null,
    val rating: Double? = null,
    @SerialName("delivery_fee_cents") val deliveryFeeCents: Int? = null,
    @SerialName("eta_minutes") val etaMinutes: Int? = null,
    @SerialName("is_open") val isOpen: Boolean? = null,
    @SerialName("image_url") val imageUrl: String? = null,
)
