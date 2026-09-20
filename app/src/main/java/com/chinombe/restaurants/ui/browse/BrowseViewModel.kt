package com.chinombe.restaurants.ui.browse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinombe.restaurants.data.remote.DemoControls
import com.chinombe.restaurants.di.DefaultDispatcher
import com.chinombe.restaurants.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BrowseViewModel
@Inject
constructor(
    private val repository: RestaurantRepository,
    private val settings: DemoControls,
    private val savedState: SavedStateHandle,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val query = savedState.getStateFlow("query", "")
    private val open = savedState.getStateFlow("open", false)
    private val refreshing = MutableStateFlow(true)
    private val failed = MutableStateFlow(false)
    private val favoriteError = MutableStateFlow<String?>(null)
    private var refreshJob: Job? = null
    private val content =
        combine(repository.observeRestaurants(), query, open, refreshing, failed, ::browseState)
    val state =
        combine(content, settings.failure, favoriteError) { content, failure, error ->
                content.copy(simulateFailure = failure, favoriteError = error)
            }
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BrowseUiState())

    init {
        refresh()
    }

    fun onEvent(event: BrowseEvent) {
        when (event) {
            is BrowseEvent.SearchChanged -> savedState["query"] = event.query
            is BrowseEvent.OpenOnlyChanged -> savedState["open"] = event.enabled
            is BrowseEvent.FailureChanged -> settings.setFailure(event.enabled)
            is BrowseEvent.FavoriteChanged ->
                viewModelScope.launch {
                    try {
                        repository.setFavorite(event.id, event.favorite)
                        favoriteError.value = null
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        favoriteError.value = "Couldn’t save your favorite. Please try again."
                    }
                }
            BrowseEvent.Retry -> refresh()
        }
    }

    private fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshing.value = true
            failed.value = false
            try {
                failed.value = repository.refreshRestaurants().isFailure
            } finally {
                refreshing.value = false
            }
        }
    }
}
