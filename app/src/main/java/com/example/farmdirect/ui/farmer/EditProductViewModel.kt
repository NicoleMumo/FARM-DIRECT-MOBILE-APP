package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.data.FarmDirectRepository
import com.example.farmdirect.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProductUiState(
    val isLoading: Boolean = false,
    val isProductUpdated: Boolean = false,
    val product: Product? = null
)

class EditProductViewModel(private val repository: FarmDirectRepository = FarmDirectRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProductUiState())
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val product = repository.getProduct(productId)
            _uiState.value = _uiState.value.copy(isLoading = false, product = product)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProductUpdated = true)
            launch {
                repository.updateProduct(product)
            }
        }
    }
}
