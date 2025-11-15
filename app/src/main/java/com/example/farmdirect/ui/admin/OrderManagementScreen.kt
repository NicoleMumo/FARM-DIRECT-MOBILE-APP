package com.example.farmdirect.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
fun OrderManagementRoute(
    viewModel: OrderManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    OrderManagementScreen(
        uiState = uiState,
        filteredOrders = viewModel.getFilteredOrders(),
        onFilterSelected = viewModel::onFilterSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged
    )
}

@Composable
fun OrderManagementScreen(
    uiState: OrderManagementUiState,
    filteredOrders: List<AdminOrder>,
    onFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        AdminHeader(
            title = "Order Management"
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OrderSummaryCard(
                        title = "Total Orders",
                        value = uiState.totalOrders.toString(),
                        iconRes = R.drawable.ic_seed,
                        iconBgColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f)
                    )
                    OrderSummaryCard(
                        title = "Pending",
                        value = uiState.pendingOrders.toString(),
                        iconRes = R.drawable.ic_seed,
                        iconBgColor = Color(0xFFFFF9C4),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Filter Options
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("All Orders", "Today", "Pending", "Completed")) { filter ->
                        FilterChip(
                            selected = uiState.selectedFilter == filter,
                            onClick = { onFilterSelected(filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF5F5F5),
                                labelColor = Color(0xFF2E7D32)
                            )
                        )
                    }
                }
            }
            
            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search orders, farmers, consumers...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
            
            // Order List
            items(filteredOrders) { order ->
                OrderItem(order = order)
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    title: String,
    value: String,
    iconRes: Int,
    iconBgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (title == "Pending") Color(0xFFFF9800) else Color(0xFF2E7D32)
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun OrderItem(order: AdminOrder) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = order.iconRes),
                            contentDescription = "Order",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = order.orderNumber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "${order.farmerName} → ${order.consumerName}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                OrderStatusChip(status = order.status)
            }
            
            Text(
                text = order.items,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ksh ${order.amount.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                TextButton(onClick = { /* View Details */ }) {
                    Text(
                        text = "View Details",
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            Text(
                text = order.dateTime,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun OrderStatusChip(status: AdminOrderStatus) {
    val (text, bgColor, textColor) = when (status) {
        AdminOrderStatus.PENDING -> Triple("Pending", Color(0xFFFFF9C4), Color(0xFFFF9800))
        AdminOrderStatus.IN_TRANSIT -> Triple("In Transit", Color(0xFFE3F2FD), Color(0xFF2196F3))
        AdminOrderStatus.DELIVERED -> Triple("Delivered", Color(0xFFE8F5E9), Color(0xFF4CAF50))
        AdminOrderStatus.COMPLETED -> Triple("Completed", Color(0xFFE8F5E9), Color(0xFF4CAF50))
        AdminOrderStatus.CANCELLED -> Triple("Cancelled", Color(0xFFFFEBEE), Color(0xFFE53935))
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

