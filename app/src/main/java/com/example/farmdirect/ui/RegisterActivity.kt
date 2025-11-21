package com.example.farmdirect.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.farmdirect.databinding.ActivityRegisterBinding
import com.example.farmdirect.models.User
import com.example.farmdirect.utils.FirebaseUtils
import com.google.firebase.Timestamp

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        val role = when {
            binding.rbFarmer.isChecked -> "farmer"
            binding.rbConsumer.isChecked -> "consumer"
            binding.rbAdmin.isChecked -> "admin"
            else -> ""
        }

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select role", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registering..."

        FirebaseUtils.auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = FirebaseUtils.auth.currentUser?.uid
                    if (uid != null) {
                        val user = User(uid, name, email, role)
                        saveUserToFirestore(user)
                    } else {
                        showError("Failed to get user ID after registration")
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Register"
                    }
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Registration failed"
                    showError("Registration failed: $errorMsg")
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                }
            }
    }

    private fun saveUserToFirestore(user: User) {
        FirebaseUtils.firestore.collection("users").document(user.uid).set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created successfully as ${user.role}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                showError("Failed to save user profile: ${e.localizedMessage}")
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Register"
            }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}
