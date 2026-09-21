package com.chinombe.restaurants.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinombe.restaurants.domain.model.Restaurant
import com.chinombe.restaurants.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailsUiState(
    val restaurant: Restaurant? = null,
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class DetailsViewModel
@Inject
constructor(private val repository: RestaurantRepository, savedState: SavedStateHandle) :
    ViewModel() {
    private val error = MutableStateFlow<String?>(null)
    private val restaurantId = savedState.get<String>("restaurantId")
    private val restaurant =
        restaurantId?.takeIf { it.isNotBlank() }?.let(repository::observeRestaurant) ?: flowOf(null)
    val state =
        combine(restaurant, error) { restaurant, error ->
            DetailsUiState(restaurant, false, error)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUiState())

    fun setFavorite(restaurant: Restaurant) {
        viewModelScope.launch {
            try {
                repository.setFavorite(restaurant.id, !restaurant.isFavorite)
                error.value = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                error.value = "Couldn’t save your favorite. Please try again."
            }
        }
    }
}
