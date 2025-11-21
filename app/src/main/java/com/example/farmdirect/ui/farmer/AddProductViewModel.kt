package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.data.FarmDirectRepository
import com.example.farmdirect.model.Product
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddProductUiState(
    val isLoading: Boolean = false,
    val isProductAdded: Boolean = false
)

class AddProductViewModel(private val repository: FarmDirectRepository = FarmDirectRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProductAdded = true)
            val farmerId = FirebaseUtils.auth.currentUser?.uid
            if (farmerId != null) {
                launch {
                    repository.addProduct(product.copy(farmerId = farmerId))
                }
            }
        }
    }
}
