package com.magicalwardrobe.ui.screens.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicalwardrobe.data.repository.WardrobeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val wardrobeRepository: WardrobeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WardrobeUiState())
    val uiState: StateFlow<WardrobeUiState> = _uiState.asStateFlow()

    init {
        loadWardrobeItems()
    }

    private fun loadWardrobeItems() {
        viewModelScope.launch {
            try {
                val items = wardrobeRepository.getAllWardrobeItems()
                _uiState.value = _uiState.value.copy(
                    wardrobeItems = items,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка загрузки гардероба",
                    isLoading = false
                )
            }
        }
    }

    fun toggleFavorite(itemId: String) {
        viewModelScope.launch {
            try {
                wardrobeRepository.toggleFavorite(itemId)
                loadWardrobeItems() // Reload to reflect changes
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка обновления избранного"
                )
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                wardrobeRepository.deleteWardrobeItem(itemId)
                loadWardrobeItems() // Reload to reflect changes
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка удаления элемента"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class WardrobeUiState(
    val wardrobeItems: List<WardrobeItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)