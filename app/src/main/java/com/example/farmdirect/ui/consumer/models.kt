package com.example.farmdirect.ui.consumer

import androidx.compose.ui.graphics.Color

data class CategoryUi(
    val name: String,
    val iconRes: Int,
    val bgColor: Color
)

data class ProductUi(
    val id: String,
    val name: String,
    val price: Double,
    val farmName: String,
    val category: String,
    val imageUrl: String? = null,
    val unit: String = "kg",
    val stock: Int = 0,
    val description: String = ""
)

data class WishlistItem(
    val id: String,
    val productId: String,
    val name: String,
    val price: Double,
    val farmName: String,
    val category: String,
    val imageUrl: String? = null,
    val unit: String = "kg",
    val stock: Int = 0
)

data class CartItem(
    val id: String,
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val unit: String = "kg",
    val imageUrl: String? = null,
    val farmerId: String = "",
    val farmerName: String = "",
    val availableStock: Int = Int.MAX_VALUE
)

data class Order(
    val id: String,
    val orderNumber: String,
    val productName: String,
    val supplier: String,
    val orderDate: String,
    val price: Double,
    val status: OrderStatus,
    val imageUrl: String? = null
)

enum class OrderStatus {
    PENDING,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}

enum class CheckoutStatus {
    IDLE,
    PROCESSING,
    SUCCESS,
    ERROR
}

data class PaymentMethod(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: Int,
    val isSelected: Boolean = false
)

data class DeliveryAddress(
    val id: String,
    val label: String,
    val location: String,
    val details: String,
    val latitude: Double = -1.2921, // Default to Nairobi center
    val longitude: Double = 36.8219
)

data class ProfileMenuItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val hasWarning: Boolean = false,
    val hasToggle: Boolean = false,
    val toggleState: Boolean = false,
    val onClick: () -> Unit
)

