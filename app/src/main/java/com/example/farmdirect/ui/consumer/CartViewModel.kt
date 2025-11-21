package com.example.farmdirect.ui.consumer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.model.Product
import com.example.farmdirect.utils.FirebaseUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.farmdirect.ui.consumer.CheckoutStatus

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
    val errorMessage: String? = null,
    val checkoutStatus: CheckoutStatus = CheckoutStatus.IDLE,
    val checkoutMessage: String? = null,
    val showPaymentPrompt: Boolean = false,
    val paymentPromptMessage: String? = null
)

class CartViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
    
    private val firestore = FirebaseUtils.firestore
    private val auth = FirebaseUtils.auth
    
    init {
        fetchCart()
        initializePaymentMethods()
    }

    private fun currentUserId(): String? = auth.currentUser?.uid
    
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
            val userId = currentUserId()
            if (userId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = emptyList(),
                    subtotal = 0.0,
                    total = _uiState.value.deliveryFee,
                    errorMessage = "Please log in to view your cart"
                )
                return@launch
            }
            try {
                val result = firestore
                    .collection("cart")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                val items = result.documents.mapNotNull { document ->
                    val productId = document.getString("productId") ?: return@mapNotNull null
                    val quantity = document.getLong("quantity")?.toInt() ?: 1
                    val productDoc = firestore
                        .collection("products")
                        .document(productId)
                        .get()
                        .await()
                    val product = productDoc.toObject(Product::class.java)?.copy(id = productDoc.id)
                    product?.let {
                        CartItem(
                            id = document.id,
                            productId = productId,
                            name = it.name,
                            price = it.price,
                            quantity = quantity.coerceAtMost(it.stock.takeIf { stock -> stock > 0 } ?: quantity),
                            unit = it.unit,
                            imageUrl = it.imageUrl,
                            farmerId = it.farmerId,
                            farmerName = it.farmerName,
                            availableStock = it.stock
                        )
                    }
                }
                
                val subtotal = items.sumOf { it.price * it.quantity }
                val total = subtotal + _uiState.value.deliveryFee
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = items,
                    subtotal = subtotal,
                    total = total,
                    errorMessage = null
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
                val userId = currentUserId()
                if (userId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Please log in to update your cart"
                    )
                    return@launch
                }
                if (quantity <= 0) {
                    removeItem(itemId)
                } else {
                    val cartDoc = firestore
                        .collection("cart")
                        .document(itemId)
                        .get()
                        .await()
                    val productId = cartDoc.getString("productId") ?: return@launch
                    val productSnapshot = firestore
                        .collection("products")
                        .document(productId)
                        .get()
                        .await()
                    val stockAvailable = productSnapshot.getLong("stock")?.toInt() ?: Int.MAX_VALUE
                    val finalQuantity = quantity.coerceAtMost(stockAvailable)
                    firestore
                        .collection("cart")
                        .document(itemId)
                        .update("quantity", finalQuantity)
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
                val userId = currentUserId()
                if (userId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Please log in to manage your cart"
                    )
                    return@launch
                }
                firestore
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
                val userId = currentUserId()
                if (userId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Please log in to add items to your cart"
                    )
                    return@launch
                }
                // Check if item already exists in cart
                val existingCart = firestore
                    .collection("cart")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("productId", productId)
                    .get()
                    .await()
                
                if (existingCart.documents.isNotEmpty()) {
                    // Update quantity
                    val docId = existingCart.documents[0].id
                    val currentQuantity = existingCart.documents[0].getLong("quantity")?.toInt() ?: 1
                    val desiredQuantity = currentQuantity + quantity
                    val productSnapshot = firestore
                        .collection("products")
                        .document(productId)
                        .get()
                        .await()
                    val stockAvailable = productSnapshot.getLong("stock")?.toInt() ?: Int.MAX_VALUE
                    val finalQuantity = desiredQuantity.coerceAtMost(stockAvailable)
                    firestore
                        .collection("cart")
                        .document(docId)
                        .update("quantity", finalQuantity)
                        .await()
                } else {
                    // Add new item
                    val productSnapshot = firestore
                        .collection("products")
                        .document(productId)
                        .get()
                        .await()
                    val stockAvailable = productSnapshot.getLong("stock")?.toInt() ?: quantity
                    val finalQuantity = quantity.coerceAtMost(stockAvailable)
                    val cartItem = hashMapOf(
                        "userId" to userId,
                        "productId" to productId,
                        "quantity" to finalQuantity
                    )
                    firestore
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
            val currentItems = _uiState.value.items
            val userId = currentUserId()
            if (userId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    checkoutStatus = CheckoutStatus.ERROR,
                    checkoutMessage = "Please log in to checkout",
                    errorMessage = "Please log in to checkout"
                )
                return@launch
            }
            if (currentItems.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    checkoutStatus = CheckoutStatus.ERROR,
                    checkoutMessage = "Your cart is empty",
                    errorMessage = "Your cart is empty"
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                checkoutStatus = CheckoutStatus.PROCESSING,
                checkoutMessage = "Initiating checkout...",
                errorMessage = null,
                showPaymentPrompt = false
            )
            try {
                val userDoc = firestore
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()
                val customerName = userDoc.getString("name") ?: "Customer"
                val customerEmail = userDoc.getString("email") ?: ""
                val orderNumber = generateOrderNumber()
                val paymentMethod = _uiState.value.paymentMethods.firstOrNull { it.isSelected }?.name ?: "M-Pesa"
                val shippingAddress = _uiState.value.deliveryAddress
                val itemsSummary = currentItems.joinToString(separator = ", ") { "${it.quantity} x ${it.name}" }
                val totalAmount = _uiState.value.total
                val farmerSummary = currentItems.mapNotNull { it.farmerName.ifBlank { null } }
                    .distinct()
                    .ifEmpty { listOf("FarmDirect Partner") }
                    .joinToString()
                val primaryItem = currentItems.first()
                val cartIds = currentItems.map { it.id }
                
                val orderData = hashMapOf(
                    "userId" to userId,
                    "status" to "PENDING",
                    "orderNumber" to orderNumber,
                    "totalAmount" to totalAmount,
                    "paymentMethod" to paymentMethod,
                    "consumerName" to customerName,
                    "consumerEmail" to customerEmail,
                    "farmerName" to farmerSummary,
                    "items" to itemsSummary,
                    "primaryProductId" to primaryItem.productId,
                    "primaryProductName" to primaryItem.name,
                    "shippingAddress" to mapOf(
                        "label" to shippingAddress.label,
                        "location" to shippingAddress.location,
                        "details" to shippingAddress.details
                    ),
                    "createdAt" to Timestamp.now()
                )
                
                val orderRef = firestore.collection("orders").document()
                orderRef.set(orderData).await()
                
                val fulfillmentEntries = mutableListOf<Map<String, Any>>()
                val farmerIds = mutableSetOf<String>()
                
                currentItems.forEach { item ->
                    val productSnapshot = firestore
                        .collection("products")
                        .document(item.productId)
                        .get()
                        .await()
                    val availableStock = productSnapshot.getLong("stock")?.toInt() ?: item.availableStock
                    if (item.quantity > availableStock) {
                        throw IllegalStateException("Only $availableStock ${item.unit} of ${item.name} left in stock")
                    }
                    val farmerId = productSnapshot.getString("farmerId") ?: item.farmerId
                    val farmerName = productSnapshot.getString("farmerName")
                        ?: item.farmerName.ifBlank { resolveFarmerName(farmerId) }
                    val orderItemRef = orderRef.collection("items").document()
                    val orderItem = mutableMapOf<String, Any>()
                    orderItem["productId"] = item.productId
                    orderItem["productName"] = item.name
                    orderItem["quantity"] = item.quantity
                    orderItem["price"] = item.price
                    orderItem["farmerId"] = farmerId
                    orderItem["farmerName"] = farmerName
                    orderItem["unit"] = item.unit
                    orderItem["status"] = "PENDING"
                    orderItem["createdAt"] = Timestamp.now()
                    orderItemRef.set(orderItem).await()
                    
                    val farmerOrderData = mutableMapOf<String, Any?>()
                    farmerOrderData["orderId"] = orderRef.id
                    farmerOrderData["orderItemId"] = orderItemRef.id
                    farmerOrderData["orderNumber"] = orderNumber
                    farmerOrderData["consumerId"] = userId
                    farmerOrderData["consumerName"] = customerName
                    farmerOrderData["consumerEmail"] = customerEmail
                    farmerOrderData["farmerId"] = farmerId
                    farmerOrderData["farmerName"] = farmerName
                    farmerOrderData["productId"] = item.productId
                    farmerOrderData["productName"] = item.name
                    farmerOrderData["category"] = productSnapshot.getString("category") ?: item.unit
                    farmerOrderData["quantity"] = item.quantity
                    farmerOrderData["unit"] = item.unit
                    farmerOrderData["price"] = item.price
                    farmerOrderData["status"] = "PENDING"
                    farmerOrderData["createdAt"] = Timestamp.now()
                    firestore
                        .collection("farmerOrders")
                        .document(orderItemRef.id)
                        .set(farmerOrderData)
                        .await()
                    
                    farmerIds.add(farmerId)
                    val fulfillmentEntry = hashMapOf<String, Any>(
                        "orderItemId" to orderItemRef.id,
                        "productName" to item.name,
                        "farmerId" to farmerId,
                        "farmerName" to farmerName,
                        "quantity" to item.quantity,
                        "unit" to item.unit,
                        "status" to "PENDING"
                    )
                    fulfillmentEntries.add(fulfillmentEntry)
                }
                
                orderRef.update(
                    "farmerIds", farmerIds.toList(),
                    "fulfillments", fulfillmentEntries
                ).await()
                
                _uiState.value = _uiState.value.copy(
                    showPaymentPrompt = true,
                    paymentPromptMessage = "A push STK has been sent to your phone. Please wait two minutes before trying again in case of delay."
                )
                simulatePayment(orderRef.id, currentItems, orderNumber, totalAmount, cartIds)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to checkout"
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    checkoutStatus = CheckoutStatus.ERROR,
                    checkoutMessage = e.message ?: "Failed to checkout",
                    showPaymentPrompt = false
                )
                fetchCart()
            }
        }
    }
    
    private suspend fun clearCartForUser(cartIds: List<String>) {
        if (cartIds.isEmpty()) return
        val batch = firestore.batch()
        cartIds.forEach { id ->
            val docRef = firestore.collection("cart").document(id)
            batch.delete(docRef)
        }
        batch.commit().await()
    }
    
    private fun simulatePayment(
        orderId: String,
        items: List<CartItem>,
        orderNumber: String,
        totalAmount: Double,
        cartIds: List<String>
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    checkoutMessage = "Awaiting payment confirmation..."
                )
                delay(2500)
                val orderRef = firestore.collection("orders").document(orderId)
                val batch = firestore.batch()
                batch.update(orderRef, mapOf(
                    "paymentStatus" to "PAID",
                    "paymentReference" to "PESAPAL-${System.currentTimeMillis()}",
                    "paymentCapturedAt" to Timestamp.now()
                ))
                
                items.forEach { item ->
                    val productRef = firestore.collection("products").document(item.productId)
                    batch.update(productRef, "stock", FieldValue.increment((-item.quantity).toLong()))
                    if (item.farmerId.isNotBlank()) {
                        val farmerRef = firestore.collection("users").document(item.farmerId)
                        batch.set(
                            farmerRef,
                            mapOf("walletBalance" to FieldValue.increment(item.price * item.quantity)),
                            SetOptions.merge()
                        )
                    }
                }
                
                batch.commit().await()
                clearCartForUser(cartIds)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = emptyList(),
                    subtotal = 0.0,
                    total = _uiState.value.deliveryFee,
                    checkoutStatus = CheckoutStatus.SUCCESS,
                    checkoutMessage = "Order $orderNumber confirmed. Payment received (KSh ${totalAmount.toInt()}).",
                    showPaymentPrompt = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    checkoutStatus = CheckoutStatus.ERROR,
                    checkoutMessage = e.message ?: "Payment processing failed",
                    errorMessage = e.message ?: "Payment processing failed",
                    showPaymentPrompt = false
                )
            } finally {
                fetchCart()
            }
        }
    }
    
    private fun generateOrderNumber(): String {
        val randomPart = (1000..9999).random()
        return "FD-${System.currentTimeMillis()}-$randomPart"
    }

    private suspend fun resolveFarmerName(farmerId: String): String {
        if (farmerId.isBlank()) return "Partner Farmer"
        return try {
            firestore
                .collection("users")
                .document(farmerId)
                .get()
                .await()
                .getString("name") ?: "Partner Farmer"
        } catch (_: Exception) {
            "Partner Farmer"
        }
    }

    fun dismissPaymentPrompt() {
        _uiState.value = _uiState.value.copy(showPaymentPrompt = false)
    }
}

