package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.R
import com.example.farmdirect.data.FarmDirectRepository
import com.example.farmdirect.model.Product
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductsUiState(
    val products: List<FarmerProduct> = emptyList(),
    val isLoading: Boolean = false
)

class ProductsViewModel(private val repository: FarmDirectRepository = FarmDirectRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val farmerId = FirebaseUtils.auth.currentUser?.uid
            if (farmerId != null) {
                val products = repository.getProductsForFarmer(farmerId)
                _uiState.value = _uiState.value.copy(
                    products = products.map { it.toFarmerProduct() },
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            val updatedProducts = _uiState.value.products.filter { it.id != productId }
            _uiState.value = _uiState.value.copy(products = updatedProducts)

            launch { 
                repository.deleteProduct(productId)
            }
        }
    }

    fun refresh() {
        loadProducts()
    }
}

private fun Product.toFarmerProduct(): FarmerProduct {
    val status = when {
        stock <= 0 -> ProductStatus.OUT_OF_STOCK
        stock < 10 -> ProductStatus.LOW_STOCK // Assuming less than 10 is low stock
        else -> ProductStatus.ACTIVE
    }
    val iconRes = when (category.lowercase()) {
        "vegetables" -> R.drawable.vegetable_icon
        "fruits" -> R.drawable.fruit_icon
        "dairy" -> R.drawable.dairy_icon
        "grains" -> R.drawable.grain_icon
        else -> R.drawable.ic_launcher_background
    }
    return FarmerProduct(
        id = id,
        name = name,
        description = description,
        price = price,
        unit = unit,
        stock = stock,
        category = category,
        status = status,
        iconRes = iconRes
    )
}
