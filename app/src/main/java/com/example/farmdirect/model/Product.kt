package com.example.farmdirect.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val farmerId: String = "",
    val unit: String = "",
    val stock: Double = 0.0,
    val category: String = ""
)
