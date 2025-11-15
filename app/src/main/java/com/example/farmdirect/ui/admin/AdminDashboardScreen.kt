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
    productManagementViewModel: ProductManagementViewModel = viewModel()
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
                        onRefresh = dashboardViewModel::refresh
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
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(
    uiState: AdminDashboardUiState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        AdminHeader(
            title = "FarmDirect"
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview Section
            item {
                Text(
                    text = "Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                OverviewMetricsGrid(uiState = uiState)
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
                // Logo - using a colored circle instead of shape drawable
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF8B400), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // You can add an icon or text here if needed
                    // For now, just showing the colored circle
                }
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun OverviewMetricsGrid(uiState: AdminDashboardUiState) {
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
                modifier = Modifier.weight(1f)
            )
            OverviewMetricCard(
                title = "Total Orders",
                value = uiState.totalOrders.toString(),
                growth = "+8%",
                iconRes = R.drawable.ic_seed,
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
            )
            OverviewMetricCard(
                title = "Active Farmers",
                value = uiState.activeFarmers.toString(),
                growth = "+5%",
                iconRes = R.drawable.ic_seed,
                modifier = Modifier.weight(1f)
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                    modifier = Modifier.size(32.dp)
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
            label = { Text("Dashboard") },
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
                if (selectedItem == "Products") {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = Color(0xFF4CAF50),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            },
            label = { Text("Products") },
            selected = selectedItem == "Products",
            onClick = { onItemSelected("Products") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

