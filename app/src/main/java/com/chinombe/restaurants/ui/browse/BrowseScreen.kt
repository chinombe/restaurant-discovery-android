package com.chinombe.restaurants.ui.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.chinombe.restaurants.BuildConfig
import com.chinombe.restaurants.ui.components.MessageContent
import com.chinombe.restaurants.ui.components.RestaurantCard

@Composable
fun BrowseScreen(
    state: BrowseUiState,
    onEvent: (BrowseEvent) -> Unit,
    onRestaurant: (String) -> Unit,
) {
    Scaffold { padding ->
        LazyColumn(
            Modifier.testTag("browseList").fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    "LOCAL TABLE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(12.dp))
                Text("Good food.\nClose to home.", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Browse restaurants around Johannesburg.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(BrowseEvent.SearchChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Search restaurants") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    shape = MaterialTheme.shapes.large,
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = state.openOnly,
                        onClick = { onEvent(BrowseEvent.OpenOnlyChanged(!state.openOnly)) },
                        label = { Text("Open now") },
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = { onEvent(BrowseEvent.Retry) },
                        enabled = !state.isRefreshing,
                    ) {
                        Text("Refresh")
                    }
                }
            }
            if (BuildConfig.DEBUG)
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Demo · Simulate failure",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Text(
                                    "Change this, then tap Refresh",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Switch(
                                modifier =
                                    Modifier.semantics {
                                        contentDescription = "Simulate data-source failure"
                                    },
                                checked = state.simulateFailure,
                                onCheckedChange = { onEvent(BrowseEvent.FailureChanged(it)) },
                            )
                        }
                    }
                }
            if (state.isRefreshing) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (state.favoriteError != null)
                item { Text(state.favoriteError, color = MaterialTheme.colorScheme.error) }
            if (state.errorMessage != null && state.hasCache)
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(state.errorMessage)
                            TextButton(
                                onClick = { onEvent(BrowseEvent.Retry) },
                                enabled = !state.isRefreshing,
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            when {
                !state.hasCache && state.isRefreshing ->
                    item {
                        MessageContent(
                            "Loading restaurants",
                            "Please wait while the list loads.",
                        )
                    }
                !state.hasCache && state.errorMessage != null ->
                    item {
                        MessageContent("Couldn’t load restaurants", state.errorMessage, "Retry") {
                            onEvent(BrowseEvent.Retry)
                        }
                    }
                state.restaurants.isEmpty() ->
                    item {
                        MessageContent(
                            "No restaurants found",
                            "Try another name or turn off Open now.",
                            "Clear filters",
                        ) {
                            onEvent(BrowseEvent.SearchChanged(""))
                            onEvent(BrowseEvent.OpenOnlyChanged(false))
                        }
                    }
                else -> {
                    item {
                        Text(
                            "${state.restaurants.size} places to explore",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    items(state.restaurants, key = { it.id }) { restaurant ->
                        RestaurantCard(
                            restaurant,
                            onClick = { onRestaurant(restaurant.id) },
                            onFavorite = {
                                onEvent(
                                    BrowseEvent.FavoriteChanged(
                                        restaurant.id,
                                        !restaurant.isFavorite,
                                    )
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
