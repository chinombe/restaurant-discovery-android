package com.chinombe.restaurants.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.chinombe.restaurants.domain.model.Restaurant

@Composable
fun RestaurantImage(restaurant: Restaurant, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = restaurant.imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        loading = { ImageFallback() },
        error = { ImageFallback() },
    )
}

@Composable
private fun ImageFallback() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            Icons.Outlined.Restaurant,
            null,
            Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun FavoriteButton(restaurant: Restaurant, onFavorite: () -> Unit) {
    IconToggleButton(
        checked = restaurant.isFavorite,
        onCheckedChange = { onFavorite() },
        modifier =
            Modifier.semantics {
                contentDescription = "Favorite ${restaurant.name}"
                stateDescription = if (restaurant.isFavorite) "Saved" else "Not saved"
            },
    ) {
        Icon(
            if (restaurant.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun RestaurantFacts(restaurant: Restaurant) {
    Text(
        restaurant.cuisines.joinToString(" · ").ifEmpty { "Cuisine unavailable" },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    Text(
        "★ ${formatRating(restaurant.rating)}  ·  ${formatEta(restaurant.etaMinutes)}",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.height(6.dp))
    Text(formatFee(restaurant.deliveryFeeCents), style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit, onFavorite: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box {
            RestaurantImage(restaurant, Modifier.fillMaxWidth().height(160.dp))
            Surface(
                Modifier.align(Alignment.TopStart).padding(12.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Text(
                    if (restaurant.isOpen) "Open now" else "Closed",
                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    restaurant.name,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                FavoriteButton(restaurant, onFavorite)
            }
            RestaurantFacts(restaurant)
        }
    }
}

@Composable
fun MessageContent(
    title: String,
    message: String,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(message, style = MaterialTheme.typography.bodyMedium)
        if (action != null) Button(onClick = onAction) { Text(action) }
    }
}
