package com.magicalwardrobe.ui.screens.camera

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
class CameraViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun onStyleSelected(style: StyleOption) {
        _uiState.value = _uiState.value.copy(selectedStyle = style)
    }

    fun generateOutfit() {
        val currentState = _uiState.value
        val imageUri = currentState.selectedImageUri ?: return
        val style = currentState.selectedStyle ?: return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isGenerating = true, error = null)
            
            try {
                val result = geminiRepository.generateOutfit(
                    imageUri = imageUri,
                    style = style.name
                )
                _uiState.value = currentState.copy(
                    isGenerating = false,
                    generatedImageUri = result,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isGenerating = false,
                    error = e.message ?: "Ошибка генерации"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CameraUiState(
    val selectedImageUri: Uri? = null,
    val selectedStyle: StyleOption? = null,
    val isGenerating: Boolean = false,
    val generatedImageUri: Uri? = null,
    val error: String? = null
)