package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductsUiState(
    val products: List<FarmerProduct> = emptyList(),
    val isLoading: Boolean = false
)

class ProductsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // TODO: Load from Firebase
            val mockProducts = listOf(
                FarmerProduct(
                    id = "1",
                    name = "Fresh Carrots",
                    description = "Organic carrots from local farm",
                    price = 350.0,
                    unit = "kg",
                    stock = 45.0,
                    category = "Vegetables",
                    status = ProductStatus.ACTIVE,
                    iconRes = com.example.farmdirect.R.drawable.vegetable_icon
                ),
                FarmerProduct(
                    id = "2",
                    name = "Farm Fresh Milk",
                    description = "Pure cow milk, daily fresh",
                    price = 220.0,
                    unit = "L",
                    stock = 120.0,
                    category = "Dairy",
                    status = ProductStatus.ACTIVE,
                    iconRes = com.example.farmdirect.R.drawable.dairy_icon
                ),
                FarmerProduct(
                    id = "3",
                    name = "Red Apples",
                    description = "Sweet and crunchy apples",
                    price = 580.0,
                    unit = "kg",
                    stock = 5.0,
                    category = "Fruits",
                    status = ProductStatus.LOW_STOCK,
                    iconRes = com.example.farmdirect.R.drawable.fruit_icon
                ),
                FarmerProduct(
                    id = "4",
                    name = "Maize",
                    description = "Freshly harvested maize",
                    price = 60.0,
                    unit = "kg",
                    stock = 0.0,
                    category = "Grains",
                    status = ProductStatus.OUT_OF_STOCK,
                    iconRes = com.example.farmdirect.R.drawable.grain_icon
                )
            )
            
            _uiState.value = _uiState.value.copy(
                products = mockProducts,
                isLoading = false
            )
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            // TODO: Delete from Firebase
            val updatedProducts = _uiState.value.products.filter { it.id != productId }
            _uiState.value = _uiState.value.copy(products = updatedProducts)
        }
    }

    fun refresh() {
        loadProducts()
    }
}

