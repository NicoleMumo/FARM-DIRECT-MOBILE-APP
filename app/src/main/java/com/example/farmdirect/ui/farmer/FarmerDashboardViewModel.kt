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
import java.util.TimeZone

data class FarmerDashboardUiState(
    val totalSales: String = "KSh 0",
    val salesGrowth: String = "+0%",
    val pendingOrders: Int = 0,
    val newPendingOrders: Int = 0,
    val restockingAlert: RestockingAlert? = null,
    val recentOrders: List<FarmerOrder> = emptyList(),
    val salesHistory: List<Pair<String, Double>> = emptyList(), // For chart: (week label, sales amount)
    val pendingHistory: List<Pair<String, Double>> = emptyList(), // For chart: (week label, pending count)
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FarmerDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FarmerDashboardUiState())
    val uiState: StateFlow<FarmerDashboardUiState> = _uiState.asStateFlow()

    private val farmerId = FirebaseUtils.auth.currentUser?.uid ?: ""
    private var ordersListener: ListenerRegistration? = null
    
    // Use Nairobi timezone for all date calculations
    private val nairobiTimeZone = TimeZone.getTimeZone("Africa/Nairobi")
    
    private fun getNairobiCalendar(): java.util.Calendar {
        return java.util.Calendar.getInstance(nairobiTimeZone, java.util.Locale.getDefault())
    }

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        if (farmerId.isBlank()) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        ordersListener?.remove()
        
        ordersListener = FirebaseUtils.firestore
            .collection("farmerOrders")
            .whereEqualTo("farmerId", farmerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load dashboard data"
                    )
                    return@addSnapshotListener
                }
                
                val allOrders = snapshot?.documents?.mapNotNull { document ->
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
                    val price = document.getDouble("price") ?: 0.0
                    
                    FarmerOrder(
                        id = document.id,
                        orderId = document.getString("orderId") ?: "",
                        orderItemId = document.getString("orderItemId") ?: document.id,
                        orderNumber = document.getString("orderNumber") ?: "Order",
                        productName = document.getString("productName") ?: "Produce",
                        quantity = "$quantity $unit",
                        quantityValue = quantity,
                        price = price,
                        customerName = document.getString("consumerName") ?: "Customer",
                        timeAgo = createdAt?.let { it.toRelativeTime() } ?: "Just now",
                        status = status,
                        iconRes = categoryToIcon(category),
                        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }.orEmpty() ?: emptyList()
                
                // Calculate total sales from DELIVERED orders only
                // Revenue = price * quantity (same calculation as Analytics)
                val deliveredOrders = allOrders.filter { it.status == FarmerOrderStatus.DELIVERED }
                val totalSales = deliveredOrders.sumOf { it.price * it.quantityValue }
                
                // Calculate pending orders
                val pendingOrders = allOrders.filter { it.status == FarmerOrderStatus.PENDING }
                val newPendingOrders = pendingOrders.size // Could enhance with timestamp check
                
                // Calculate sales growth (compare current month to previous month)
                val salesGrowth = calculateSalesGrowth(deliveredOrders)
                
                // Get recent orders (last 5)
                val recentOrders = allOrders.take(5)
                
                // Calculate sales history for chart (last 4 weeks)
                val salesHistory = calculateSalesHistory(deliveredOrders)
                
                // Calculate pending history for chart (last 4 weeks)
                val pendingHistory = calculatePendingHistory(allOrders)
                
                _uiState.value = _uiState.value.copy(
                    totalSales = "KSh ${String.format("%.2f", totalSales)}",
                    salesGrowth = salesGrowth,
                    pendingOrders = pendingOrders.size,
                    newPendingOrders = newPendingOrders,
                    recentOrders = recentOrders,
                    salesHistory = salesHistory,
                    pendingHistory = pendingHistory,
                    isLoading = false
                )
            }
    }
    
    private fun calculateSalesGrowth(deliveredOrders: List<FarmerOrder>): String {
        val calendar = getNairobiCalendar()
        val currentMonth = calendar.get(java.util.Calendar.MONTH)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        val currentMonthSales = deliveredOrders.filter { order ->
            calendar.timeInMillis = order.createdAt
            calendar.get(java.util.Calendar.MONTH) == currentMonth && 
            calendar.get(java.util.Calendar.YEAR) == currentYear
        }.sumOf { it.price * it.quantityValue }
        
        calendar.add(java.util.Calendar.MONTH, -1)
        val previousMonth = calendar.get(java.util.Calendar.MONTH)
        val previousYear = calendar.get(java.util.Calendar.YEAR)
        
        val previousMonthSales = deliveredOrders.filter { order ->
            calendar.timeInMillis = order.createdAt
            calendar.get(java.util.Calendar.MONTH) == previousMonth && 
            calendar.get(java.util.Calendar.YEAR) == previousYear
        }.sumOf { it.price * it.quantityValue }
        
        return if (previousMonthSales > 0) {
            val percentChange = ((currentMonthSales - previousMonthSales) / previousMonthSales * 100)
            if (percentChange >= 0) "+${String.format("%.1f", percentChange)}%" else "${String.format("%.1f", percentChange)}%"
        } else {
            if (currentMonthSales > 0) "+100%" else "+0%"
        }
    }
    
    private fun calculateSalesHistory(deliveredOrders: List<FarmerOrder>): List<Pair<String, Double>> {
        val calendar = getNairobiCalendar()
        val now = calendar.timeInMillis
        val weeks = mutableListOf<Pair<String, Double>>()
        
        for (i in 3 downTo 0) {
            val weekStart = now - (i * 7 * 24 * 60 * 60 * 1000L)
            val weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000L)
            
            val weekSales = deliveredOrders
                .filter { it.createdAt >= weekStart && it.createdAt < weekEnd }
                .sumOf { it.price * it.quantityValue }
            
            weeks.add(Pair("Week ${4 - i}", weekSales))
        }
        
        return weeks.ifEmpty {
            listOf(
                Pair("Week 1", 0.0),
                Pair("Week 2", 0.0),
                Pair("Week 3", 0.0),
                Pair("Week 4", 0.0)
            )
        }
    }
    
    private fun calculatePendingHistory(allOrders: List<FarmerOrder>): List<Pair<String, Double>> {
        val calendar = getNairobiCalendar()
        val now = calendar.timeInMillis
        val weeks = mutableListOf<Pair<String, Double>>()
        
        for (i in 3 downTo 0) {
            val weekStart = now - (i * 7 * 24 * 60 * 60 * 1000L)
            val weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000L)
            
            val weekPending = allOrders
                .filter { 
                    it.status == FarmerOrderStatus.PENDING &&
                    it.createdAt >= weekStart && it.createdAt < weekEnd
                }
                .size.toDouble()
            
            weeks.add(Pair("Week ${4 - i}", weekPending))
        }
        
        return weeks.ifEmpty {
            listOf(
                Pair("Week 1", 0.0),
                Pair("Week 2", 0.0),
                Pair("Week 3", 0.0),
                Pair("Week 4", 0.0)
            )
        }
    }

    fun refresh() {
        loadDashboardData()
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
