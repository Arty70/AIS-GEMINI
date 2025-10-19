package com.magicalwardrobe.utils

import android.content.Context
import android.widget.Toast
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ApiKeyTest {
    
    fun testApiKey(context: Context, apiKey: String, onResult: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-1.5-pro",
                    apiKey = apiKey
                )
                
                val response = model.generateContent("Привет! Это тест API ключа.")
                val result = response.text
                
                if (result != null && result.isNotEmpty()) {
                    onResult(true, "API ключ работает корректно!")
                } else {
                    onResult(false, "API ключ не вернул результат")
                }
            } catch (e: Exception) {
                onResult(false, "Ошибка: ${e.message}")
            }
        }
    }
    
    fun showTestResult(context: Context, success: Boolean, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                context,
                if (success) "✅ $message" else "❌ $message",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}