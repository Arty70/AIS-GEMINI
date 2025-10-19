package com.magicalwardrobe.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.magicalwardrobe.ui.components.GradientButton
import com.magicalwardrobe.ui.theme.*

@Composable
fun OnboardingScreen(
    navController: NavController
) {
    var currentStep by remember { mutableStateOf(0) }
    
    val steps = listOf(
        OnboardingStep(
            title = "Добро пожаловать в Волшебный Гардероб!",
            description = "Создавайте невероятные образы с помощью искусственного интеллекта Gemini AI",
            icon = Icons.Default.AutoAwesome,
            gradient = listOf(MagicalPurple, MagicalPink)
        ),
        OnboardingStep(
            title = "Настройте API ключ",
            description = "Для работы с ИИ необходимо настроить API ключ Gemini AI",
            icon = Icons.Default.Key,
            gradient = listOf(MagicalBlue, MagicalPurple)
        ),
        OnboardingStep(
            title = "Начните создавать образы",
            description = "Загрузите фото, выберите стиль и получите результат в 4K качестве",
            icon = Icons.Default.CameraAlt,
            gradient = listOf(MagicalPink, MagicalGold)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundLight, Color.White)
                )
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Step Indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentStep) MagicalPurple else Color.Gray.copy(alpha = 0.3f)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Current Step Content
        val currentStepData = steps[currentStep]
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(currentStepData.gradient),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = currentStepData.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = currentStepData.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = currentStepData.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Instructions for API Key
        if (currentStep == 1) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Как получить API ключ:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val instructions = listOf(
                        "1. Перейдите на https://aistudio.google.com/",
                        "2. Войдите в аккаунт Google",
                        "3. Нажмите 'Get API key'",
                        "4. Создайте новый API ключ",
                        "5. Скопируйте ключ и вставьте в настройки"
                    )
                    
                    instructions.forEach { instruction ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = instruction,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                GradientButton(
                    text = "Назад",
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f),
                    gradient = listOf(Color.Gray, Color.Gray)
                )
            }
            
            GradientButton(
                text = if (currentStep == steps.size - 1) "Начать" else "Далее",
                onClick = { 
                    if (currentStep == steps.size - 1) {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    } else {
                        currentStep++
                    }
                },
                modifier = Modifier.weight(1f),
                gradient = currentStepData.gradient
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val gradient: List<Color>
)