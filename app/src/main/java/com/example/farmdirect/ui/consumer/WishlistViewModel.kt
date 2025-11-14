package com.example.farmdirect.ui.consumer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class WishlistUiState(
    val isLoading: Boolean = false,
    val items: List<WishlistItem> = emptyList(),
    val filteredItems: List<WishlistItem> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class WishlistViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()
    
    private val userId = FirebaseUtils.auth.currentUser?.uid ?: ""
    
    init {
        fetchWishlist()
    }
    
    fun fetchWishlist() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = FirebaseUtils.firestore
                    .collection("wishlist")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                val items = result.documents.mapNotNull { document ->
                    val productId = document.getString("productId") ?: return@mapNotNull null
                    val productDoc = FirebaseUtils.firestore
                        .collection("products")
                        .document(productId)
                        .get()
                        .await()
                    
                    val product = productDoc.toObject(com.example.farmdirect.model.Product::class.java)
                    product?.let {
                        WishlistItem(
                            id = document.id,
                            productId = productId,
                            name = it.name,
                            price = it.price,
                            farmName = "Green Valley Farm", // TODO: Get from farmer data
                            category = "Vegetables", // TODO: Add to Product model
                            imageUrl = null
                        )
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = items,
                    filteredItems = items
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load wishlist"
                )
            }
        }
    }
    
    fun removeFromWishlist(itemId: String) {
        viewModelScope.launch {
            try {
                FirebaseUtils.firestore
                    .collection("wishlist")
                    .document(itemId)
                    .delete()
                    .await()
                fetchWishlist()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to remove item"
                )
            }
        }
    }
    
    fun addToCart(productId: String) {
        viewModelScope.launch {
            try {
                val cartItem = hashMapOf(
                    "userId" to userId,
                    "productId" to productId,
                    "quantity" to 1
                )
                FirebaseUtils.firestore
                    .collection("cart")
                    .add(cartItem)
                    .await()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to add to cart"
                )
            }
        }
    }
    
    fun onSearchChanged(query: String) {
        val filtered = _uiState.value.items.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.farmName.contains(query, ignoreCase = true)
        }
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredItems = filtered
        )
    }
}

