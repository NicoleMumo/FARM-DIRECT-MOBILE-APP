package com.example.farmdirect.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.farmdirect.adapter.ProductAdapter
import com.example.farmdirect.databinding.ActivityFarmerDashboardBinding
import com.example.farmdirect.model.Product
import com.example.farmdirect.utils.FirebaseUtils

class FarmerDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFarmerDashboardBinding
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val farmerId = FirebaseUtils.auth.currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFarmerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchProducts()

        binding.btnAddProduct.setOnClickListener {
            // TODO: Implement Add Product functionality
            Toast.makeText(this, "Add Product clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(productList)
        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FarmerDashboardActivity)
            adapter = productAdapter
        }
    }

    private fun fetchProducts() {
        FirebaseUtils.firestore.collection("products").whereEqualTo("farmerId", farmerId).get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("FarmerDashboardActivity", "Error getting documents: ", exception)
            }
    }
}
