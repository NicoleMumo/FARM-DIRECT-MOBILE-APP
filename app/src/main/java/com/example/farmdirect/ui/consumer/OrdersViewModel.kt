package com.example.farmdirect.ui.consumer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

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
    
    init {
        fetchOrders()
    }
    
    fun fetchOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = FirebaseUtils.firestore
                    .collection("orders")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                val orders = result.documents.mapNotNull { document ->
                    val productId = document.getString("productId") ?: return@mapNotNull null
                    val productDoc = FirebaseUtils.firestore
                        .collection("products")
                        .document(productId)
                        .get()
                        .await()
                    
                    val product = productDoc.toObject(com.example.farmdirect.model.Product::class.java)
                    val statusString = document.getString("status") ?: "PENDING"
                    val status = when (statusString) {
                        "PENDING" -> OrderStatus.PENDING
                        "IN_TRANSIT" -> OrderStatus.IN_TRANSIT
                        "DELIVERED" -> OrderStatus.DELIVERED
                        "CANCELLED" -> OrderStatus.CANCELLED
                        else -> OrderStatus.PENDING
                    }
                    
                    val timestamp = document.getTimestamp("orderDate")
                    val dateString = timestamp?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it.toDate())
                    } ?: "Unknown"
                    
                    product?.let {
                        Order(
                            id = document.id,
                            orderNumber = document.getString("orderNumber") ?: "Order #FD${document.id.takeLast(7)}",
                            productName = it.name,
                            supplier = "Green Valley Farm", // TODO: Get from farmer data
                            orderDate = dateString,
                            price = it.price,
                            status = status
                        )
                    }
                }.sortedByDescending { it.orderDate }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    orders = orders,
                    filteredOrders = orders
                )
                applyFilter(_uiState.value.selectedFilter)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load orders"
                )
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
                // TODO: Implement reorder logic - add items back to cart
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to reorder"
                )
            }
        }
    }
}

