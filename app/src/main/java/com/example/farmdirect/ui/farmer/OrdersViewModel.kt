package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.R
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

data class OrdersUiState(
    val pendingCount: Int = 0,
    val preparedCount: Int = 0,
    val deliveredCount: Int = 0,
    val selectedFilter: FarmerOrderStatus? = null,
    val orders: List<FarmerOrder> = emptyList(),
    val isLoading: Boolean = false
)

class OrdersViewModel(private val repository: FarmDirectRepository = FarmDirectRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val farmerId = FirebaseUtils.auth.currentUser?.uid
            if (farmerId != null) {
                val orders = repository.getOrdersForFarmer(farmerId)
                val farmerOrders = orders.map { order ->
                    async {
                        val product = repository.getProduct(order.productId)
                        val customer = repository.getUser(order.customerId)
                        order.toFarmerOrder(product?.name, customer?.name)
                    }
                }.awaitAll()

                _uiState.value = _uiState.value.copy(
                    orders = farmerOrders,
                    pendingCount = farmerOrders.count { it.status == FarmerOrderStatus.PENDING },
                    preparedCount = farmerOrders.count { it.status == FarmerOrderStatus.PREPARED },
                    deliveredCount = farmerOrders.count { it.status == FarmerOrderStatus.DELIVERED },
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
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
        loadOrders()
    }
}

private fun Order.toFarmerOrder(productName: String?, customerName: String?): FarmerOrder {
    val farmerStatus = when (status) {
        OrderStatus.PENDING -> FarmerOrderStatus.PENDING
        OrderStatus.PREPARED -> FarmerOrderStatus.PREPARED
        OrderStatus.CONFIRMED -> FarmerOrderStatus.CONFIRMED
        OrderStatus.DELIVERED -> FarmerOrderStatus.DELIVERED
        OrderStatus.CANCELLED -> FarmerOrderStatus.CANCELLED
    }
    val iconRes = when (productName?.lowercase()) { // Assuming product name hints at category
        "carrots", "vegetables" -> R.drawable.vegetable_icon
        "milk" -> R.drawable.dairy_icon
        "apples", "fruits" -> R.drawable.fruit_icon
        "maize", "grains" -> R.drawable.grain_icon
        else -> R.drawable.ic_launcher_background
    }
    return FarmerOrder(
        id = id,
        orderNumber = orderNumber,
        productName = productName ?: "Unknown Product",
        quantity = "$quantity kg", // Assuming kg for now
        price = price,
        customerName = customerName ?: "Unknown Customer",
        rating = 4.5, // Placeholder
        timeAgo = "2 hours ago", // Placeholder
        status = farmerStatus,
        iconRes = iconRes
    )
}
