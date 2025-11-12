package com.example.farmdirect.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "" // "farmer", "consumer", "admin"
)
