package com.example.farmdirect.ui.farmer

import androidx.compose.ui.graphics.Color

data class FarmerProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val unit: String, // "kg", "L", etc.
    val stock: Double,
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
    val orderNumber: String,
    val productName: String,
    val quantity: String, // e.g., "5kg"
    val price: Double,
    val customerName: String,
    val customerAvatar: String? = null,
    val rating: Double? = null,
    val timeAgo: String,
    val status: FarmerOrderStatus,
    val iconRes: Int
)

enum class FarmerOrderStatus {
    PENDING,
    PREPARED,
    DELIVERED
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

