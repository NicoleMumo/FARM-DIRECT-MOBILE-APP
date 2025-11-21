package com.example.farmdirect.data

import com.example.farmdirect.model.Order
import com.example.farmdirect.model.Product
import com.example.farmdirect.models.User
import com.example.farmdirect.utils.FirebaseUtils
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class FarmDirectRepository {
    suspend fun getProductsForFarmer(farmerId: String): List<Product> {
        val snapshot = FirebaseUtils.firestore.collection("products")
            .whereEqualTo("farmerId", farmerId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { document ->
            document.toObject<Product>()?.copy(id = document.id)
        }
    }

    suspend fun getUser(userId: String): User? {
        return FirebaseUtils.firestore.collection("users").document(userId).get().await().toObject<User>()
    }

    suspend fun getOrdersForFarmer(farmerId: String): List<Order> {
        val snapshot = FirebaseUtils.firestore.collection("orders")
            .whereEqualTo("farmerId", farmerId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { document ->
            document.toObject<Order>()?.copy(id = document.id)
        }
    }

    suspend fun getProduct(productId: String): Product? {
        if (productId.isBlank()) return null
        val document = FirebaseUtils.firestore.collection("products").document(productId).get().await()
        return document.toObject<Product>()?.copy(id = document.id)
    }

    suspend fun addProduct(product: Product) {
        FirebaseUtils.firestore.collection("products").add(product).await()
    }

    suspend fun deleteProduct(productId: String) {
        if (productId.isBlank()) return
        FirebaseUtils.firestore.collection("products").document(productId).delete().await()
    }

    suspend fun updateProduct(product: Product) {
        if (product.id.isBlank()) return
        FirebaseUtils.firestore.collection("products").document(product.id).set(product).await()
    }
}
