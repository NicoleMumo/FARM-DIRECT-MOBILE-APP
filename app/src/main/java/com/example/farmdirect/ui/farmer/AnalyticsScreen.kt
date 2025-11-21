package com.example.farmdirect.ui.farmer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirect.R

@Composable
fun AnalyticsRoute(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    AnalyticsScreen(
        uiState = uiState,
        onRefresh = viewModel::refresh
    )
}

@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        FarmerHeader(title = "Analytics")
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Revenue Trends Card
            RevenueTrendsCard(revenueData = uiState.revenueData)
            
            // Best Selling Products
            Text(
                text = "Best Selling Products",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.bestSellingProducts) { product ->
                    BestSellingProductCard(product = product)
                }
            }
        }
    }
}

@Composable
fun RevenueTrendsCard(revenueData: List<RevenueData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Revenue Trends",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            
            // Simple Bar Chart
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Y-axis labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0", fontSize = 12.sp, color = Color.Gray)
                    Text("10k", fontSize = 12.sp, color = Color.Gray)
                    Text("20k", fontSize = 12.sp, color = Color.Gray)
                }
                
                // Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    revenueData.forEach { data ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            val maxRevenue = revenueData.maxOfOrNull { it.revenue } ?: 20000.0
                            val barHeight = (data.revenue / maxRevenue * 120).dp.coerceAtLeast(8.dp)
                            
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(barHeight)
                                    .background(Color(0xFFFF9800), RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = data.week,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BestSellingProductCard(product: BestSellingProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(product.bgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = product.iconRes),
                    contentDescription = product.name,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = product.quantitySold,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            // Revenue
            Text(
                text = product.revenue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

