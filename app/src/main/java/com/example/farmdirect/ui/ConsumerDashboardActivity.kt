package com.example.farmdirect.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.farmdirect.ui.consumer.ConsumerHomeRoute
import com.example.farmdirect.ui.theme.FarmDirectTheme

class ConsumerDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmDirectTheme {
                ConsumerHomeRoute()
            }
        }
    }
}
