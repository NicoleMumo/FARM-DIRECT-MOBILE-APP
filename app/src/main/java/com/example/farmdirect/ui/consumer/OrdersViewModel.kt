package com.example.farmdirect.ui.consumer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.utils.FirebaseUtils
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
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
    
    private val userId = FirebaseUtils.auth.currentUser?.uid ?: ""
    private var ordersListener: ListenerRegistration? = null
    
    init {
        observeOrders()
    }
    
    fun fetchOrders() {
        observeOrders()
    }
    
    private fun observeOrders() {
        if (userId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Please log in to view orders"
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        ordersListener?.remove()
        ordersListener = FirebaseUtils.firestore
            .collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load orders"
                    )
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.map { document ->
                    val statusString = document.getString("status") ?: "PENDING"
                    val status = when (statusString.uppercase(Locale.getDefault())) {
                        "PENDING" -> OrderStatus.PENDING
                        "PREPARED" -> OrderStatus.IN_TRANSIT
                        "IN_TRANSIT", "INTRANSIT" -> OrderStatus.IN_TRANSIT
                        "DELIVERED", "COMPLETED" -> OrderStatus.DELIVERED
                        "CANCELLED" -> OrderStatus.CANCELLED
                        else -> OrderStatus.PENDING
                    }
                    val timestamp = document.getTimestamp("createdAt")
                    val dateString = timestamp?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it.toDate())
                    } ?: "Pending"
                    Order(
                        id = document.id,
                        orderNumber = document.getString("orderNumber") ?: "Order #FD${document.id.takeLast(7)}",
                        productName = document.getString("primaryProductName")
                            ?: document.getString("items")
                            ?: "Multiple items",
                        supplier = document.getString("farmerName") ?: "Partner Farmer",
                        orderDate = dateString,
                        price = document.getDouble("totalAmount") ?: 0.0,
                        status = status
                    )
                }.orEmpty()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    orders = orders,
                    filteredOrders = orders
                )
                applyFilter(_uiState.value.selectedFilter)
            }
    }
    
    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
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
                if (userId.isBlank()) {
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

