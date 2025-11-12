package com.example.farmdirect.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.farmdirect.databinding.ActivityRegisterBinding
import com.example.farmdirect.models.User
import com.example.farmdirect.utils.FirebaseUtils

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
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
                return@setOnClickListener
            }

            FirebaseUtils.auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = FirebaseUtils.auth.currentUser!!.uid
                        val user = User(uid, name, email, role)
                        FirebaseUtils.firestore.collection("users").document(uid).set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, task.exception?.localizedMessage ?: "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
