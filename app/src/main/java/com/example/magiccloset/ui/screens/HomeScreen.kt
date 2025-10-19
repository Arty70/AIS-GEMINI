package com.example.magiccloset.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.magiccloset.ui.navigation.Routes

@Composable
fun HomeScreen(nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Magic Closet") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { nav.navigate(Routes.Editor.route) }) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text("  Try a new outfit")
            }
            Button(onClick = { nav.navigate(Routes.Gallery.route) }) {
                Icon(Icons.Default.Collections, contentDescription = null)
                Text("  Gallery")
            }
            Button(onClick = { /* future: presets */ }) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Text("  Style presets")
            }
        }
    }
}
