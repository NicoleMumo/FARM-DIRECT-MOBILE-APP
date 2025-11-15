package com.example.farmdirect.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.R
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProductManagementUiState(
    val totalProducts: Int = 5,
    val pendingProducts: Int = 2,
    val flaggedProducts: Int = 2,
    val products: List<AdminProduct> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class ProductManagementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProductManagementUiState())
    val uiState: StateFlow<ProductManagementUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        FirebaseUtils.firestore.collection("products")
            .get()
            .addOnSuccessListener { result ->
                val products = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: "Unknown Product"
                    val seller = doc.getString("sellerName") ?: "Unknown Seller"
                    val price = doc.getDouble("price") ?: 0.0
                    val unit = doc.getString("unit") ?: "kg"
                    val category = doc.getString("category") ?: "Vegetables"
                    val status = when (doc.getString("status")?.uppercase()) {
                        "APPROVED" -> ProductStatus.APPROVED
                        "FLAGGED" -> ProductStatus.FLAGGED
                        else -> ProductStatus.PENDING
                    }
                    
                    val iconRes = when (category.lowercase()) {
                        "vegetables" -> R.drawable.vegetable_icon
                        "fruits" -> R.drawable.fruit_icon
                        "dairy" -> R.drawable.dairy_icon
                        "grains" -> R.drawable.grain_icon
                        else -> R.drawable.vegetable_icon
                    }
                    
                    AdminProduct(
                        id = doc.id,
                        name = name,
                        seller = seller,
                        price = "Ksh ${price.toInt()}/$unit",
                        category = category,
                        status = status,
                        iconRes = iconRes
                    )
                }
                
                val pending = products.count { it.status == ProductStatus.PENDING }
                val flagged = products.count { it.status == ProductStatus.FLAGGED }
                
                _uiState.value = _uiState.value.copy(
                    products = products,
                    totalProducts = products.size,
                    pendingProducts = pending,
                    flaggedProducts = flagged,
                    isLoading = false
                )
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredProducts(): List<AdminProduct> {
        var filtered = _uiState.value.products
        
        // Apply category filter
        if (_uiState.value.selectedCategory != "All") {
            filtered = filtered.filter { 
                it.category.equals(_uiState.value.selectedCategory, ignoreCase = true)
            }
        }
        
        // Apply search filter
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                it.seller.lowercase().contains(query)
            }
        }
        
        return filtered
    }

    fun approveProduct(productId: String) {
        FirebaseUtils.firestore.collection("products").document(productId)
            .update("status", "APPROVED")
            .addOnSuccessListener {
                loadProducts()
            }
    }

    fun flagProduct(productId: String) {
        FirebaseUtils.firestore.collection("products").document(productId)
            .update("status", "FLAGGED")
            .addOnSuccessListener {
                loadProducts()
            }
    }

    fun deleteProduct(productId: String) {
        FirebaseUtils.firestore.collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                loadProducts()
            }
    }
}

