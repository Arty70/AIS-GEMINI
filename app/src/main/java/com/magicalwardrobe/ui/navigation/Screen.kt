package com.magicalwardrobe.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Wardrobe : Screen("wardrobe")
    object Profile : Screen("profile")
    object Result : Screen("result")
    object Settings : Screen("settings")
}