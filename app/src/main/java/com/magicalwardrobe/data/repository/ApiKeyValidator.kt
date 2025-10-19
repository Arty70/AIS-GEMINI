package com.magicalwardrobe.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyValidator @Inject constructor() {
    
    suspend fun validateApiKey(apiKey: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY") {
                false
            } else {
                // Test the API key by creating a simple model instance
                val testModel = GenerativeModel(
                    modelName = "gemini-1.5-pro",
                    apiKey = apiKey
                )
                // Try to generate a simple text to validate the key
                val response = testModel.generateContent("test")
                response.text != null
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun isApiKeyFormatValid(apiKey: String): Boolean {
        // Basic validation for Gemini API key format
        return apiKey.isNotEmpty() && 
               apiKey != "YOUR_GEMINI_API_KEY" && 
               apiKey.length > 20 && 
               apiKey.matches(Regex("[A-Za-z0-9_-]+"))
    }
}