package com.example.farmdirect.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.farmdirect.adapter.ProductAdapter
import com.example.farmdirect.databinding.ActivityCustomerDashboardBinding
import com.example.farmdirect.model.Product
import com.example.farmdirect.utils.FirebaseUtils

class CustomerDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerDashboardBinding
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchAllProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(productList)
        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CustomerDashboardActivity)
            adapter = productAdapter
        }
    }

    private fun fetchAllProducts() {
        FirebaseUtils.firestore.collection("products").get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("CustomerDashboardActivity", "Error getting documents: ", exception)
            }
    }
}
