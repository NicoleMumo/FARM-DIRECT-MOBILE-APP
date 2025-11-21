package com.example.farmdirect.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val farmerId: String = "",
    val farmerName: String = "",
    val category: String = "",
    val unit: String = "kg",
    val stock: Int = 0,
    val imageUrl: String? = null
)
