package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FarmerProfileUiState(
    val farmerName: String = "John Farmer",
    val email: String = "farmer@example.com",
    val phone: String = "+254 712 345 678",
    val farmName: String = "Green Valley Farm",
    val location: String = "Nakuru County",
    val joinDate: String = "Joined Jan 2024",
    val totalProducts: Int = 12,
    val totalOrders: Int = 45,
    val totalRevenue: String = "KSh 125,000",
    val isLoading: Boolean = false
)

class FarmerProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FarmerProfileUiState())
    val uiState: StateFlow<FarmerProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // TODO: Load from Firebase
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun refresh() {
        loadProfile()
    }
}

