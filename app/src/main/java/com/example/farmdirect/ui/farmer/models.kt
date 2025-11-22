package com.example.farmdirect.ui.farmer

import androidx.compose.ui.graphics.Color

data class FarmerProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val unit: String, // "kg", "L", etc.
    val stock: Int,
    val category: String,
    val status: ProductStatus,
    val iconRes: Int
)

enum class ProductStatus {
    ACTIVE,
    LOW_STOCK,
    OUT_OF_STOCK
}

data class FarmerOrder(
    val id: String,
    val orderId: String,
    val orderItemId: String,
    val orderNumber: String,
    val productName: String,
    val quantity: String, // e.g., "5kg"
    val quantityValue: Int = 0, // Numeric quantity for calculations
    val price: Double,
    val customerName: String,
    val rating: Double? = null,
    val timeAgo: String,
    val status: FarmerOrderStatus,
    val iconRes: Int,
    val createdAt: Long = System.currentTimeMillis() // Timestamp for calculations
)

enum class FarmerOrderStatus {
    PENDING,
    PREPARED,
    CONFIRMED,
    DELIVERED,
    CANCELLED
}

data class DashboardMetric(
    val title: String,
    val value: String,
    val growth: String? = null,
    val newCount: String? = null,
    val iconRes: Int,
    val iconColor: Color,
    val growthColor: Color = Color(0xFF4CAF50)
)

data class RestockingAlert(
    val count: Int,
    val products: List<String>
)

data class BestSellingProduct(
    val id: String,
    val name: String,
    val quantitySold: String,
    val revenue: String,
    val iconRes: Int,
    val bgColor: Color
)

data class RevenueData(
    val week: String,
    val revenue: Double
)

data class DailySale(
    val day: String,
    val sales: Double
)

