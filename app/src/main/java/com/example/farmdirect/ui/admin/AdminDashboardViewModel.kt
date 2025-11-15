package com.example.farmdirect.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.R
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

data class AdminDashboardUiState(
    val totalUsers: Int = 2847,
    val totalOrders: Int = 1234,
    val revenue: String = "Ksh 47.2K",
    val activeFarmers: Int = 186,
    val recentActivities: List<RecentActivity> = emptyList(),
    val isLoading: Boolean = false
)

class AdminDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        try {
            android.util.Log.d("AdminDashboardViewModel", "ViewModel init started")
            loadDashboardData()
        } catch (e: Exception) {
            android.util.Log.e("AdminDashboardViewModel", "Error in init: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                android.util.Log.d("AdminDashboardViewModel", "loadDashboardData started")
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Fetch real data from Firebase
                fetchMetrics()
                fetchRecentActivities()
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                android.util.Log.d("AdminDashboardViewModel", "loadDashboardData completed")
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboardViewModel", "Error in loadDashboardData: ${e.message}", e)
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun fetchMetrics() {
        try {
            android.util.Log.d("AdminDashboardViewModel", "fetchMetrics started")
            // Fetch users count
            FirebaseUtils.firestore.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    try {
                        val totalUsers = result.size()
                        val farmers = result.documents.count { it.getString("role") == "farmer" }
                        
                        _uiState.value = _uiState.value.copy(
                            totalUsers = totalUsers,
                            activeFarmers = farmers
                        )
                        android.util.Log.d("AdminDashboardViewModel", "Users fetched: $totalUsers, Farmers: $farmers")
                    } catch (e: Exception) {
                        android.util.Log.e("AdminDashboardViewModel", "Error processing users: ${e.message}", e)
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("AdminDashboardViewModel", "Error fetching users: ${e.message}", e)
                }

            // Fetch orders count and revenue
            FirebaseUtils.firestore.collection("orders")
                .get()
                .addOnSuccessListener { result ->
                    try {
                        val totalOrders = result.size()
                        val revenue = result.documents.sumOf { 
                            it.getDouble("totalAmount") ?: 0.0 
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            totalOrders = totalOrders,
                            revenue = "Ksh ${String.format("%.1fK", revenue / 1000)}"
                        )
                        android.util.Log.d("AdminDashboardViewModel", "Orders fetched: $totalOrders, Revenue: $revenue")
                    } catch (e: Exception) {
                        android.util.Log.e("AdminDashboardViewModel", "Error processing orders: ${e.message}", e)
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("AdminDashboardViewModel", "Error fetching orders: ${e.message}", e)
                }
        } catch (e: Exception) {
            android.util.Log.e("AdminDashboardViewModel", "Error in fetchMetrics: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun fetchRecentActivities() {
        try {
            android.util.Log.d("AdminDashboardViewModel", "fetchRecentActivities started")
            // Fetch recent activities from various collections
            val activities = mutableListOf<RecentActivity>()
            
            // Recent users
            FirebaseUtils.firestore.collection("users")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    try {
                        result.documents.firstOrNull()?.let { doc ->
                            val name = doc.getString("name") ?: "User"
                            val role = doc.getString("role") ?: "consumer"
                            activities.add(
                                RecentActivity(
                                    id = doc.id,
                                    title = "New user registered",
                                    description = "$name joined as a $role",
                                    timestamp = "2 minutes ago",
                                    iconRes = R.drawable.ic_seed,
                                    iconBgColor = Color(0xFFE3F2FD)
                                )
                            )
                            updateActivities(activities)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminDashboardViewModel", "Error processing recent users: ${e.message}", e)
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("AdminDashboardViewModel", "Error fetching recent users: ${e.message}", e)
                }

            // Recent orders
            FirebaseUtils.firestore.collection("orders")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    try {
                        result.documents.firstOrNull()?.let { doc ->
                            val orderNumber = doc.getString("orderNumber") ?: doc.id
                            val items = doc.getString("items") ?: "Order"
                            activities.add(
                                RecentActivity(
                                    id = doc.id,
                                    title = "New order placed",
                                    description = "Order #$orderNumber - $items",
                                    timestamp = "5 minutes ago",
                                    iconRes = R.drawable.ic_seed,
                                    iconBgColor = Color(0xFFE8F5E9)
                                )
                            )
                            updateActivities(activities)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminDashboardViewModel", "Error processing recent orders: ${e.message}", e)
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("AdminDashboardViewModel", "Error fetching recent orders: ${e.message}", e)
                }
        } catch (e: Exception) {
            android.util.Log.e("AdminDashboardViewModel", "Error in fetchRecentActivities: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun updateActivities(activities: List<RecentActivity>) {
        _uiState.value = _uiState.value.copy(
            recentActivities = activities.take(3)
        )
    }

    fun refresh() {
        loadDashboardData()
    }
}

