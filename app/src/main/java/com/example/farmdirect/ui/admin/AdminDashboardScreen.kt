package com.example.farmdirect.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirect.R

@Composable
fun AdminDashboardRoute(
    dashboardViewModel: AdminDashboardViewModel = viewModel(),
    userManagementViewModel: UserManagementViewModel = viewModel(),
    orderManagementViewModel: OrderManagementViewModel = viewModel(),
    productManagementViewModel: ProductManagementViewModel = viewModel(),
    profileViewModel: AdminProfileViewModel = viewModel()
) {
    var selectedBottomNavItem by remember { mutableStateOf("Dashboard") }
    
    Scaffold(
        bottomBar = {
            AdminBottomNavigationBar(
                selectedItem = selectedBottomNavItem,
                onItemSelected = { selectedBottomNavItem = it }
            )
        },
        containerColor = Color(0xFFF7F9FA)
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color(0xFFF7F9FA)
        ) {
            when (selectedBottomNavItem) {
                "Dashboard" -> {
                    val uiState by dashboardViewModel.uiState.collectAsState()
                    AdminDashboardScreen(
                        uiState = uiState,
                        onRefresh = dashboardViewModel::refresh,
                        onViewAnalytics = { selectedBottomNavItem = "Analytics" }
                    )
                }
                "Users" -> {
                    UserManagementRoute(viewModel = userManagementViewModel)
                }
                "Orders" -> {
                    OrderManagementRoute(viewModel = orderManagementViewModel)
                }
                "Products" -> {
                    ProductManagementRoute(viewModel = productManagementViewModel)
                }
                "Analytics" -> {
                    AdminAnalyticsRoute(dashboardViewModel = dashboardViewModel)
                }
                "Profile" -> {
                    AdminProfileRoute(viewModel = profileViewModel)
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(
    uiState: AdminDashboardUiState,
    onRefresh: () -> Unit,
    onViewAnalytics: () -> Unit
) {
    var selectedChart by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        AdminHeader(
            title = "Farm Direct Admin"
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    TextButton(onClick = onViewAnalytics) {
                        Text(
                            text = "View Analytics",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            item {
                OverviewMetricsGrid(
                    uiState = uiState,
                    onMetricClick = { chartType -> selectedChart = chartType }
                )
            }
            
            // Recent Activity Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    TextButton(onClick = { /* View All */ }) {
                        Text(
                            text = "View All",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            items(uiState.recentActivities) { activity ->
                RecentActivityItem(activity = activity)
            }
        }
    }
    
    // Show chart dialog when a metric is clicked
    selectedChart?.let { chartType ->
        MetricChartDialog(
            chartType = chartType,
            uiState = uiState,
            onDismiss = { selectedChart = null }
        )
    }
}

@Composable
fun AdminHeader(
    title: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2E7D32),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Logo - using a colored circle with icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFF8B400), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_seed),
                        contentDescription = "Logo",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OverviewMetricsGrid(
    uiState: AdminDashboardUiState,
    onMetricClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverviewMetricCard(
                title = "Total Users",
                value = uiState.totalUsers.toString(),
                growth = "+12%",
                iconRes = R.drawable.ic_seed,
                modifier = Modifier.weight(1f),
                onClick = { onMetricClick("users") }
            )
            OverviewMetricCard(
                title = "Total Orders",
                value = uiState.totalOrders.toString(),
                growth = "+8%",
                iconRes = R.drawable.ic_seed,
                modifier = Modifier.weight(1f),
                onClick = { onMetricClick("orders") }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverviewMetricCard(
                title = "Revenue",
                value = uiState.revenue,
                growth = "+15%",
                iconRes = R.drawable.ic_seed,
                modifier = Modifier.weight(1f),
                onClick = { onMetricClick("revenue") }
            )
            OverviewMetricCard(
                title = "Active Farmers",
                value = uiState.activeFarmers.toString(),
                growth = "+5%",
                iconRes = R.drawable.ic_seed,
                modifier = Modifier.weight(1f),
                onClick = { onMetricClick("farmers") }
            )
        }
    }
}

@Composable
fun OverviewMetricCard(
    title: String,
    value: String,
    growth: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = growth,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50)
                )
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun MetricChartDialog(
    chartType: String,
    uiState: AdminDashboardUiState,
    onDismiss: () -> Unit
) {
    var sortOrder by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (chartType) {
                        "users" -> "Total Users Trend"
                        "orders" -> "Total Orders Trend"
                        "revenue" -> "Revenue Trend"
                        "farmers" -> "Active Farmers Trend"
                        else -> "Chart"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                IconButton(onClick = { sortOrder = !sortOrder }) {
                    Text(
                        text = if (sortOrder) "↑" else "↓",
                        fontSize = 20.sp,
                        color = Color(0xFFFFB300),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Simple bar chart
                val rawDataPoints = when (chartType) {
                    "users" -> listOf(2500.0, 2600.0, 2700.0, uiState.totalUsers.toDouble())
                    "orders" -> listOf(1000.0, 1100.0, 1200.0, uiState.totalOrders.toDouble())
                    "revenue" -> listOf(40.0, 42.0, 45.0, uiState.revenue.replace("Ksh ", "").replace("K", "").toDoubleOrNull() ?: 47.2)
                    "farmers" -> listOf(150.0, 165.0, 175.0, uiState.activeFarmers.toDouble())
                    else -> emptyList<Double>()
                }
                
                val labels = listOf("Week 1", "Week 2", "Week 3", "Current")
                val dataWithLabels = rawDataPoints.mapIndexed { index, value ->
                    Pair(labels[index], value)
                }
                
                val dataPoints = if (sortOrder) {
                    dataWithLabels.sortedByDescending { it.second }
                } else {
                    dataWithLabels
                }
                
                if (dataPoints.isNotEmpty()) {
                    val maxValue = dataPoints.maxOfOrNull { it.second } ?: 1.0
                    
                    // Chart area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        // Y-axis labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                if (chartType == "revenue") "Ksh ${String.format("%.1fK", maxValue)}"
                                else maxValue.toInt().toString(),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Bars
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dataPoints.forEach { (label, value) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val barHeight = ((value / maxValue) * 180).dp.coerceAtLeast(8.dp)
                                    val barColor = when {
                                        value == dataPoints.maxOfOrNull { it.second } -> Color(0xFFFFB300) // Yellow for highest
                                        value >= maxValue * 0.7 -> Color(0xFFFFC107) // Lighter yellow
                                        else -> Color(0xFF4CAF50) // Green
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(barHeight)
                                            .background(barColor, RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                    
                    // X-axis labels and values on same line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        dataPoints.forEach { (label, value) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (chartType == "revenue") {
                                        "Ksh ${String.format("%.1fK", value)}"
                                    } else {
                                        value.toInt().toString()
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF4CAF50))
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun RecentActivityItem(activity: RecentActivity) {
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(activity.iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = activity.iconRes),
                    contentDescription = activity.title,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = activity.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = activity.timestamp,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AdminBottomNavigationBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color(0xFF2E7D32)
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (selectedItem == "Dashboard") Icons.Filled.List else Icons.Outlined.List,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Home") },
            selected = selectedItem == "Dashboard",
            onClick = { onItemSelected("Dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-4).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedItem == "Users") Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Users",
                        modifier = Modifier.size(16.dp),
                        tint = if (selectedItem == "Users") Color(0xFF4CAF50) else Color.Gray
                    )
                    Icon(
                        imageVector = if (selectedItem == "Users") Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (selectedItem == "Users") Color(0xFF4CAF50) else Color.Gray
                    )
                }
            },
            label = { Text("Users") },
            selected = selectedItem == "Users",
            onClick = { onItemSelected("Users") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (selectedItem == "Orders") Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                    contentDescription = "Orders"
                )
            },
            label = { Text("Orders") },
            selected = selectedItem == "Orders",
            onClick = { onItemSelected("Orders") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (selectedItem == "Analytics") Color(0xFFFFB300) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        color = if (selectedItem == "Analytics") Color.White else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = if (selectedItem == "Analytics") FontWeight.Bold else FontWeight.Normal
                    )
                }
            },
            label = { Text("Analytics") },
            selected = selectedItem == "Analytics",
            onClick = { onItemSelected("Analytics") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFB300),
                selectedTextColor = Color(0xFFFFB300),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (selectedItem == "Profile") Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            selected = selectedItem == "Profile",
            onClick = { onItemSelected("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

