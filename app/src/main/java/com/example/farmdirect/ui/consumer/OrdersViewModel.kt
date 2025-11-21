package com.example.farmdirect.ui.consumer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedFilter: String? = null,
    val errorMessage: String? = null
)

class OrdersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()
    
    private var ordersListener: ValueEventListener? = null
    private val database = FirebaseUtils.database
    
    private fun currentUserId(): String? = FirebaseUtils.auth.currentUser?.uid
    
    init {
        observeOrders()
    }
    
    fun fetchOrders() {
        observeOrders()
    }
    
    private fun observeOrders() {
        val userId = currentUserId()
        if (userId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Please log in to view orders"
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        // Remove previous listener
        ordersListener?.let {
            database.reference.child("orders").child(userId).removeEventListener(it)
        }
        
        // Listen to Realtime Database for instant updates
        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()
                
                snapshot.children.forEach { orderSnapshot ->
                    val orderData = orderSnapshot.value as? Map<*, *> ?: return@forEach
                    val orderId = orderSnapshot.key ?: return@forEach
                    
                    val statusString = (orderData["status"] as? String) ?: "PENDING"
                    val status = when (statusString.uppercase(Locale.getDefault())) {
                        "PENDING" -> OrderStatus.PENDING
                        "PREPARED" -> OrderStatus.IN_TRANSIT
                        "IN_TRANSIT", "INTRANSIT" -> OrderStatus.IN_TRANSIT
                        "DELIVERED", "COMPLETED" -> OrderStatus.DELIVERED
                        "CANCELLED" -> OrderStatus.CANCELLED
                        else -> OrderStatus.PENDING
                    }
                    
                    val createdAt = (orderData["createdAt"] as? Long) ?: System.currentTimeMillis()
                    val dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(createdAt))
                    
                    orders.add(
                        Order(
                            id = orderId,
                            orderNumber = (orderData["orderNumber"] as? String) ?: "Order #FD${orderId.takeLast(7)}",
                            productName = (orderData["primaryProductName"] as? String)
                                ?: (orderData["items"] as? String)
                                ?: "Multiple items",
                            supplier = (orderData["farmerName"] as? String) ?: "Partner Farmer",
                            orderDate = dateString,
                            price = ((orderData["totalAmount"] as? Number)?.toDouble()) ?: 0.0,
                            status = status
                        )
                    )
                }
                
                // Sort by creation time (newest first)
                val sortedOrders = orders.sortedByDescending { order ->
                    snapshot.child(order.id).child("createdAt").getValue(Long::class.java) ?: 0L
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    orders = sortedOrders,
                    filteredOrders = sortedOrders
                )
                applyFilter(_uiState.value.selectedFilter)
            }
            
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load orders"
                )
            }
        }
        
        database.reference.child("orders").child(userId).addValueEventListener(ordersListener!!)
    }
    
    override fun onCleared() {
        super.onCleared()
        val userId = currentUserId()
        ordersListener?.let {
            if (userId != null) {
                database.reference.child("orders").child(userId).removeEventListener(it)
            }
        }
    }
    
    fun selectFilter(filter: String?) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyFilter(filter)
    }
    
    private fun applyFilter(filter: String?) {
        val orders = _uiState.value.orders
        val filtered = if (filter == null) {
            orders
        } else {
            orders.filter {
                when (filter) {
                    "Pending" -> it.status == OrderStatus.PENDING
                    "In Transit" -> it.status == OrderStatus.IN_TRANSIT
                    "Delivered" -> it.status == OrderStatus.DELIVERED
                    else -> true
                }
            }
        }
        _uiState.value = _uiState.value.copy(filteredOrders = filtered)
    }
    
    fun reorder(orderId: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId()
                if (userId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Please log in to reorder items"
                    )
                    return@launch
                }
                val orderItemsSnapshot = FirebaseUtils.firestore
                    .collection("orders")
                    .document(orderId)
                    .collection("items")
                    .get()
                    .await()
                
                orderItemsSnapshot.documents.forEach { itemDoc ->
                    val productId = itemDoc.getString("productId") ?: return@forEach
                    val quantity = itemDoc.getLong("quantity")?.toInt() ?: 1
                    
                    val existingCart = FirebaseUtils.firestore
                        .collection("cart")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("productId", productId)
                        .get()
                        .await()
                    
                    if (existingCart.documents.isNotEmpty()) {
                        val docId = existingCart.documents.first().id
                        val currentQty = existingCart.documents.first().getLong("quantity")?.toInt() ?: 1
                        FirebaseUtils.firestore
                            .collection("cart")
                            .document(docId)
                            .update("quantity", currentQty + quantity)
                            .await()
                    } else {
                        FirebaseUtils.firestore
                            .collection("cart")
                            .add(
                                mapOf(
                                    "userId" to userId,
                                    "productId" to productId,
                                    "quantity" to quantity
                                )
                            )
                            .await()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to reorder"
                )
            }
        }
    }
}

