package com.chinombe.restaurants.ui.components

import java.util.Locale

fun formatFee(cents: Int?): String =
    when {
        cents == null || cents < 0 -> "Fee unavailable"
        cents == 0 -> "Free delivery"
        else -> "R %.2f delivery".format(Locale.US, cents / 100.0)
    }

fun formatRating(rating: Double?): String =
    rating?.let { "%.1f".format(Locale.US, it) } ?: "Unrated"

fun formatEta(minutes: Int?): String = minutes?.let { "$it min" } ?: "Time unavailable"
