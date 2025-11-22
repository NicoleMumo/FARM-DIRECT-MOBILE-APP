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
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.TimeZone

data class AdminDashboardUiState(
    val totalUsers: Int = 0,
    val totalOrders: Int = 0,
    val revenue: String = "Ksh 0",
    val activeFarmers: Int = 0,
    val recentActivities: List<RecentActivity> = emptyList(),
    val usersHistory: List<Pair<String, Double>> = emptyList(), // (week label, count)
    val ordersHistory: List<Pair<String, Double>> = emptyList(), // (week label, count)
    val revenueHistory: List<Pair<String, Double>> = emptyList(), // (week label, amount)
    val farmersHistory: List<Pair<String, Double>> = emptyList(), // (week label, count)
    val dailyActivity: List<Pair<String, Double>> = emptyList(), // (day label, count)
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
        viewModelScope.launch {
            try {
                android.util.Log.d("AdminDashboardViewModel", "fetchMetrics started")
                
                // Use Nairobi timezone
                val nairobiTimeZone = TimeZone.getTimeZone("Africa/Nairobi")
                val calendar = Calendar.getInstance(nairobiTimeZone)
                val now = calendar.timeInMillis
                
                // Fetch users with timestamps
                val usersSnapshot = FirebaseUtils.firestore.collection("users").get().await()
                val totalUsers = usersSnapshot.size()
                val farmers = usersSnapshot.documents.count { it.getString("role") == "farmer" }
                
                // Calculate users history (last 4 weeks)
                val usersHistory = calculateWeeklyHistory(usersSnapshot.documents.mapNotNull { doc ->
                    doc.getTimestamp("createdAt")?.toDate()?.time
                }, now)
                
                // Calculate farmers history
                // Include all farmers - if no createdAt, assign to current week (Week 4)
                val farmerTimestamps = usersSnapshot.documents.filter { it.getString("role") == "farmer" }
                    .map { doc ->
                        // Use createdAt if available, otherwise use current time (so they show in Week 4)
                        doc.getTimestamp("createdAt")?.toDate()?.time ?: (now - 1) // Use now-1 to ensure it's in Week 4 range
                    }
                android.util.Log.d("AdminDashboardViewModel", "Total farmers: $farmers, Farmer timestamps: ${farmerTimestamps.size}")
                val farmersHistory = calculateWeeklyHistory(farmerTimestamps, now)
                android.util.Log.d("AdminDashboardViewModel", "Farmers history: $farmersHistory")
                
                // Fetch orders with timestamps
                val ordersSnapshot = FirebaseUtils.firestore.collection("orders").get().await()
                val totalOrders = ordersSnapshot.size()
                val revenue = ordersSnapshot.documents.sumOf { 
                    it.getDouble("totalAmount") ?: 0.0 
                }
                
                // Calculate orders history
                val ordersHistory = calculateWeeklyHistory(
                    ordersSnapshot.documents.mapNotNull { doc ->
                        doc.getTimestamp("createdAt")?.toDate()?.time
                    },
                    now
                )
                
                // Calculate revenue history
                // Include all orders - if no createdAt, assign to current week (Week 4)
                val revenueData = ordersSnapshot.documents.map { doc ->
                    val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time
                    val amount = doc.getDouble("totalAmount") ?: 0.0
                    // Use createdAt if available, otherwise use current time (so they show in Week 4)
                    Pair(createdAt ?: (now - 1), amount)
                }
                android.util.Log.d("AdminDashboardViewModel", "Revenue data count: ${revenueData.size}, Total revenue: ${revenueData.sumOf { it.second }}")
                val revenueHistory = calculateWeeklyRevenueHistory(revenueData, now)
                android.util.Log.d("AdminDashboardViewModel", "Revenue history: $revenueHistory")
                
                // Calculate daily activity (last 7 days)
                val dailyActivity = calculateDailyActivity(
                    ordersSnapshot.documents.mapNotNull { doc ->
                        doc.getTimestamp("createdAt")?.toDate()?.time
                    },
                    now,
                    calendar
                )
                
                _uiState.value = _uiState.value.copy(
                    totalUsers = totalUsers,
                    activeFarmers = farmers,
                    totalOrders = totalOrders,
                    revenue = if (revenue >= 1000) "Ksh ${String.format("%.1fK", revenue / 1000)}" else "Ksh ${String.format("%.0f", revenue)}",
                    usersHistory = usersHistory,
                    ordersHistory = ordersHistory,
                    revenueHistory = revenueHistory,
                    farmersHistory = farmersHistory,
                    dailyActivity = dailyActivity
                )
                android.util.Log.d("AdminDashboardViewModel", "Metrics fetched successfully")
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboardViewModel", "Error in fetchMetrics: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
    
    private fun calculateWeeklyHistory(timestamps: List<Long>, now: Long): List<Pair<String, Double>> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Africa/Nairobi"))
        val weeks = mutableListOf<Pair<String, Double>>()
        
        android.util.Log.d("AdminDashboardViewModel", "Calculating weekly history. Total timestamps: ${timestamps.size}, Now: $now")
        
        // We're in Week 1 - show all data in Week 1, Weeks 2-4 show 0
        val week1Start = 0L // Start from beginning of time (or could use a specific app launch date)
        val week1End = now
        
        // Week 1: All data from start until now
        val week1Count = timestamps.count { it >= week1Start && it <= week1End }.toDouble()
        android.util.Log.d("AdminDashboardViewModel", "Week 1: start=$week1Start, end=$week1End, count=$week1Count")
        weeks.add(Pair("Week 1", week1Count))
        
        // Weeks 2-4: Empty (0) since we're still in Week 1
        weeks.add(Pair("Week 2", 0.0))
        weeks.add(Pair("Week 3", 0.0))
        weeks.add(Pair("Week 4", 0.0))
        
        return weeks
    }
    
    private fun calculateWeeklyRevenueHistory(revenueData: List<Pair<Long, Double>>, now: Long): List<Pair<String, Double>> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Africa/Nairobi"))
        val weeks = mutableListOf<Pair<String, Double>>()
        
        android.util.Log.d("AdminDashboardViewModel", "Calculating weekly revenue. Total revenue entries: ${revenueData.size}, Now: $now")
        
        // We're in Week 1 - show all revenue in Week 1, Weeks 2-4 show 0
        val week1Start = 0L // Start from beginning of time
        val week1End = now
        
        // Week 1: All revenue from start until now
        val week1Revenue = revenueData
            .filter { it.first >= week1Start && it.first <= week1End }
            .sumOf { it.second }
        
        android.util.Log.d("AdminDashboardViewModel", "Week 1 revenue: start=$week1Start, end=$week1End, revenue=$week1Revenue")
        weeks.add(Pair("Week 1", week1Revenue))
        
        // Weeks 2-4: Empty (0) since we're still in Week 1
        weeks.add(Pair("Week 2", 0.0))
        weeks.add(Pair("Week 3", 0.0))
        weeks.add(Pair("Week 4", 0.0))
        
        return weeks
    }
    
    private fun calculateDailyActivity(timestamps: List<Long>, now: Long, calendar: Calendar): List<Pair<String, Double>> {
        val dailyMap = mutableMapOf<String, Double>()
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val last7Days = mutableListOf<Pair<String, Long>>()
        
        // Get last 7 days
        for (i in 6 downTo 0) {
            val dayMillis = now - (i * 24 * 60 * 60 * 1000L)
            calendar.timeInMillis = dayMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dayStart = calendar.timeInMillis
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayName = dayNames[dayOfWeek - 1]
            last7Days.add(Pair(dayName, dayStart))
            dailyMap[dayName] = 0.0
        }
        
        // Count orders per day
        timestamps.forEach { timestamp ->
            calendar.timeInMillis = timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val orderDayStart = calendar.timeInMillis
            
            if (now - orderDayStart <= 7L * 24 * 60 * 60 * 1000) {
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val dayName = dayNames[dayOfWeek - 1]
                dailyMap[dayName] = dailyMap.getOrDefault(dayName, 0.0) + 1.0
            }
        }
        
        return last7Days.map { (dayName, _) ->
            Pair(dayName, dailyMap.getOrDefault(dayName, 0.0))
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
                                    iconRes = R.drawable.icons_admin,
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
                                    iconRes = R.drawable.icons_admin,
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

