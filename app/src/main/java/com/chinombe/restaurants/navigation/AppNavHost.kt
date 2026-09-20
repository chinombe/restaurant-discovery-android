package com.chinombe.restaurants.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chinombe.restaurants.ui.browse.BrowseScreen
import com.chinombe.restaurants.ui.browse.BrowseViewModel
import com.chinombe.restaurants.ui.details.DetailsScreen
import com.chinombe.restaurants.ui.details.DetailsViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "browse") {
        composable("browse") {
            val viewModel: BrowseViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            BrowseScreen(state, viewModel::onEvent) {
                navController.navigate("restaurant/${Uri.encode(it)}") { launchSingleTop = true }
            }
        }
        composable(
            "restaurant/{restaurantId}",
            arguments = listOf(navArgument("restaurantId") { type = NavType.StringType }),
        ) {
            val viewModel: DetailsViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            DetailsScreen(state, { navController.popBackStack() }, viewModel::setFavorite)
        }
    }
}
