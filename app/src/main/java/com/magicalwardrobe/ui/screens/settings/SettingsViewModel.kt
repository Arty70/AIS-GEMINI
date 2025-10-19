package com.magicalwardrobe.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicalwardrobe.data.config.ApiConfig
import com.magicalwardrobe.data.repository.ApiKeyValidator
import com.magicalwardrobe.data.auth.GoogleAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiConfig: ApiConfig,
    private val apiKeyValidator: ApiKeyValidator,
    private val googleAuthService: GoogleAuthService
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
            
            val isGoogleSignedIn = googleAuthService.isSignedIn()
            
            _uiState.value = _uiState.value.copy(
                isApiKeyConfigured = isApiKeyConfigured,
                currentApiKey = currentApiKey,
                isGoogleSignedIn = isGoogleSignedIn,
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

    fun signInGoogle() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val account = googleAuthService.signIn()
                if (account != null) {
                    _uiState.value = _uiState.value.copy(
                        isGoogleSignedIn = true,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Не удалось войти в Google аккаунт"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка входа в Google"
                )
            }
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            try {
                googleAuthService.signOut()
                _uiState.value = _uiState.value.copy(
                    isGoogleSignedIn = false,
                    googleDriveEnabled = false,
                    googlePhotosEnabled = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка выхода из Google"
                )
            }
        }
    }

    fun setGoogleDrive(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(googleDriveEnabled = enabled)
        // In real implementation, save to preferences
    }

    fun setGooglePhotos(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(googlePhotosEnabled = enabled)
        // In real implementation, save to preferences
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SettingsUiState(
    val isApiKeyConfigured: Boolean = false,
    val currentApiKey: String = "",
    val isGoogleSignedIn: Boolean = false,
    val googleDriveEnabled: Boolean = false,
    val googlePhotosEnabled: Boolean = false,
    val highQualityEnabled: Boolean = true,
    val autoSaveEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val isValidating: Boolean = false,
    val error: String? = null
)