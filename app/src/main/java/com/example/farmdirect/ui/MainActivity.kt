package com.example.farmdirect.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.farmdirect.databinding.ActivityMainBinding
import com.example.farmdirect.utils.FirebaseUtils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            FirebaseUtils.auth.signOut()
            finish()
        }
    }
}


