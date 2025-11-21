package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.R
import com.example.farmdirect.utils.FirebaseUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

data class OrdersUiState(
    val pendingCount: Int = 0,
    val preparedCount: Int = 0,
    val deliveredCount: Int = 0,
    val selectedFilter: FarmerOrderStatus? = null,
    val orders: List<FarmerOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class OrdersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val farmerId = FirebaseUtils.auth.currentUser?.uid ?: ""
    private var ordersListener: ListenerRegistration? = null

    init {
        observeOrders()
    }

    private fun observeOrders() {
        if (farmerId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Please log in to view orders"
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        ordersListener?.remove()
        ordersListener = FirebaseUtils.firestore
            .collection("farmerOrders")
            .whereEqualTo("farmerId", farmerId)
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
                    val status = when (document.getString("status")?.uppercase()) {
                        "PREPARED" -> FarmerOrderStatus.PREPARED
                        "DELIVERED" -> FarmerOrderStatus.DELIVERED
                        "CANCELLED" -> FarmerOrderStatus.CANCELLED
                        else -> FarmerOrderStatus.PENDING
                    }
                    val quantity = document.getLong("quantity")?.toInt() ?: 0
                    val unit = document.getString("unit") ?: "kg"
                    val category = document.getString("category") ?: "vegetables"
                    val createdAt = document.getTimestamp("createdAt")
                    FarmerOrder(
                        id = document.id,
                        orderId = document.getString("orderId") ?: "",
                        orderItemId = document.getString("orderItemId") ?: document.id,
                        orderNumber = document.getString("orderNumber") ?: "Order",
                        productName = document.getString("productName") ?: "Produce",
                        quantity = "$quantity $unit",
                        price = document.getDouble("price") ?: 0.0,
                        customerName = document.getString("consumerName") ?: "Customer",
                        timeAgo = createdAt?.let { it.toRelativeTime() } ?: "Just now",
                        status = status,
                        iconRes = categoryToIcon(category)
                    )
                }.orEmpty()

                _uiState.value = _uiState.value.copy(
                    orders = orders,
                    pendingCount = orders.count { it.status == FarmerOrderStatus.PENDING },
                    preparedCount = orders.count { it.status == FarmerOrderStatus.PREPARED },
                    deliveredCount = orders.count { it.status == FarmerOrderStatus.DELIVERED },
                    isLoading = false
                )
            }
    }

    fun selectFilter(status: FarmerOrderStatus?) {
        _uiState.value = _uiState.value.copy(selectedFilter = status)
    }

    fun getFilteredOrders(): List<FarmerOrder> {
        val filter = _uiState.value.selectedFilter
        return if (filter == null) {
            _uiState.value.orders
        } else {
            _uiState.value.orders.filter { it.status == filter }
        }
    }

    fun refresh() {
        observeOrders()
    }

    fun updateOrderStatus(order: FarmerOrder, newStatus: FarmerOrderStatus) {
        if (order.orderId.isBlank()) return
        viewModelScope.launch {
            try {
                val statusString = when (newStatus) {
                    FarmerOrderStatus.PENDING -> "PENDING"
                    FarmerOrderStatus.PREPARED -> "PREPARED"
                    FarmerOrderStatus.CONFIRMED -> "IN_TRANSIT"
                    FarmerOrderStatus.DELIVERED -> "DELIVERED"
                    FarmerOrderStatus.CANCELLED -> "CANCELLED"
                }
                val batch = FirebaseUtils.firestore.batch()
                val farmerOrderRef = FirebaseUtils.firestore
                    .collection("farmerOrders")
                    .document(order.id)
                batch.update(
                    farmerOrderRef,
                    mapOf(
                        "status" to statusString,
                        "updatedAt" to Timestamp.now()
                    )
                )

                if (order.orderItemId.isNotBlank()) {
                    val orderItemRef = FirebaseUtils.firestore
                        .collection("orders")
                        .document(order.orderId)
                        .collection("items")
                        .document(order.orderItemId)
                    batch.update(orderItemRef, "status", statusString)
                }

                batch.commit().await()
                recalculateConsumerOrderStatus(order.orderId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to update order"
                )
            }
        }
    }

    private fun recalculateConsumerOrderStatus(orderId: String) {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseUtils.firestore
                    .collection("orders")
                    .document(orderId)
                    .collection("items")
                    .get()
                    .await()
                val statuses = snapshot.documents.mapNotNull { it.getString("status") }
                if (statuses.isEmpty()) return@launch
                val aggregate = when {
                    statuses.any { it == "CANCELLED" } -> "CANCELLED"
                    statuses.any { it == "PENDING" } -> "PENDING"
                    statuses.any { it == "PREPARED" || it == "IN_TRANSIT" } -> "IN_TRANSIT"
                    statuses.all { it == "DELIVERED" } -> "DELIVERED"
                    else -> "PENDING"
                }
                FirebaseUtils.firestore
                    .collection("orders")
                    .document(orderId)
                    .update("status", aggregate)
                    .await()
            } catch (_: Exception) {
                // Ignore aggregation failure; consumer view will retry on next update
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
    }
}

private fun Timestamp.toRelativeTime(): String {
    val diffMillis = System.currentTimeMillis() - this.toDate().time
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 1440 -> "${TimeUnit.MINUTES.toHours(minutes)} hr ago"
        else -> "${TimeUnit.MINUTES.toDays(minutes)} d ago"
    }
}

private fun categoryToIcon(category: String): Int {
    return when (category.lowercase()) {
        "vegetables" -> R.drawable.vegetable_icon
        "fruits" -> R.drawable.fruit_icon
        "dairy" -> R.drawable.dairy_icon
        "grains" -> R.drawable.grain_icon
        else -> R.drawable.ic_launcher_background
    }
}