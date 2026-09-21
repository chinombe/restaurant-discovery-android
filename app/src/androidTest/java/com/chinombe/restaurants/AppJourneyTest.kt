package com.chinombe.restaurants

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test

// Run this flow with the real database and bundled data.
class AppJourneyTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test
    fun discoveryFavoritesOfflineAndRecovery() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("demo", Context.MODE_PRIVATE).edit().clear().commit()
        context.deleteDatabase("restaurants.db")
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.waitUntil(15_000) {
                compose
                    .onAllNodesWithText("200 places to explore")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            val name = "Basil Street · Bryanston"
            compose.onNodeWithTag("browseList").performScrollToNode(hasText(name))
            compose.onNodeWithContentDescription("Favorite $name").performClick()
            compose.onNodeWithContentDescription("Favorite $name").assertIsOn()
            compose.onNodeWithText(name).performClick()
            compose.onNodeWithText("Remove from favorites").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("Remove from favorites").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText("Save to favorites").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Save to favorites").performScrollTo().assertIsDisplayed()
            compose.onNodeWithContentDescription("Back").performClick()
            compose.onNodeWithTag("browseList").performScrollToIndex(0)
            compose.onNodeWithContentDescription("Simulate data-source failure").performClick()
            compose.onNodeWithText("Refresh").performClick()
            compose.waitUntil(10_000) {
                compose
                    .onAllNodesWithText("Couldn’t refresh. Showing saved restaurants.")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            compose.onNodeWithContentDescription("Simulate data-source failure").performClick()
            compose.onNodeWithText("Retry").performScrollTo().performClick()
            compose.waitUntil(10_000) {
                compose
                    .onAllNodesWithText("Couldn’t refresh. Showing saved restaurants.")
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
            compose.onNodeWithText("Search restaurants").performTextInput("zzzz-no-match")
            compose.onNodeWithTag("browseList").performScrollToNode(hasText("No restaurants found"))
            compose.onNodeWithText("No restaurants found").assertIsDisplayed()
        }
    }
}
