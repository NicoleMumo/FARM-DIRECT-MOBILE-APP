package com.example.farmdirect.ui.admin

import androidx.compose.ui.graphics.Color

data class OverviewMetric(
    val title: String,
    val value: String,
    val growth: String,
    val iconRes: Int
)

data class RecentActivity(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val iconRes: Int,
    val iconBgColor: Color
)

data class AdminUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String, // "farmer", "consumer"
    val joinDate: String,
    val status: UserStatus
)

enum class UserStatus {
    ACTIVE,
    SUSPENDED,
    PENDING
}

data class AdminOrder(
    val id: String,
    val orderNumber: String,
    val farmerName: String,
    val consumerName: String,
    val items: String,
    val amount: Double,
    val dateTime: String,
    val status: AdminOrderStatus,
    val iconRes: Int
)

enum class AdminOrderStatus {
    PENDING,
    IN_TRANSIT,
    DELIVERED,
    COMPLETED,
    CANCELLED
}

data class AdminProduct(
    val id: String,
    val name: String,
    val seller: String,
    val price: String,
    val category: String,
    val status: ProductStatus,
    val iconRes: Int
)

enum class ProductStatus {
    PENDING,
    APPROVED,
    FLAGGED
}

