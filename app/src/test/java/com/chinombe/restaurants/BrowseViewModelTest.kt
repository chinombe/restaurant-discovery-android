package com.chinombe.restaurants

import androidx.lifecycle.SavedStateHandle
import com.chinombe.restaurants.data.remote.DemoControls
import com.chinombe.restaurants.domain.model.Restaurant
import com.chinombe.restaurants.domain.repository.RestaurantRepository
import com.chinombe.restaurants.ui.browse.BrowseEvent
import com.chinombe.restaurants.ui.browse.BrowseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModelTest {
    @Test
    fun loadingContentFiltersFailureAndRecovery() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val rows = MutableStateFlow(emptyList<Restaurant>())
            var fail = false
            val repository =
                object : RestaurantRepository {
                    override fun observeRestaurants() = rows

                    override fun observeRestaurant(id: String) = rows.map { list ->
                        list.find { it.id == id }
                    }

                    override suspend fun refreshRestaurants(): Result<Unit> {
                        delay(100)
                        if (fail) return Result.failure(java.io.IOException())
                        rows.value =
                            listOf(
                                Restaurant("one", "Basil Street", isOpen = true),
                                Restaurant("two", "Closed Place"),
                            )
                        return Result.success(Unit)
                    }

                    override suspend fun setFavorite(id: String, favorite: Boolean) {
                        rows.value =
                            rows.value.map {
                                if (it.id == id) it.copy(isFavorite = favorite) else it
                            }
                    }
                }
            val settings =
                object : DemoControls {
                    override val failure = MutableStateFlow(false)

                    override fun setFailure(enabled: Boolean) {
                        failure.value = enabled
                    }
                }
            val viewModel = BrowseViewModel(repository, settings, SavedStateHandle(), dispatcher)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            assertTrue(viewModel.state.value.isRefreshing)
            advanceUntilIdle()
            assertEquals(2, viewModel.state.value.restaurants.size)
            viewModel.onEvent(BrowseEvent.OpenOnlyChanged(true))
            runCurrent()
            assertEquals(1, viewModel.state.value.restaurants.size)
            viewModel.onEvent(BrowseEvent.SearchChanged("missing"))
            runCurrent()
            assertTrue(viewModel.state.value.restaurants.isEmpty())
            assertTrue(viewModel.state.value.hasCache)
            fail = true
            viewModel.onEvent(BrowseEvent.Retry)
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.errorMessage)
            assertTrue(viewModel.state.value.hasCache)
            viewModel.onEvent(BrowseEvent.SearchChanged(""))
            runCurrent()
            viewModel.onEvent(BrowseEvent.FavoriteChanged("one", true))
            runCurrent()
            assertTrue(viewModel.state.value.restaurants.single().isFavorite)
            fail = false
            viewModel.onEvent(BrowseEvent.Retry)
            advanceUntilIdle()
            assertNull(viewModel.state.value.errorMessage)
            rows.value = emptyList()
            fail = true
            viewModel.onEvent(BrowseEvent.Retry)
            advanceUntilIdle()
            assertFalse(viewModel.state.value.hasCache)
            assertNotNull(viewModel.state.value.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
