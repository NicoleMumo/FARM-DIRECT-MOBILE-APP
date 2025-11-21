package com.example.farmdirect.ui.farmer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirect.R

@Composable
fun FarmerDashboardRoute(
    dashboardViewModel: FarmerDashboardViewModel = viewModel(),
    productsViewModel: ProductsViewModel = viewModel(),
    ordersViewModel: OrdersViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel(),
    profileViewModel: FarmerProfileViewModel = viewModel()
) {
    var selectedBottomNavItem by remember { mutableStateOf("Dashboard") }
    
    Scaffold(
        bottomBar = {
            FarmerBottomNavigationBar(
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
                    FarmerDashboardScreen(
                        uiState = uiState,
                        onRefresh = dashboardViewModel::refresh,
                        onViewAllOrders = { selectedBottomNavItem = "Orders" },
                        onAddProduct = { selectedBottomNavItem = "Products" },
                        onViewAnalytics = { selectedBottomNavItem = "Analytics" }
                    )
                }
                "Products" -> {
                    ProductsRoute(viewModel = productsViewModel)
                }
                "Orders" -> {
                    OrdersRoute(viewModel = ordersViewModel)
                }
                "Analytics" -> {
                    AnalyticsRoute(viewModel = analyticsViewModel)
                }
                "Profile" -> {
                    ProfileRoute(viewModel = profileViewModel)
                }
            }
        }
    }
}

@Composable
fun FarmerDashboardScreen(
    uiState: FarmerDashboardUiState,
    onRefresh: () -> Unit,
    onViewAllOrders: () -> Unit,
    onAddProduct: () -> Unit,
    onViewAnalytics: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        FarmerHeader(title = "FarmDirect")
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KPI Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Sales Card
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Sales",
                        value = uiState.totalSales,
                        growth = uiState.salesGrowth,
                        iconRes = R.drawable.ic_seed,
                        iconColor = Color(0xFFFFC107)
                    )
                    
                    // Pending Orders Card
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending Orders",
                        value = uiState.pendingOrders.toString(),
                        newCount = "${uiState.newPendingOrders} new",
                        iconRes = R.drawable.ic_seed,
                        iconColor = Color(0xFF4CAF50),
                        newCountColor = Color(0xFFE53935)
                    )
                }
            }
            
            // Restocking Alert
            item {
                uiState.restockingAlert?.let { alert ->
                    RestockingAlertCard(alert = alert)
                }
            }
            
            // Recent Orders Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Orders",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.clickable { onViewAllOrders() }
                    )
                }
            }
            
            // Recent Orders List
            items(uiState.recentOrders) { order ->
                RecentOrderCard(order = order)
            }
            
            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        title = "Add Product",
                        icon = Icons.Default.Add,
                        backgroundColor = Color(0xFF4CAF50),
                        onClick = onAddProduct
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        title = "View Analytics",
                        // Using Settings icon here to avoid unsupported chart icons
                        icon = Icons.Default.Settings,
                        backgroundColor = Color(0xFFFFC107),
                        onClick = onViewAnalytics
                    )
                }
            }
        }
    }
}

@Composable
fun FarmerHeader(title: String) {
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFFFC107), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_seed),
                        contentDescription = "Logo",
                        modifier = Modifier.size(20.dp)
                    )
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
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    growth: String? = null,
    newCount: String? = null,
    iconRes: Int,
    iconColor: Color,
    newCountColor: Color = Color(0xFF4CAF50)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(if (title == "Total Sales") 3 else 1) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = iconColor
                        )
                    }
                }
                if (growth != null) {
                    Text(
                        text = growth,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
                if (newCount != null) {
                    Text(
                        text = newCount,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = newCountColor
                    )
                }
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (title == "Total Sales") Color(0xFF2E7D32) else Color.Black
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RestockingAlertCard(alert: RestockingAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFFFF9800)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Low Stock",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF9800)
                )
                Text(
                    text = "${alert.count} Products Need Restocking",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = alert.products.joinToString(", "),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun RecentOrderCard(order: FarmerOrder) {
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
                    .size(48.dp)
                    .background(
                        color = Color(0xFFE6F8EB),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = order.iconRes),
                    contentDescription = order.productName,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = order.productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${order.quantity} - KSh ${order.price.toInt()}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Order #${order.orderNumber} • Customer: ${order.customerName}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            StatusBadge(status = order.status)
        }
    }
}

@Composable
fun StatusBadge(status: FarmerOrderStatus) {
    val (text, color) = when (status) {
        FarmerOrderStatus.PENDING -> "Pending" to Color(0xFFFFC107)
        FarmerOrderStatus.PREPARED -> "Prepared" to Color(0xFF4CAF50)
        FarmerOrderStatus.DELIVERED -> "Delivered" to Color(0xFF4CAF50)
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FarmerBottomNavigationBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF2E7D32),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (selectedItem == "Dashboard") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard") },
            selected = selectedItem == "Dashboard",
            onClick = { onItemSelected("Dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White
            )
        )
        NavigationBarItem(
            icon = {
                // Use ShoppingCart icon for Products to avoid unsupported Inventory icon
                Icon(
                    imageVector = if (selectedItem == "Products") Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                    contentDescription = "Products"
                )
            },
            label = { Text("Products") },
            selected = selectedItem == "Products",
            onClick = { onItemSelected("Products") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White
            )
        )
        NavigationBarItem(
            icon = {
                // Custom text-based icon for Orders (\"O\") to avoid unsupported icons
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "O",
                        color = if (selectedItem == "Orders") Color(0xFFFFC107) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (selectedItem == "Orders") FontWeight.Bold else FontWeight.Normal
                    )
                }
            },
            label = { Text("Orders") },
            selected = selectedItem == "Orders",
            onClick = { onItemSelected("Orders") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White
            )
        )
        NavigationBarItem(
            icon = {
                // Text-based icon for Analytics (\"A\") to avoid unsupported chart icons
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        color = if (selectedItem == "Analytics") Color(0xFFFFC107) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (selectedItem == "Analytics") FontWeight.Bold else FontWeight.Normal
                    )
                }
            },
            label = { Text("Analytics") },
            selected = selectedItem == "Analytics",
            onClick = { onItemSelected("Analytics") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (selectedItem == "Profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            selected = selectedItem == "Profile",
            onClick = { onItemSelected("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White
            )
        )
    }
}

