package com.example.farmdirect.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.farmdirect.ui.admin.AdminDashboardRoute
import com.example.farmdirect.ui.theme.FarmDirectTheme

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d("AdminDashboardActivity", "onCreate started")
        
        setContent {
            FarmDirectTheme {
                AdminDashboardRoute()
            }
        }
        
        android.util.Log.d("AdminDashboardActivity", "setContent completed successfully")
    }
}
