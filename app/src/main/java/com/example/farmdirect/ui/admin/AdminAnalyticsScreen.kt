package com.example.farmdirect.ui.admin

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AdminAnalyticsRoute(
    dashboardViewModel: AdminDashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    var selectedChart by remember { mutableStateOf<String?>(null) }
    
    AdminAnalyticsScreen(
        uiState = uiState,
        onChartClick = { chartType -> selectedChart = chartType },
        onDismissChart = { selectedChart = null }
    )
    
    selectedChart?.let { chartType ->
        MetricChartDialog(
            chartType = chartType,
            uiState = uiState,
            onDismiss = { selectedChart = null }
        )
    }
}

@Composable
fun AdminAnalyticsScreen(
    uiState: AdminDashboardUiState,
    onChartClick: (String) -> Unit,
    onDismissChart: () -> Unit
) {
    var chartOrder by remember { mutableStateOf(listOf("Revenue", "Users", "Orders", "Daily Activity")) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        AdminHeader(title = "Analytics")
        
        // Organization Options - Draggable
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display charts in the order specified by chartOrder
            items(chartOrder) { chartName ->
                when (chartName) {
                    "Revenue" -> {
                        RevenueChartSummary(
                            revenue = uiState.revenue,
                            onClick = { onChartClick("revenue") }
                        )
                    }
                    "Users" -> {
                        UsersChartSummary(
                            totalUsers = uiState.totalUsers,
                            activeFarmers = uiState.activeFarmers,
                            onClick = { onChartClick("users") }
                        )
                    }
                    "Orders" -> {
                        OrdersChartSummary(
                            totalOrders = uiState.totalOrders,
                            onClick = { onChartClick("orders") }
                        )
                    }
                    "Daily Activity" -> {
                        DailyActivityChartSummary(
                            totalOrders = uiState.totalOrders,
                            onClick = { onChartClick("daily") }
                        )
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
        modifier = Modifier
            .pointerInput(label) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Swap with adjacent chip based on drag direction
                        if (dragAmount.x > 50 && index < 3) {
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
fun RevenueChartSummary(
    revenue: String,
    onClick: () -> Unit
) {
    ChartSummaryCard(
        title = "Revenue",
        value = revenue,
        change = "+15%",
        icon = Icons.Default.Menu,
        onClick = onClick
    )
}

@Composable
fun UsersChartSummary(
    totalUsers: Int,
    activeFarmers: Int,
    onClick: () -> Unit
) {
    ChartSummaryCard(
        title = "Users",
        value = "$totalUsers",
        subtitle = "$activeFarmers active farmers",
        change = "+12%",
        icon = Icons.Default.Person,
        onClick = onClick
    )
}

@Composable
fun OrdersChartSummary(
    totalOrders: Int,
    onClick: () -> Unit
) {
    ChartSummaryCard(
        title = "Orders",
        value = "$totalOrders",
        change = "+8%",
        icon = Icons.Default.ShoppingCart,
        onClick = onClick
    )
}

@Composable
fun DailyActivityChartSummary(
    totalOrders: Int,
    onClick: () -> Unit
) {
    ChartSummaryCard(
        title = "Daily Activity",
        value = "$totalOrders",
        change = "+5%",
        icon = Icons.Default.Menu,
        onClick = onClick
    )
}

@Composable
fun ChartSummaryCard(
    title: String,
    value: String,
    subtitle: String? = null,
    change: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = change,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50)
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View details",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

