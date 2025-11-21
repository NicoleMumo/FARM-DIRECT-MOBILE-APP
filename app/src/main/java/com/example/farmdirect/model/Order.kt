package com.example.farmdirect.model

import com.google.firebase.Timestamp

enum class OrderStatus {
    PENDING, PREPARED, CONFIRMED, DELIVERED, CANCELLED
}

data class Order(
    val id: String = "",
    val orderNumber: String = "",
    val productId: String = "",
    val customerId: String = "",
    val farmerId: String = "",
    val quantity: Double = 0.0,
    val price: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now()
)
