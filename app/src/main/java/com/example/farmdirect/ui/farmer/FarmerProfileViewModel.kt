package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.data.FarmDirectRepository
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FarmerProfileUiState(
    val farmerName: String = "",
    val email: String = "",
    val phone: String = "",
    val farmName: String = "",
    val location: String = "",
    val joinDate: String = "",
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val totalRevenue: String = "",
    val isLoading: Boolean = false
)

class FarmerProfileViewModel(private val repository: FarmDirectRepository = FarmDirectRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(FarmerProfileUiState())
    val uiState: StateFlow<FarmerProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = FirebaseUtils.auth.currentUser?.uid
            if (userId != null) {
                val farmer = repository.getUser(userId)
                if (farmer != null) {
                    _uiState.value = _uiState.value.copy(
                        farmerName = farmer.name,
                        email = farmer.email,
                        isLoading = false
                    )
                }
            } else {
                // Handle user not logged in
            }
        }
    }

    fun refresh() {
        loadProfile()
    }
}
