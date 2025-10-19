package com.magicalwardrobe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.magicalwardrobe.ui.screens.onboarding.OnboardingScreen
import com.magicalwardrobe.ui.screens.home.HomeScreen
import com.magicalwardrobe.ui.screens.camera.CameraScreen
import com.magicalwardrobe.ui.screens.wardrobe.WardrobeScreen
import com.magicalwardrobe.ui.screens.profile.ProfileScreen
import com.magicalwardrobe.ui.screens.result.ResultScreen
import com.magicalwardrobe.ui.screens.settings.SettingsScreen

@Composable
fun MagicalWardrobeNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Camera.route) {
            CameraScreen(navController = navController)
        }
        composable(Screen.Wardrobe.route) {
            WardrobeScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Result.route) {
            ResultScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}