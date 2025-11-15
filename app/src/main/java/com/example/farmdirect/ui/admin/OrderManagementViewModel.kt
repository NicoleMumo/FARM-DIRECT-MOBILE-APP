package com.example.farmdirect.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.R
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OrderManagementUiState(
    val totalOrders: Int = 248,
    val pendingOrders: Int = 32,
    val orders: List<AdminOrder> = emptyList(),
    val selectedFilter: String = "All Orders",
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class OrderManagementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrderManagementUiState())
    val uiState: StateFlow<OrderManagementUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        FirebaseUtils.firestore.collection("orders")
            .get()
            .addOnSuccessListener { result ->
                val orders = result.documents.mapNotNull { doc ->
                    val orderNumber = doc.getString("orderNumber") ?: doc.id
                    val farmerName = doc.getString("farmerName") ?: "Unknown Farmer"
                    val consumerName = doc.getString("consumerName") ?: "Unknown Consumer"
                    val items = doc.getString("items") ?: "Order items"
                    val amount = doc.getDouble("totalAmount") ?: 0.0
                    val createdAt = doc.getTimestamp("createdAt")?.toDate()
                    val dateTime = if (createdAt != null) {
                        formatDateTime(createdAt)
                    } else {
                        "Today, 2:30 PM"
                    }
                    val status = when (doc.getString("status")?.uppercase()) {
                        "PENDING" -> AdminOrderStatus.PENDING
                        "IN_TRANSIT", "IN TRANSIT" -> AdminOrderStatus.IN_TRANSIT
                        "DELIVERED" -> AdminOrderStatus.DELIVERED
                        "COMPLETED" -> AdminOrderStatus.COMPLETED
                        "CANCELLED" -> AdminOrderStatus.CANCELLED
                        else -> AdminOrderStatus.PENDING
                    }
                    
                    AdminOrder(
                        id = doc.id,
                        orderNumber = orderNumber,
                        farmerName = farmerName,
                        consumerName = consumerName,
                        items = items,
                        amount = amount,
                        dateTime = dateTime,
                        status = status,
                        iconRes = R.drawable.ic_seed
                    )
                }
                
                val pending = orders.count { it.status == AdminOrderStatus.PENDING }
                
                _uiState.value = _uiState.value.copy(
                    orders = orders,
                    totalOrders = orders.size,
                    pendingOrders = pending,
                    isLoading = false
                )
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
    }

    private fun formatDateTime(date: java.util.Date): String {
        val now = java.util.Date()
        val diff = now.time - date.time
        val minutes = diff / (1000 * 60)
        
        return when {
            minutes < 60 -> "Today, ${java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(date)}"
            minutes < 1440 -> "Yesterday, ${java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(date)}"
            else -> java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)
        }
    }

    fun onFilterSelected(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredOrders(): List<AdminOrder> {
        var filtered = _uiState.value.orders
        
        // Apply status filter
        when (_uiState.value.selectedFilter) {
            "Today" -> {
                val today = java.util.Date()
                filtered = filtered.filter {
                    it.dateTime.startsWith("Today")
                }
            }
            "Pending" -> {
                filtered = filtered.filter { it.status == AdminOrderStatus.PENDING }
            }
            "Completed" -> {
                filtered = filtered.filter { 
                    it.status == AdminOrderStatus.DELIVERED || 
                    it.status == AdminOrderStatus.COMPLETED 
                }
            }
        }
        
        // Apply search filter
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.orderNumber.lowercase().contains(query) ||
                it.farmerName.lowercase().contains(query) ||
                it.consumerName.lowercase().contains(query) ||
                it.items.lowercase().contains(query)
            }
        }
        
        return filtered
    }
}

