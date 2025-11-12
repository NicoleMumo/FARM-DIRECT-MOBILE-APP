package com.example.farmdirect.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.farmdirect.adapter.UserAdapter
import com.example.farmdirect.databinding.ActivityAdminDashboardBinding
import com.example.farmdirect.models.User
import com.example.farmdirect.utils.FirebaseUtils

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(userList)
        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
            adapter = userAdapter
        }
    }

    private fun fetchUsers() {
        FirebaseUtils.firestore.collection("users").get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("AdminDashboardActivity", "Error getting documents: ", exception)
            }
    }
}
