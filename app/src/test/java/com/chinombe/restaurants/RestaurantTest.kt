package com.chinombe.restaurants

import com.chinombe.restaurants.data.mapper.toEntity
import com.chinombe.restaurants.data.remote.RestaurantDto
import com.chinombe.restaurants.domain.model.Restaurant
import com.chinombe.restaurants.domain.model.filterRestaurants
import com.chinombe.restaurants.ui.browse.browseState
import com.chinombe.restaurants.ui.components.formatEta
import com.chinombe.restaurants.ui.components.formatFee
import com.chinombe.restaurants.ui.components.formatRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestaurantTest {
    @Test
    fun bundledDatasetContains200ValidUniqueRestaurants() {
        val rows =
            kotlinx.serialization.json.Json.decodeFromString<List<RestaurantDto>>(
                java.io.File("src/main/assets/restaurants.json").readText()
            )
        assertEquals(200, rows.size)
        val entities = rows.map { it.toEntity() }
        assertEquals(200, entities.map { it.id }.distinct().size)
        assertTrue(entities.any { it.rating == null })
        assertTrue(entities.any { !it.isOpen })
    }

    private val items =
        listOf(
            Restaurant("1", "Basil Street", isOpen = true),
            Restaurant("2", "Basil Kitchen", isOpen = false),
        )

    @Test
    fun searchTrimsAndIgnoresCase() {
        assertEquals(2, filterRestaurants(items, " BASIL ", false).size)
    }

    @Test
    fun openFilterCombinesWithSearch() {
        assertEquals(listOf(items[0]), filterRestaurants(items, "basil", true))
        assertTrue(filterRestaurants(items, "pizza", true).isEmpty())
    }

    @Test
    fun mappingHandlesNullsAndInvalidOptionalValues() {
        val row =
            RestaurantDto("1", " Name ", rating = 9.0, deliveryFeeCents = -1, etaMinutes = -2)
                .toEntity()
        assertEquals("Name", row.name)
        assertTrue(row.cuisines.isEmpty())
        assertNull(row.rating)
        assertNull(row.deliveryFeeCents)
        assertNull(row.etaMinutes)
        assertFalse(row.isOpen)
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingIdentityRejected() {
        RestaurantDto("", "Name").toEntity()
    }

    @Test
    fun formattingDoesNotInventMissingData() {
        assertEquals("Free delivery", formatFee(0))
        assertEquals("R 19.99 delivery", formatFee(1999))
        assertEquals("Fee unavailable", formatFee(null))
        assertEquals("Unrated", formatRating(null))
        assertEquals("Time unavailable", formatEta(null))
    }

    @Test
    fun filteredEmptyCacheStillShowsOfflineBanner() {
        val state = browseState(items, "unmatched", false, false, true)
        assertTrue(state.hasCache)
        assertTrue(state.restaurants.isEmpty())
        assertEquals("Couldn’t refresh. Showing saved restaurants.", state.errorMessage)
    }

    @Test
    fun initialFailureAndLoadingAreDistinct() {
        assertTrue(browseState(emptyList(), "", false, true, false).isRefreshing)
        val error = browseState(emptyList(), "", false, false, true)
        assertFalse(error.hasCache)
        assertNotNull(error.errorMessage)
    }
}
