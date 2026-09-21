package com.chinombe.restaurants

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.chinombe.restaurants.domain.model.Restaurant
import com.chinombe.restaurants.domain.repository.RestaurantRepository
import com.chinombe.restaurants.ui.details.DetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    @Test
    fun missingBlankAndUnknownIdsShowUnavailable() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModels = ViewModelStore()
        val repository = object : RestaurantRepository {
            override fun observeRestaurants() = flowOf(emptyList<Restaurant>())
            override fun observeRestaurant(id: String) = flowOf<Restaurant?>(null)
            override suspend fun refreshRestaurants() = Result.success(Unit)
            override suspend fun setFavorite(id: String, favorite: Boolean) = Unit
        }
        try {
            for (id in listOf(null, "", " ", "unknown")) {
                val savedState = SavedStateHandle()
                if (id != null) savedState["restaurantId"] = id
                val viewModel = DetailsViewModel(repository, savedState)
                viewModels.put("details-$id", viewModel)
                val collection = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.state.collect()
                }
                runCurrent()
                assertFalse(viewModel.state.value.loading)
                assertNull(viewModel.state.value.restaurant)
                collection.cancel()
            }
        } finally {
            viewModels.clear()
            runCurrent()
            Dispatchers.resetMain()
        }
    }
}
