package com.example.farmdirect.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.farmdirect.databinding.ActivityLoginBinding
import com.example.farmdirect.utils.FirebaseUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listener for the Login Button
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        // Listener for the "Go to Register" text
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        // Get email and password from the EditText fields
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()

        // Simple validation to ensure fields are not empty
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return // Stop the function here
        }

        // Use Firebase Authentication to sign the user in
        FirebaseUtils.auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login was successful. Now, find out the user's role from Firestore.
                    val uid = FirebaseUtils.auth.currentUser?.uid
                    if (uid != null) {
                        checkUserRoleAndNavigate(uid)
                    } else {
                        // This is unlikely to happen if login is successful, but it's good practice to handle it.
                        Toast.makeText(this, "Could not get user ID.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Login failed. Show a more specific error message from Firebase.
                    val errorMessage = task.exception?.localizedMessage ?: "Login failed. Please check your credentials."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserRoleAndNavigate(uid: String) {
        FirebaseUtils.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { documentSnapshot ->
                // Default to "consumer" if the role is not found
                val role = documentSnapshot.getString("role") ?: "consumer"

                // Navigate to the correct dashboard based on the role
                when (role) {
                    "farmer" -> startActivity(Intent(this, FarmerDashboardActivity::class.java))
                    "consumer" -> startActivity(Intent(this, CustomerDashboardActivity::class.java))
                    "admin" -> startActivity(Intent(this, AdminDashboardActivity::class.java))
                    else -> startActivity(Intent(this, CustomerDashboardActivity::class.java)) // Fallback
                }
                finish() // Close the LoginActivity so the user can't go back to it
            }
            .addOnFailureListener {
                // If reading from Firestore fails, fallback to the default dashboard
                Toast.makeText(this, "Could not verify user role. Logging in as customer.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CustomerDashboardActivity::class.java))
                finish()
            }
    }
}
