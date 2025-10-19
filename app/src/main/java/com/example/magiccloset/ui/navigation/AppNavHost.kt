package com.example.magiccloset.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.magiccloset.ui.screens.EditorScreen
import com.example.magiccloset.ui.screens.GalleryScreen
import com.example.magiccloset.ui.screens.HomeScreen

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Editor : Routes("editor")
    data object Gallery : Routes("gallery")
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.Home.route) {
        composable(Routes.Home.route) { HomeScreen(navController) }
        composable(Routes.Editor.route) { EditorScreen(navController) }
        composable(Routes.Gallery.route) { GalleryScreen(navController) }
    }
}
