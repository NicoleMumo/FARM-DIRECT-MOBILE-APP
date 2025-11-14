package com.example.farmdirect.ui.consumer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.model.Product
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ConsumerHomeUiState(
    val isLoading: Boolean = false,
    val products: List<ProductUi> = emptyList(),
    val filteredProducts: List<ProductUi> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val errorMessage: String? = null
)

class ConsumerHomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConsumerHomeUiState())
    val uiState: StateFlow<ConsumerHomeUiState> = _uiState.asStateFlow()

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = FirebaseUtils.firestore.collection("products").get().await()
                val products = result.documents.mapNotNull { document ->
                    val product = document.toObject(Product::class.java)
                    product?.let {
                        // Map Product to ProductUi
                        // For now, we'll use placeholder data for farmName and category
                        // You can update this when your Product model has these fields
                        ProductUi(
                            id = document.id,
                            name = it.name,
                            price = it.price,
                            farmName = "Green Valley Farm", // Placeholder - update when Product model has this
                            category = "Vegetables", // Placeholder - update when Product model has this
                            imageUrl = null
                        )
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = products,
                    filteredProducts = products
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load products"
                )
            }
        }
    }

    fun onSearchChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterProducts(query, _uiState.value.selectedCategory)
    }

    fun onCategorySelected(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        filterProducts(_uiState.value.searchQuery, category)
    }

    private fun filterProducts(query: String, category: String?) {
        val products = _uiState.value.products
        val filtered = products.filter { product ->
            val matchesQuery = query.isEmpty() || product.name.contains(query, ignoreCase = true)
            val matchesCategory = category == null || product.category == category
            matchesQuery && matchesCategory
        }
        _uiState.value = _uiState.value.copy(filteredProducts = filtered)
    }
}

