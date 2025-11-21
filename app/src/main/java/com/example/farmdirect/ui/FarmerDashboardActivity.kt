package com.example.farmdirect.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.farmdirect.ui.farmer.FarmerDashboardRoute
import com.example.farmdirect.ui.theme.FarmDirectTheme

class FarmerDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmDirectTheme {
                FarmerDashboardRoute()
            }
        }
    }
}
