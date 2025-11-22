package com.example.farmdirect.ui.consumer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.model.Product
import com.example.farmdirect.utils.FirebaseUtils
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    private var productsListener: ListenerRegistration? = null

    init {
        observeProducts()
    }

    fun observeProducts() {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        productsListener?.remove()
        productsListener = FirebaseUtils.firestore.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load products"
                    )
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { document ->
                    val product = document.toObject(Product::class.java)
                    product?.let {
                        if (it.stock <= 0) {
                            null
                        } else {
                        ProductUi(
                            id = document.id,
                            name = it.name,
                            price = it.price,
                                farmName = it.farmerName.ifBlank { "Partner Farm" },
                                category = it.category.ifBlank { "Vegetables" },
                                imageUrl = it.imageUrl,
                                unit = it.unit.ifBlank { "kg" },
                                stock = it.stock,
                                description = it.description
                        )
                    }
                }
                }.orEmpty()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = products,
                    filteredProducts = products
                )
                filterProducts(
                    _uiState.value.searchQuery,
                    _uiState.value.selectedCategory,
                    products
                )
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

    private fun filterProducts(query: String, category: String?, source: List<ProductUi>? = null) {
        val products = source ?: _uiState.value.products
        val filtered = products.filter { product ->
            val matchesQuery = query.isEmpty() || product.name.contains(query, ignoreCase = true)
            val matchesCategory = category == null || product.category == category
            matchesQuery && matchesCategory
        }
        _uiState.value = _uiState.value.copy(filteredProducts = filtered)
    }

    override fun onCleared() {
        super.onCleared()
        productsListener?.remove()
    }
}

