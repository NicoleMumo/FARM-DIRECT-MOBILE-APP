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
                if (documentSnapshot.exists()) {
                    // Get role from document
                    val role = documentSnapshot.getString("role") ?: "consumer"
                    
                    // Log for debugging
                    android.util.Log.d("LoginActivity", "User role: $role, UID: $uid")
                    
                    // Navigate to the correct dashboard based on the role
                    when (role.lowercase()) {
                        "farmer" -> {
                            startActivity(Intent(this, FarmerDashboardActivity::class.java))
                        }
                        "consumer" -> {
                            startActivity(Intent(this, ConsumerDashboardActivity::class.java))
                        }
                        "admin" -> {
                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                        }
                        else -> {
                            Toast.makeText(this, "Unknown role: $role. Logging in as consumer.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ConsumerDashboardActivity::class.java))
                        }
                    }
                } else {
                    // Document doesn't exist - create it with default role
                    android.util.Log.w("LoginActivity", "User document not found for UID: $uid")
                    Toast.makeText(this, "User profile not found. Please register again.", Toast.LENGTH_LONG).show()
                    FirebaseUtils.auth.signOut()
                }
                finish() // Close the LoginActivity so the user can't go back to it
            }
            .addOnFailureListener { exception ->
                // If reading from Firestore fails, show error and don't navigate
                android.util.Log.e("LoginActivity", "Error reading user role: ${exception.message}", exception)
                Toast.makeText(this, "Error verifying user role: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
