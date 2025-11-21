package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FarmerDashboardUiState(
    val totalSales: String = "KSh 45,200",
    val salesGrowth: String = "+12%",
    val pendingOrders: Int = 12,
    val newPendingOrders: Int = 5,
    val restockingAlert: RestockingAlert? = RestockingAlert(
        count = 3,
        products = listOf("Maize", "Milk", "Tomatoes")
    ),
    val recentOrders: List<FarmerOrder> = emptyList(),
    val isLoading: Boolean = false
)

class FarmerDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FarmerDashboardUiState())
    val uiState: StateFlow<FarmerDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // TODO: Load from Firebase
            val mockOrders = listOf(
                FarmerOrder(
                    id = "1",
                    orderNumber = "12345",
                    productName = "Fresh Maize",
                    quantity = "5kg",
                    price = 500.0,
                    customerName = "Mary K.",
                    rating = 4.8,
                    timeAgo = "2 hours ago",
                    status = FarmerOrderStatus.PENDING,
                    iconRes = com.example.farmdirect.R.drawable.grain_icon
                ),
                FarmerOrder(
                    id = "2",
                    orderNumber = "12344",
                    productName = "Fresh Milk",
                    quantity = "2L",
                    price = 200.0,
                    customerName = "James M.",
                    rating = 4.5,
                    timeAgo = "1 day ago",
                    status = FarmerOrderStatus.DELIVERED,
                    iconRes = com.example.farmdirect.R.drawable.dairy_icon
                )
            )
            
            _uiState.value = _uiState.value.copy(
                recentOrders = mockOrders,
                isLoading = false
            )
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}

