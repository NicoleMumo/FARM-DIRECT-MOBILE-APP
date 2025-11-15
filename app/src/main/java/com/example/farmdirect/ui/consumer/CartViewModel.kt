package com.example.farmdirect.ui.consumer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CartUiState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 50.0,
    val total: Double = 0.0,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val deliveryAddress: DeliveryAddress = DeliveryAddress(
        id = "1",
        label = "Home",
        location = "Kilimani, Nairobi",
        details = "Building 12, Apt 4B"
    ),
    val errorMessage: String? = null
)

class CartViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
    
    private val userId = FirebaseUtils.auth.currentUser?.uid ?: ""
    
    init {
        fetchCart()
        initializePaymentMethods()
    }
    
    private fun initializePaymentMethods() {
        val methods = listOf(
            PaymentMethod(
                id = "mpesa",
                name = "M-Pesa",
                description = "Pay via STK Push",
                iconRes = com.example.farmdirect.R.drawable.ic_seed, // TODO: Add M-Pesa icon
                isSelected = true
            ),
            PaymentMethod(
                id = "card",
                name = "Card Payment",
                description = "Visa, Mastercard",
                iconRes = com.example.farmdirect.R.drawable.ic_seed // TODO: Add card icon
            )
        )
        _uiState.value = _uiState.value.copy(paymentMethods = methods)
    }
    
    fun fetchCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = FirebaseUtils.firestore
                    .collection("cart")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                val items = result.documents.mapNotNull { document ->
                    val productId = document.getString("productId") ?: return@mapNotNull null
                    val quantity = document.getLong("quantity")?.toInt() ?: 1
                    
                    val productDoc = FirebaseUtils.firestore
                        .collection("products")
                        .document(productId)
                        .get()
                        .await()
                    
                    val product = productDoc.toObject(com.example.farmdirect.model.Product::class.java)
                    product?.let {
                        CartItem(
                            id = document.id,
                            productId = productId,
                            name = it.name,
                            price = it.price,
                            quantity = quantity
                        )
                    }
                }
                
                val subtotal = items.sumOf { it.price * it.quantity }
                val total = subtotal + _uiState.value.deliveryFee
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = items,
                    subtotal = subtotal,
                    total = total
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load cart"
                )
            }
        }
    }
    
    fun updateQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                if (quantity <= 0) {
                    removeItem(itemId)
                } else {
                    FirebaseUtils.firestore
                        .collection("cart")
                        .document(itemId)
                        .update("quantity", quantity)
                        .await()
                    fetchCart()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to update quantity"
                )
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            try {
                FirebaseUtils.firestore
                    .collection("cart")
                    .document(itemId)
                    .delete()
                    .await()
                fetchCart()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to remove item"
                )
            }
        }
    }
    
    fun selectPaymentMethod(methodId: String) {
        val updatedMethods = _uiState.value.paymentMethods.map {
            it.copy(isSelected = it.id == methodId)
        }
        _uiState.value = _uiState.value.copy(paymentMethods = updatedMethods)
    }
    
    fun addToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                // Check if item already exists in cart
                val existingCart = FirebaseUtils.firestore
                    .collection("cart")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("productId", productId)
                    .get()
                    .await()
                
                if (existingCart.documents.isNotEmpty()) {
                    // Update quantity
                    val docId = existingCart.documents[0].id
                    val currentQuantity = existingCart.documents[0].getLong("quantity")?.toInt() ?: 1
                    FirebaseUtils.firestore
                        .collection("cart")
                        .document(docId)
                        .update("quantity", currentQuantity + quantity)
                        .await()
                } else {
                    // Add new item
                    val cartItem = hashMapOf(
                        "userId" to userId,
                        "productId" to productId,
                        "quantity" to quantity
                    )
                    FirebaseUtils.firestore
                        .collection("cart")
                        .add(cartItem)
                        .await()
                }
                fetchCart()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to add to cart"
                )
            }
        }
    }
    
    fun checkout() {
        viewModelScope.launch {
            try {
                // TODO: Implement checkout logic
                // Create order, process payment, clear cart, etc.
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to checkout"
                )
            }
        }
    }
}

