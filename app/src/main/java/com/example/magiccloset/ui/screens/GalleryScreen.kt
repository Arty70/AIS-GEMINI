package com.example.magiccloset.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun GalleryScreen(nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Gallery") }) }) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140),
            modifier = Modifier.fillMaxSize()
        ) {
            items(listOf<String>()) { _ ->
                // placeholder
            }
        }
    }
}
