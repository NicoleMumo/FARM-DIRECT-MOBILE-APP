package com.example.farmdirect.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.farmdirect.databinding.ActivityRegisterBinding
import com.example.farmdirect.models.User
import com.example.farmdirect.utils.FirebaseUtils

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityRegisterBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.btnRegister.setOnClickListener {
                registerUser()
            }
        } catch (e: Exception) {
            logError("onCreate", e)
            showErrorDialog("Error initializing registration: ${e.message}")
        }
    }

    private fun registerUser() {
        try {
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

            logInfo("Starting registration for: $email, role: $role")

            // Disable button to prevent multiple clicks
            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = "Registering..."

            FirebaseUtils.auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            logInfo("Firebase auth successful")
                            val uid = FirebaseUtils.auth.currentUser?.uid
                            if (uid != null) {
                                saveUserToFirestore(uid, name, email, role)
                            } else {
                                logError("registerUser", Exception("UID is null after successful auth"))
                                showError("Failed to get user ID after registration")
                                binding.btnRegister.isEnabled = true
                                binding.btnRegister.text = "Register"
                            }
                        } else {
                            val exception = task.exception
                            val errorMsg = exception?.localizedMessage ?: "Registration failed"
                            logError("registerUser - Auth failed", exception ?: Exception(errorMsg))
                            showError("Registration failed: $errorMsg")
                            binding.btnRegister.isEnabled = true
                            binding.btnRegister.text = "Register"
                        }
                    } catch (e: Exception) {
                        logError("registerUser - onComplete", e)
                        showError("Error during registration: ${e.message}")
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Register"
                    }
                }
        } catch (e: Exception) {
            logError("registerUser", e)
            showError("Error starting registration: ${e.message}")
            binding.btnRegister.isEnabled = true
            binding.btnRegister.text = "Register"
        }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String, role: String) {
        try {
            logInfo("Saving user to Firestore: UID=$uid, Role=$role")
            
            // Create user document with all fields explicitly
            val userData = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "role" to role,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            
            FirebaseUtils.firestore.collection("users").document(uid).set(userData)
                .addOnSuccessListener {
                    try {
                        logInfo("User saved successfully: UID=$uid, Role=$role")
                        Toast.makeText(this, "Account created successfully as $role", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } catch (e: Exception) {
                        logError("saveUserToFirestore - onSuccess", e)
                        showError("Error navigating after registration: ${e.message}")
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Register"
                    }
                }
                .addOnFailureListener { e ->
                    logError("saveUserToFirestore - Firestore error", e)
                    showError("Failed to save user profile: ${e.localizedMessage}\n\nYour account was created but profile couldn't be saved. Please try logging in.")
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                }
        } catch (e: Exception) {
            logError("saveUserToFirestore", e)
            showError("Error saving user: ${e.message}")
            binding.btnRegister.isEnabled = true
            binding.btnRegister.text = "Register"
        }
    }

    private fun logInfo(message: String) {
        android.util.Log.i("RegisterActivity", message)
        println("RegisterActivity: $message") // Also print to console
    }

    private fun logError(tag: String, exception: Exception?) {
        val errorMsg = exception?.message ?: "Unknown error"
        val stackTrace = exception?.stackTraceToString() ?: "No stack trace"
        android.util.Log.e("RegisterActivity", "$tag: $errorMsg", exception)
        println("RegisterActivity ERROR [$tag]: $errorMsg")
        println("Stack trace: $stackTrace")
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showErrorDialog(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
