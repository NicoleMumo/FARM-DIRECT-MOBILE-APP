package com.example.farmdirect.ui.consumer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun OrdersRoute(
    viewModel: OrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    OrdersScreen(
        uiState = uiState,
        onFilterSelected = viewModel::selectFilter,
        onViewDetails = { /* TODO: Navigate to order details */ },
        onTrackOrder = { /* TODO: Navigate to tracking */ },
        onReorder = viewModel::reorder
    )
}

@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    onFilterSelected: (String?) -> Unit,
    onViewDetails: (String) -> Unit,
    onTrackOrder: (String) -> Unit,
    onReorder: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Orders",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* TODO: Show filter dialog */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Filter",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Filter",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // Filter Tabs
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTab(
                    label = "All",
                    isSelected = uiState.selectedFilter == null,
                    onClick = { onFilterSelected(null) }
                )
                FilterTab(
                    label = "Pending",
                    isSelected = uiState.selectedFilter == "Pending",
                    onClick = { onFilterSelected("Pending") }
                )
                FilterTab(
                    label = "In Transit",
                    isSelected = uiState.selectedFilter == "In Transit",
                    onClick = { onFilterSelected("In Transit") }
                )
                FilterTab(
                    label = "Delivered",
                    isSelected = uiState.selectedFilter == "Delivered",
                    onClick = { onFilterSelected("Delivered") }
                )
            }
        }
        
        // Orders List
        if (uiState.filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No orders found",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredOrders) { order ->
                    OrderCard(
                        order = order,
                        onViewDetails = { onViewDetails(order.id) },
                        onTrackOrder = { onTrackOrder(order.id) },
                        onReorder = { onReorder(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF4CAF50) else Color.White,
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
        } else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    onViewDetails: () -> Unit,
    onTrackOrder: () -> Unit,
    onReorder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = when (order.status) {
                            OrderStatus.DELIVERED -> Color(0xFFE6F8EB)
                            OrderStatus.IN_TRANSIT -> Color(0xFFFFF3D8)
                            OrderStatus.PENDING -> Color(0xFFFFE7E7)
                            OrderStatus.CANCELLED -> Color(0xFFE0E0E0)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = when {
                            order.productName.contains("Corn", ignoreCase = true) ||
                            order.productName.contains("Rice", ignoreCase = true) ||
                            order.productName.contains("Grain", ignoreCase = true) -> R.drawable.grain_icon
                            order.productName.contains("Apple", ignoreCase = true) ||
                            order.productName.contains("Orange", ignoreCase = true) -> R.drawable.fruit_icon
                            order.productName.contains("Milk", ignoreCase = true) ||
                            order.productName.contains("Dairy", ignoreCase = true) -> R.drawable.dairy_icon
                            else -> R.drawable.vegetable_icon
                        }
                    ),
                    contentDescription = order.productName,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Order Details
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
                    text = order.supplier,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = order.orderNumber,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = order.orderDate,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ksh ${order.price.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    StatusBadge(status = order.status)
                }
            }
        }
        
        // Action Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (order.status) {
                OrderStatus.CANCELLED -> {
                    OutlinedButton(
                        onClick = onReorder,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Reorder", fontSize = 14.sp)
                    }
                }
                OrderStatus.IN_TRANSIT -> {
                    Button(
                        onClick = onTrackOrder,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Track Order", fontSize = 14.sp, color = Color.White)
                    }
                }
                else -> {
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View Details", fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: OrderStatus) {
    val (text, textColor, bgColor) = when (status) {
        OrderStatus.DELIVERED -> Triple("Delivered", Color(0xFF4CAF50), Color(0xFFE6F8EB))
        OrderStatus.IN_TRANSIT -> Triple("In Transit", Color(0xFFFF9800), Color(0xFFFFF3D8))
        OrderStatus.PENDING -> Triple("Pending", Color(0xFFFF9800), Color(0xFFFFE7E7))
        OrderStatus.CANCELLED -> Triple("Cancelled", Color.Gray, Color(0xFFE0E0E0))
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

