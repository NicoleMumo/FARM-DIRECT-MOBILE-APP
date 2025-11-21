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

    private val firestore = FirebaseUtils.firestore
    private val auth = FirebaseUtils.auth

    init {
        fetchWishlist()
    }

    private fun currentUserId(): String? = auth.currentUser?.uid

    fun fetchWishlist() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val userId = currentUserId()
            if (userId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = emptyList(),
                    filteredItems = emptyList(),
                    errorMessage = "Please log in to view your wishlist"
                )
                return@launch
            }
            try {
                val result = firestore
                    .collection("wishlist")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val items = result.documents.mapNotNull { document ->
                    val productId = document.getString("productId") ?: return@mapNotNull null
                    val productDoc = firestore
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
                            farmName = it.farmerName.ifBlank { "Partner Farm" },
                            category = it.category.ifBlank { "Vegetables" },
                            imageUrl = it.imageUrl,
                            unit = it.unit.ifBlank { "kg" },
                            stock = it.stock
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

    fun addToWishlist(productId: String) {
        viewModelScope.launch {
            val userId = currentUserId()
            if (userId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Please log in to save items"
                )
                return@launch
            }
            try {
                val existing = firestore
                    .collection("wishlist")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("productId", productId)
                    .get()
                    .await()

                if (existing.documents.isEmpty()) {
                    val wishlistItem = hashMapOf(
                        "userId" to userId,
                        "productId" to productId,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    firestore
                        .collection("wishlist")
                        .add(wishlistItem)
                        .await()
                    fetchWishlist()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to add to wishlist"
                )
            }
        }
    }

    fun removeFromWishlist(itemId: String) {
        viewModelScope.launch {
            val userId = currentUserId()
            if (userId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Please log in to manage your wishlist"
                )
                return@launch
            }
            try {
                firestore
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

    fun moveItemToCart(item: WishlistItem) {
        viewModelScope.launch {
            val userId = currentUserId()
            if (userId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Please log in to add items to your cart"
                )
                return@launch
            }
            try {
                val productSnapshot = firestore
                    .collection("products")
                    .document(item.productId)
                    .get()
                    .await()
                val stockAvailable = productSnapshot.getLong("stock")?.toInt() ?: item.stock
                if (stockAvailable <= 0) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "${item.name} is currently out of stock"
                    )
                    return@launch
                }

                val cartQuery = firestore
                    .collection("cart")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("productId", item.productId)
                    .get()
                    .await()

                if (cartQuery.documents.isNotEmpty()) {
                    val cartDoc = cartQuery.documents.first()
                    val currentQuantity = cartDoc.getLong("quantity")?.toInt() ?: 0
                    if (currentQuantity >= stockAvailable) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "You already have the maximum stock for ${item.name} in your cart"
                        )
                        return@launch
                    }
                    firestore
                        .collection("cart")
                        .document(cartDoc.id)
                        .update("quantity", currentQuantity + 1)
                        .await()
                } else {
                    val cartItem = hashMapOf(
                        "userId" to userId,
                        "productId" to item.productId,
                        "quantity" to 1
                    )
                    firestore
                        .collection("cart")
                        .add(cartItem)
                        .await()
                }

                firestore
                    .collection("wishlist")
                    .document(item.id)
                    .delete()
                    .await()

                fetchWishlist()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to add item to cart"
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

