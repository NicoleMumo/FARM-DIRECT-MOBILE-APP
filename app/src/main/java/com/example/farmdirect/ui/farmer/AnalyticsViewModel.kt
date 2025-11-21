package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

data class AnalyticsUiState(
    val revenueData: List<RevenueData> = emptyList(),
    val bestSellingProducts: List<BestSellingProduct> = emptyList(),
    val isLoading: Boolean = false
)

class AnalyticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // TODO: Load from Firebase
            val revenueData = listOf(
                RevenueData("Week 1", 9500.0),
                RevenueData("Week 2", 10500.0),
                RevenueData("Week 3", 16500.0),
                RevenueData("Week 4", 19500.0)
            )
            
            val bestSelling = listOf(
                BestSellingProduct(
                    id = "1",
                    name = "Fresh Tomatoes",
                    quantitySold = "45 kg sold",
                    revenue = "Ksh12,500",
                    iconRes = com.example.farmdirect.R.drawable.vegetable_icon,
                    bgColor = Color(0xFFE6F8EB)
                ),
                BestSellingProduct(
                    id = "2",
                    name = "Maize",
                    quantitySold = "120 kg sold",
                    revenue = "Ksh 8,900",
                    iconRes = com.example.farmdirect.R.drawable.grain_icon,
                    bgColor = Color(0xFFFFF3D8)
                ),
                BestSellingProduct(
                    id = "3",
                    name = "Carrots",
                    quantitySold = "32 kg sold",
                    revenue = "Ksh 6,400",
                    iconRes = com.example.farmdirect.R.drawable.vegetable_icon,
                    bgColor = Color(0xFFE6F8EB)
                )
            )
            
            _uiState.value = _uiState.value.copy(
                revenueData = revenueData,
                bestSellingProducts = bestSelling,
                isLoading = false
            )
        }
    }

    fun refresh() {
        loadAnalytics()
    }
}

