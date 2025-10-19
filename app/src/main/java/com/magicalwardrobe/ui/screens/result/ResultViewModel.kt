package com.magicalwardrobe.ui.screens.result

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicalwardrobe.data.repository.GeminiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun setGeneratedImage(uri: Uri, style: String) {
        _uiState.value = _uiState.value.copy(
            generatedImageUri = uri,
            selectedStyle = style
        )
    }

    fun saveImage() {
        viewModelScope.launch {
            try {
                val uri = _uiState.value.generatedImageUri ?: return@launch
                // Implement save to gallery
                _uiState.value = _uiState.value.copy(
                    isSaving = true,
                    error = null
                )
                
                // Simulate save operation
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    showSaveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Ошибка сохранения"
                )
            }
        }
    }

    fun shareImage() {
        viewModelScope.launch {
            try {
                val uri = _uiState.value.generatedImageUri ?: return@launch
                // Implement share functionality
                _uiState.value = _uiState.value.copy(
                    isSharing = true,
                    error = null
                )
                
                // Simulate share operation
                kotlinx.coroutines.delay(500)
                
                _uiState.value = _uiState.value.copy(
                    isSharing = false,
                    showShareSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSharing = false,
                    error = e.message ?: "Ошибка отправки"
                )
            }
        }
    }

    fun generateVariation(variation: StyleVariation) {
        viewModelScope.launch {
            try {
                val currentUri = _uiState.value.generatedImageUri ?: return@launch
                _uiState.value = _uiState.value.copy(
                    isGenerating = true,
                    error = null
                )
                
                val result = geminiRepository.generateOutfit(
                    imageUri = currentUri,
                    style = variation.name
                )
                
                _uiState.value = _uiState.value.copy(
                    generatedImageUri = result,
                    selectedStyle = variation.name,
                    isGenerating = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    error = e.message ?: "Ошибка генерации варианта"
                )
            }
        }
    }

    fun enhanceQuality() {
        viewModelScope.launch {
            try {
                val currentUri = _uiState.value.generatedImageUri ?: return@launch
                _uiState.value = _uiState.value.copy(
                    isEnhancing = true,
                    error = null
                )
                
                val result = geminiRepository.enhanceImageQuality(currentUri)
                
                _uiState.value = _uiState.value.copy(
                    generatedImageUri = result,
                    isEnhancing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isEnhancing = false,
                    error = e.message ?: "Ошибка улучшения качества"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessages() {
        _uiState.value = _uiState.value.copy(
            showSaveSuccess = false,
            showShareSuccess = false
        )
    }
}

data class ResultUiState(
    val generatedImageUri: Uri? = null,
    val selectedStyle: String = "",
    val isSaving: Boolean = false,
    val isSharing: Boolean = false,
    val isGenerating: Boolean = false,
    val isEnhancing: Boolean = false,
    val showSaveSuccess: Boolean = false,
    val showShareSuccess: Boolean = false,
    val error: String? = null
)