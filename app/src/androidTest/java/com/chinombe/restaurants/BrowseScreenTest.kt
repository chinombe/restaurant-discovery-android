package com.chinombe.restaurants

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.chinombe.restaurants.domain.model.Restaurant
import com.chinombe.restaurants.ui.browse.BrowseEvent
import com.chinombe.restaurants.ui.browse.BrowseScreen
import com.chinombe.restaurants.ui.browse.BrowseUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BrowseScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun errorRetryDispatchesEvent() {
        var event: BrowseEvent? = null
        compose.setContent {
            MaterialTheme {
                BrowseScreen(
                    BrowseUiState(isRefreshing = false, errorMessage = "Couldn’t load"),
                    { event = it },
                    {},
                )
            }
        }
        compose.onNodeWithText("Retry").performScrollTo().performClick()
        assertEquals(BrowseEvent.Retry, event)
    }

    @Test
    fun unmatchedSearchOffersClearFilters() {
        compose.setContent {
            MaterialTheme {
                BrowseScreen(BrowseUiState(isRefreshing = false, searchQuery = "missing"), {}, {})
            }
        }
        compose.onNodeWithText("No restaurants found").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun cardNavigationAndFavoriteCarryStableId() {
        val restaurant = Restaurant("one", "Basil Street", listOf("Italian"), 4.5, 0, 25, true)
        var selected = ""
        var event: BrowseEvent? = null
        compose.setContent {
            MaterialTheme {
                BrowseScreen(
                    BrowseUiState(
                        restaurants = listOf(restaurant),
                        hasCache = true,
                        isRefreshing = false,
                    ),
                    { event = it },
                    { selected = it },
                )
            }
        }
        compose
            .onNodeWithContentDescription("Favorite Basil Street")
            .performScrollTo()
            .performClick()
        assertEquals(BrowseEvent.FavoriteChanged("one", true), event)
        compose.onNodeWithText("Basil Street").performClick()
        assertEquals("one", selected)
    }
}
