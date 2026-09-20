package com.chinombe.restaurants.ui.browse

import com.chinombe.restaurants.domain.model.Restaurant
import com.chinombe.restaurants.domain.model.filterRestaurants

data class BrowseUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val searchQuery: String = "",
    val openOnly: Boolean = false,
    val isRefreshing: Boolean = true,
    val hasCache: Boolean = false,
    val errorMessage: String? = null,
    val simulateFailure: Boolean = false,
    val favoriteError: String? = null,
)

sealed interface BrowseEvent {
    data class SearchChanged(val query: String) : BrowseEvent

    data class OpenOnlyChanged(val enabled: Boolean) : BrowseEvent

    data class FavoriteChanged(val id: String, val favorite: Boolean) : BrowseEvent

    data class FailureChanged(val enabled: Boolean) : BrowseEvent

    data object Retry : BrowseEvent
}

fun browseState(
    items: List<Restaurant>,
    query: String,
    open: Boolean,
    refreshing: Boolean,
    failed: Boolean,
): BrowseUiState {
    // No search matches doesn't mean we have no cached restaurants.
    val hasCache = items.isNotEmpty()
    val errorMessage =
        when {
            !failed -> null
            hasCache -> "You’re offline. Showing saved restaurants."
            else -> "Couldn’t load restaurants. Please try again."
        }

    return BrowseUiState(
        restaurants = filterRestaurants(items, query, open),
        searchQuery = query,
        openOnly = open,
        isRefreshing = refreshing,
        hasCache = hasCache,
        errorMessage = errorMessage,
    )
}
