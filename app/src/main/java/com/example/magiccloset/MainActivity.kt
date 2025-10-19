package com.example.magiccloset

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.magiccloset.ui.theme.MagicTheme
import com.example.magiccloset.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MagicTheme {
                Surface {
                    AppNavHost()
                }
            }
        }
    }
}
