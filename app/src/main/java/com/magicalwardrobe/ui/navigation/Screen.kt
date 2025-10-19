package com.magicalwardrobe.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Wardrobe : Screen("wardrobe")
    object Profile : Screen("profile")
    object Result : Screen("result")
}