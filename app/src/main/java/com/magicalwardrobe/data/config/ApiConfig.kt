package com.magicalwardrobe.data.config

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("api_config", Context.MODE_PRIVATE)
    
    companion object {
        private const val GEMINI_API_KEY = "gemini_api_key"
        private const val DEFAULT_API_KEY = "YOUR_GEMINI_API_KEY"
    }
    
    fun getGeminiApiKey(): String {
        return prefs.getString(GEMINI_API_KEY, DEFAULT_API_KEY) ?: DEFAULT_API_KEY
    }
    
    fun setGeminiApiKey(apiKey: String) {
        prefs.edit().putString(GEMINI_API_KEY, apiKey).apply()
    }
    
    fun isApiKeyConfigured(): Boolean {
        val apiKey = getGeminiApiKey()
        return apiKey.isNotEmpty() && apiKey != DEFAULT_API_KEY
    }
    
    fun clearApiKey() {
        prefs.edit().remove(GEMINI_API_KEY).apply()
    }
}