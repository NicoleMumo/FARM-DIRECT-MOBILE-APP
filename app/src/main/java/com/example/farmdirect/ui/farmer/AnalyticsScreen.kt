package com.example.farmdirect.ui.farmer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AnalyticsRoute(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedChart by remember { mutableStateOf<String?>(null) }
    
    AnalyticsScreen(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onChartClick = { chartType -> selectedChart = chartType },
        onDismissChart = { selectedChart = null }
    )
    
    selectedChart?.let { chartType ->
        FarmerAnalyticsChartDialog(
            chartType = chartType,
            uiState = uiState,
            onDismiss = { selectedChart = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    onRefresh: () -> Unit,
    onChartClick: (String) -> Unit,
    onDismissChart: () -> Unit
) {
    var chartOrder by remember { mutableStateOf(listOf("Revenue Trends", "Best Selling Products", "Daily Sales", "Total Orders")) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        FarmerHeader(title = "Analytics")
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            }
        } else if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.errorMessage,
                    color = Color(0xFFD32F2F),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Retry")
                }
            }
        } else {
            // Organization Options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Organize:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Drag item to desired location to reorder charts",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chartOrder.forEachIndexed { index, chartName ->
                            DraggableChartChip(
                                label = chartName,
                                index = index,
                                onMove = { fromIndex, toIndex ->
                                    val newOrder = chartOrder.toMutableList()
                                    val item = newOrder.removeAt(fromIndex)
                                    newOrder.add(toIndex, item)
                                    chartOrder = newOrder
                                }
                            )
                        }
                    }
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Display charts in the order specified by chartOrder
                items(chartOrder.size) { index ->
                    val chartName = chartOrder[index]
                    when (chartName) {
                        "Revenue Trends" -> {
                            RevenueTrendsChartSummary(
                                revenueData = uiState.revenueData,
                                onClick = { onChartClick("revenue") }
                            )
                        }
                        "Best Selling Products" -> {
                            BestSellingProductsChartSummary(
                                products = uiState.bestSellingProducts,
                                onClick = { onChartClick("products") }
                            )
                        }
                        "Daily Sales" -> {
                            DailySalesChartSummary(
                                dailySales = uiState.dailySales,
                                onClick = { onChartClick("daily") }
                            )
                        }
                        "Total Orders" -> {
                            TotalOrdersChartSummary(
                                totalOrders = uiState.totalOrders,
                                change = uiState.totalOrdersChange,
                                onClick = { onChartClick("orders") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableChartChip(
    label: String,
    index: Int,
    onMove: (Int, Int) -> Unit
) {
    FilterChip(
        selected = false,
        onClick = { 
            // Move to front on click
            onMove(index, 0)
        },
        label = { 
            Text(
                text = label, 
                fontSize = 12.sp
            )
        },
        shape = RoundedCornerShape(24.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFF1F3F4),
            labelColor = Color.DarkGray
        ),
        modifier = Modifier
            .pointerInput(label) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Swap with adjacent chip based on drag direction
                        val maxIndex = 3 // 4 items (0-3)
                        if (dragAmount.x > 50 && index < maxIndex) {
                            onMove(index, index + 1)
                        } else if (dragAmount.x < -50 && index > 0) {
                            onMove(index, index - 1)
                        }
                    }
                )
            }
    )
}

@Composable
fun RevenueTrendsChartSummary(
    revenueData: List<RevenueData>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Summary
            val totalRevenue = revenueData.sumOf { it.revenue }
            Text(
                text = "Ksh ${String.format("%.0f", totalRevenue)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "Last 4 weeks",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun BestSellingProductsChartSummary(
    products: List<BestSellingProduct>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Best Selling Products",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Summary - show top 3
            products.take(3).forEach { product ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = product.name,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = product.revenue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
            if (products.isEmpty()) {
                Text(
                    text = "No sales yet",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DailySalesChartSummary(
    dailySales: List<DailySale>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Sales",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Summary
            val totalDailySales = dailySales.sumOf { it.sales }
            Text(
                text = "Ksh ${String.format("%.0f", totalDailySales)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "Last 7 days",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TotalOrdersChartSummary(
    totalOrders: Int,
    change: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Orders",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$totalOrders",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = change,
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun FarmerAnalyticsChartDialog(
    chartType: String,
    uiState: AnalyticsUiState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (chartType) {
                    "revenue" -> "Revenue Trends"
                    "products" -> "Best Selling Products"
                    "daily" -> "Daily Sales"
                    "orders" -> "Total Orders"
                    else -> "Chart"
                },
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (chartType) {
                    "revenue" -> {
                        // Revenue bar chart
                        val maxRevenue = uiState.revenueData.maxOfOrNull { it.revenue } ?: 20000.0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            uiState.revenueData.forEach { data ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val barHeight = ((data.revenue / maxRevenue) * 200).dp.coerceAtLeast(8.dp)
                                    Box(
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(barHeight)
                                            .background(Color(0xFFFFB300), RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = data.week,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    "products" -> {
                        // Best selling products list
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.bestSellingProducts) { product ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = product.name,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = product.revenue,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                    "daily" -> {
                        // Daily sales line chart (as bars)
                        val maxSales = uiState.dailySales.maxOfOrNull { it.sales } ?: 8000.0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            uiState.dailySales.forEach { sale ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val barHeight = ((sale.sales / maxSales) * 200).dp.coerceAtLeast(4.dp)
                                    Box(
                                        modifier = Modifier
                                            .width(30.dp)
                                            .height(barHeight)
                                            .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sale.day,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    "orders" -> {
                        // Total orders summary
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${uiState.totalOrders}",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "Total Orders",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.totalOrdersChange,
                                fontSize = 16.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF2E7D32))
            }
        }
    )
}
