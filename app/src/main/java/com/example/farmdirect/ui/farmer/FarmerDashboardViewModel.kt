package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.data.FarmDirectRepository
import com.example.farmdirect.model.Order
import com.example.farmdirect.model.OrderStatus
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FarmerDashboardUiState(
    val totalSales: String = "KSh 0",
    val salesGrowth: String = "+0%",
    val pendingOrders: Int = 0,
    val newPendingOrders: Int = 0,
    val restockingAlert: RestockingAlert? = null,
    val recentOrders: List<FarmerOrder> = emptyList(),
    val isLoading: Boolean = false
)

class FarmerDashboardViewModel(private val repository: FarmDirectRepository = FarmDirectRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(FarmerDashboardUiState())
    val uiState: StateFlow<FarmerDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = FirebaseUtils.auth.currentUser?.uid
            if (userId != null) {
                val orders = repository.getOrdersForFarmer(userId)
                val farmerOrders = orders.map { order ->
                    async {
                        val product = repository.getProduct(order.productId)
                        val customer = repository.getUser(order.customerId)
                        FarmerOrder(
                            id = order.id,
                            orderNumber = order.orderNumber,
                            productName = product?.name ?: "",
                            quantity = "${order.quantity} kg", // Assuming kg for now
                            price = order.price,
                            customerName = customer?.name ?: "",
                            rating = 4.5, // Placeholder
                            timeAgo = "2 hours ago", // Placeholder
                            status = if (order.status == OrderStatus.PENDING) FarmerOrderStatus.PENDING else FarmerOrderStatus.DELIVERED,
                            iconRes = com.example.farmdirect.R.drawable.grain_icon // Placeholder
                        )
                    }
                }.awaitAll()

                val totalSales = orders.sumOf { it.price }
                val pendingOrders = orders.count { it.status == OrderStatus.PENDING }

                _uiState.value = _uiState.value.copy(
                    recentOrders = farmerOrders,
                    totalSales = "KSh $totalSales",
                    pendingOrders = pendingOrders,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}
