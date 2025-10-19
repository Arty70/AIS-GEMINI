package com.magicalwardrobe.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicalwardrobe.data.config.ApiConfig
import com.magicalwardrobe.data.repository.ApiKeyValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiConfig: ApiConfig,
    private val apiKeyValidator: ApiKeyValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val isApiKeyConfigured = apiConfig.isApiKeyConfigured()
            val currentApiKey = if (isApiKeyConfigured) {
                apiConfig.getGeminiApiKey()
            } else {
                ""
            }
            
            _uiState.value = _uiState.value.copy(
                isApiKeyConfigured = isApiKeyConfigured,
                currentApiKey = currentApiKey,
                isLoading = false
            )
        }
    }

    fun updateApiKey(apiKey: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isValidating = true, error = null)
                
                // Validate API key format first
                if (!apiKeyValidator.isApiKeyFormatValid(apiKey)) {
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        error = "Неверный формат API ключа"
                    )
                    return@launch
                }
                
                // Validate API key with Gemini
                val isValid = apiKeyValidator.validateApiKey(apiKey)
                if (isValid) {
                    apiConfig.setGeminiApiKey(apiKey)
                    _uiState.value = _uiState.value.copy(
                        isApiKeyConfigured = true,
                        currentApiKey = apiKey,
                        isValidating = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        error = "API ключ недействителен. Проверьте правильность ключа."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    error = e.message ?: "Ошибка валидации API ключа"
                )
            }
        }
    }

    fun setHighQuality(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(highQualityEnabled = enabled)
        // In real implementation, save to preferences
    }

    fun setAutoSave(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoSaveEnabled = enabled)
        // In real implementation, save to preferences
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SettingsUiState(
    val isApiKeyConfigured: Boolean = false,
    val currentApiKey: String = "",
    val highQualityEnabled: Boolean = true,
    val autoSaveEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val isValidating: Boolean = false,
    val error: String? = null
)