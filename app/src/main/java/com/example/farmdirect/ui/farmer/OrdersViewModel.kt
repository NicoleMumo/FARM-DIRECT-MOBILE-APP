package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrdersUiState(
    val pendingCount: Int = 2,
    val preparedCount: Int = 1,
    val deliveredCount: Int = 1,
    val selectedFilter: FarmerOrderStatus? = null,
    val orders: List<FarmerOrder> = emptyList(),
    val isLoading: Boolean = false
)

class OrdersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // TODO: Load from Firebase
            val mockOrders = listOf(
                FarmerOrder(
                    id = "1",
                    orderNumber = "12346",
                    productName = "Mixed Vegetables",
                    quantity = "3kg",
                    price = 180.0,
                    customerName = "John K.",
                    rating = 4.8,
                    timeAgo = "3 days ago",
                    status = FarmerOrderStatus.DELIVERED,
                    iconRes = com.example.farmdirect.R.drawable.vegetable_icon
                )
            )
            
            _uiState.value = _uiState.value.copy(
                orders = mockOrders,
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
        loadOrders()
    }
}

