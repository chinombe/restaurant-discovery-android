package com.chinombe.restaurants.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chinombe.restaurants.domain.model.Restaurant
import com.chinombe.restaurants.ui.components.MessageContent
import com.chinombe.restaurants.ui.components.RestaurantFacts
import com.chinombe.restaurants.ui.components.RestaurantImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(state: DetailsUiState, onBack: () -> Unit, onFavorite: (Restaurant) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurant details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            val restaurant = state.restaurant
            when {
                state.loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                restaurant == null ->
                    MessageContent(
                        "Restaurant unavailable",
                        "This restaurant is no longer in your saved list.",
                        "Back to browse",
                        onBack,
                    )
                else -> {
                    RestaurantImage(restaurant, Modifier.fillMaxWidth().height(280.dp))
                    Column(
                        Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            if (restaurant.isOpen) "OPEN NOW" else "CURRENTLY CLOSED",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(restaurant.name, style = MaterialTheme.typography.headlineLarge)
                        RestaurantFacts(restaurant)
                        HorizontalDivider()
                        Button(
                            onClick = { onFavorite(restaurant) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (restaurant.isFavorite) "Remove from favorites"
                                else "Save to favorites"
                            )
                        }
                        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Text(
                            "Saved favorites",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "Favorites stay saved on this device, even when you’re offline.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
