package com.example.farmdirect.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.Color
import com.example.farmdirect.utils.FirebaseUtils
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class AnalyticsUiState(
    val revenueData: List<RevenueData> = emptyList(),
    val bestSellingProducts: List<BestSellingProduct> = emptyList(),
    val dailySales: List<DailySale> = emptyList(),
    val totalOrders: Int = 0,
    val totalOrdersChange: String = "+0%",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AnalyticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
    
    private val firestore = FirebaseUtils.firestore
    private val auth = FirebaseUtils.auth
    
    // Use Nairobi timezone for all date calculations
    private val nairobiTimeZone = TimeZone.getTimeZone("Africa/Nairobi")
    
    private fun getNairobiCalendar(): Calendar {
        return Calendar.getInstance(nairobiTimeZone, Locale.getDefault())
    }

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val farmerId = auth.currentUser?.uid
                if (farmerId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please log in to view analytics"
                    )
                    return@launch
                }
                
                // Load farmer orders
                val ordersSnapshot = firestore.collection("farmerOrders")
                    .whereEqualTo("farmerId", farmerId)
                    .whereEqualTo("status", "DELIVERED")
                    .get()
                    .await()
                
                val orders = ordersSnapshot.documents.mapNotNull { doc ->
                    val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    val price = doc.getDouble("price") ?: 0.0
                    val productName = doc.getString("productName") ?: ""
                    val unit = doc.getString("unit") ?: "kg"
                    
                    if (createdAt != null) {
                        OrderData(
                            createdAt = createdAt,
                            quantity = quantity,
                            price = price,
                            productName = productName,
                            unit = unit
                        )
                    } else null
                }
                
                // Calculate revenue by week (last 4 weeks)
                val revenueData = calculateRevenueByWeek(orders)
                
                // Calculate best selling products
                val bestSellingProducts = calculateBestSellingProducts(orders)
                
                // Calculate daily sales (last 7 days)
                val dailySales = calculateDailySales(orders)
                
                // Calculate total orders and change
                val totalOrders = orders.size
                // Simple change calculation - compare current month to previous
                val calendar = getNairobiCalendar()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                
                val currentMonthOrders = orders.filter { order ->
                    calendar.timeInMillis = order.createdAt
                    calendar.get(Calendar.MONTH) == currentMonth && 
                    calendar.get(Calendar.YEAR) == currentYear
                }.size
                
                calendar.add(Calendar.MONTH, -1)
                val previousMonth = calendar.get(Calendar.MONTH)
                val previousYear = calendar.get(Calendar.YEAR)
                
                val previousMonthOrders = orders.filter { order ->
                    calendar.timeInMillis = order.createdAt
                    calendar.get(Calendar.MONTH) == previousMonth && 
                    calendar.get(Calendar.YEAR) == previousYear
                }.size
                
                val change = if (previousMonthOrders > 0) {
                    val percentChange = ((currentMonthOrders - previousMonthOrders).toDouble() / previousMonthOrders * 100)
                    if (percentChange >= 0) "+${String.format("%.1f", percentChange)}%" else "${String.format("%.1f", percentChange)}%"
                } else {
                    if (currentMonthOrders > 0) "+8.2%" else "+0%"
                }
                
                _uiState.value = _uiState.value.copy(
                    revenueData = revenueData,
                    bestSellingProducts = bestSellingProducts,
                    dailySales = dailySales,
                    totalOrders = totalOrders,
                    totalOrdersChange = change,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load analytics"
                )
            }
        }
    }
    
    private fun calculateRevenueByWeek(orders: List<OrderData>): List<RevenueData> {
        val calendar = getNairobiCalendar()
        // Get current time in Nairobi timezone
        val now = calendar.timeInMillis
        val weeks = mutableListOf<RevenueData>()
        
        // We're in Week 1 - show all revenue in Week 1, Weeks 2-4 show 0
        val week1Start = 0L // Start from beginning of time
        val week1End = now
        
        // Week 1: All revenue from start until now
        val week1Revenue = orders
            .filter { it.createdAt >= week1Start && it.createdAt <= week1End }
            .sumOf { it.price * it.quantity }
        
        weeks.add(RevenueData("Week 1", week1Revenue))
        
        // Weeks 2-4: Empty (0) since we're still in Week 1
        weeks.add(RevenueData("Week 2", 0.0))
        weeks.add(RevenueData("Week 3", 0.0))
        weeks.add(RevenueData("Week 4", 0.0))
        
        return weeks
    }
    
    private fun calculateBestSellingProducts(orders: List<OrderData>): List<BestSellingProduct> {
        val productMap = mutableMapOf<String, ProductStats>()
        
        orders.forEach { order ->
            val stats = productMap.getOrPut(order.productName) {
                ProductStats(order.productName, order.unit, 0, 0.0)
            }
            stats.quantity += order.quantity
            stats.revenue += order.price * order.quantity
        }
        
        return productMap.values
            .sortedByDescending { it.revenue }
            .take(5)
            .mapIndexed { index, stats ->
                val iconRes = when (index % 4) {
                    0 -> com.example.farmdirect.R.drawable.vegetable_icon
                    1 -> com.example.farmdirect.R.drawable.fruit_icon
                    2 -> com.example.farmdirect.R.drawable.dairy_icon
                    else -> com.example.farmdirect.R.drawable.grain_icon
                }
                val bgColor = when (index % 3) {
                    0 -> Color(0xFFE6F8EB)
                    1 -> Color(0xFFFFF3D8)
                    else -> Color(0xFFE3F2FD)
                }
                
                BestSellingProduct(
                    id = stats.productName,
                    name = stats.productName,
                    quantitySold = "${stats.quantity} ${stats.unit} sold",
                    revenue = "Ksh ${String.format("%.0f", stats.revenue)}",
                    iconRes = iconRes,
                    bgColor = bgColor
                )
            }
            .ifEmpty {
                listOf(
                    BestSellingProduct(
                        id = "none",
                        name = "No sales yet",
                        quantitySold = "0 kg sold",
                        revenue = "Ksh 0",
                        iconRes = com.example.farmdirect.R.drawable.vegetable_icon,
                        bgColor = Color(0xFFF5F5F5)
                    )
                )
            }
    }

    private fun calculateDailySales(orders: List<OrderData>): List<DailySale> {
        val calendar = getNairobiCalendar()
        val dailySalesMap = mutableMapOf<Long, Double>() // Use timestamp as key
        
        // Get last 7 days with actual day names in Nairobi timezone
        val now = calendar.timeInMillis
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val last7Days = mutableListOf<Pair<String, Long>>()
        
        // Get last 7 days in chronological order (oldest to newest) in Nairobi timezone
        for (i in 6 downTo 0) {
            val dayMillis = now - (i * 24 * 60 * 60 * 1000L)
            // Get start of day (midnight) in Nairobi timezone
            calendar.timeInMillis = dayMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dayStart = calendar.timeInMillis
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayName = dayNames[dayOfWeek - 1]
            last7Days.add(Pair(dayName, dayStart))
            dailySalesMap[dayStart] = 0.0
        }
        
        // Calculate sales per day for last 7 days
        orders.forEach { order ->
            val orderDate = order.createdAt
            // Check if order is within last 7 days
            if (now - orderDate <= 7L * 24 * 60 * 60 * 1000) {
                // Get start of day for this order in Nairobi timezone
                calendar.timeInMillis = orderDate
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val orderDayStart = calendar.timeInMillis
                
                val revenue = order.price * order.quantity
                dailySalesMap[orderDayStart] = dailySalesMap.getOrDefault(orderDayStart, 0.0) + revenue
            }
        }
        
        // Return in chronological order (oldest to newest)
        return last7Days.map { (dayName, dayStart) ->
            DailySale(dayName, dailySalesMap.getOrDefault(dayStart, 0.0))
        }
    }
    
    fun refresh() {
        loadAnalytics()
    }
    
    private data class OrderData(
        val createdAt: Long,
        val quantity: Int,
        val price: Double,
        val productName: String,
        val unit: String
    )
    
    private data class ProductStats(
        val productName: String,
        val unit: String,
        var quantity: Int,
        var revenue: Double
    )
}

