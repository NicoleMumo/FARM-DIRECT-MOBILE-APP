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
import androidx.compose.runtime.remember
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
fun OrdersRoute(
    viewModel: OrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredOrders = remember(uiState.selectedFilter, uiState.orders) {
        viewModel.getFilteredOrders()
    }
    
    OrdersScreen(
        uiState = uiState,
        filteredOrders = filteredOrders,
        onFilterSelected = viewModel::selectFilter,
        onRefresh = viewModel::refresh,
        onViewDetails = { /* TODO: Navigate to order details */ }
    )
}

@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    filteredOrders: List<FarmerOrder>,
    onFilterSelected: (FarmerOrderStatus?) -> Unit,
    onRefresh: () -> Unit,
    onViewDetails: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        FarmerHeader(title = "Orders")
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OrderSummaryCard(
                    modifier = Modifier.weight(1f),
                    count = uiState.pendingCount,
                    label = "Pending",
                    // Use Info icon instead of unsupported AccessTime
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFFFFC107)
                )
                OrderSummaryCard(
                    modifier = Modifier.weight(1f),
                    count = uiState.preparedCount,
                    label = "Prepared",
                    icon = Icons.Default.Check,
                    iconColor = Color(0xFF4CAF50)
                )
                OrderSummaryCard(
                    modifier = Modifier.weight(1f),
                    count = uiState.deliveredCount,
                    label = "Delivered",
                    // Use ShoppingCart icon instead of unsupported LocalShipping
                    icon = Icons.Default.ShoppingCart,
                    iconColor = Color(0xFF4CAF50)
                )
            }
            
            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedFilter == null,
                    onClick = { onFilterSelected(null) },
                    label = { Text("All") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.selectedFilter == FarmerOrderStatus.PENDING,
                    onClick = { onFilterSelected(FarmerOrderStatus.PENDING) },
                    label = { Text("Pending") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.selectedFilter == FarmerOrderStatus.PREPARED,
                    onClick = { onFilterSelected(FarmerOrderStatus.PREPARED) },
                    label = { Text("Prepared") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.selectedFilter == FarmerOrderStatus.DELIVERED,
                    onClick = { onFilterSelected(FarmerOrderStatus.DELIVERED) },
                    label = { Text("Delivered") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Orders List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders) { order ->
                    OrderItemCard(
                        order = order,
                        onViewDetails = { onViewDetails(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = iconColor
            )
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun OrderItemCard(
    order: FarmerOrder,
    onViewDetails: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE6F8EB), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = order.iconRes),
                        contentDescription = order.productName,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Product Info
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
                        text = "${order.quantity} • Ksh${order.price.toInt()}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // Status Badge
                OrderStatusBadge(status = order.status)
            }
            
            // Customer Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFFFB3BA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.customerName.first().toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = order.customerName,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    if (order.rating != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFC107)
                            )
                            Text(
                                text = "${order.rating} rating",
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
                        text = order.timeAgo,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    TextButton(
                        onClick = onViewDetails,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: FarmerOrderStatus) {
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

