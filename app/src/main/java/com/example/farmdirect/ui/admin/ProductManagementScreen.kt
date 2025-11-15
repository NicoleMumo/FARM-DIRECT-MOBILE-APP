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
fun ProductManagementRoute(
    viewModel: ProductManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    ProductManagementScreen(
        uiState = uiState,
        filteredProducts = viewModel.getFilteredProducts(),
        onCategorySelected = viewModel::onCategorySelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onApprove = viewModel::approveProduct,
        onFlag = viewModel::flagProduct,
        onDelete = viewModel::deleteProduct
    )
}

@Composable
fun ProductManagementScreen(
    uiState: ProductManagementUiState,
    filteredProducts: List<AdminProduct>,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onApprove: (String) -> Unit,
    onFlag: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        AdminHeader(
            title = "Product Management"
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search products...") },
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
            
            // Category Filters
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("All", "Vegetables", "Fruits", "Dairy", "Grains")) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category) },
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
            
            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductStatCard(
                        value = uiState.totalProducts.toString(),
                        label = "Total Products",
                        bgColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f)
                    )
                    ProductStatCard(
                        value = uiState.pendingProducts.toString(),
                        label = "Pending",
                        bgColor = Color(0xFFFFF9C4),
                        modifier = Modifier.weight(1f)
                    )
                    ProductStatCard(
                        value = uiState.flaggedProducts.toString(),
                        label = "Flagged",
                        bgColor = Color(0xFFFFEBEE),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Product List
            items(filteredProducts) { product ->
                ProductItem(
                    product = product,
                    onApprove = { onApprove(product.id) },
                    onFlag = { onFlag(product.id) },
                    onDelete = { onDelete(product.id) }
                )
            }
        }
    }
}

@Composable
fun ProductStatCard(
    value: String,
    label: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
fun ProductItem(
    product: AdminProduct,
    onApprove: () -> Unit,
    onFlag: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // Status Badge
            ProductStatusBadge(
                status = product.status,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                when (product.category.lowercase()) {
                                    "vegetables" -> Color(0xFFE8F5E9)
                                    "fruits" -> Color(0xFFFFE7E7)
                                    "dairy" -> Color(0xFFE3F2FD)
                                    "grains" -> Color(0xFFFFF9C4)
                                    else -> Color(0xFFE8F5E9)
                                },
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = product.iconRes),
                            contentDescription = product.name,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = product.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "by ${product.seller}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = product.price,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (product.status == ProductStatus.PENDING) {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Approve",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { /* Already approved */ },
                            modifier = Modifier.weight(1f),
                            enabled = false,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Approved",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approved")
                        }
                    }
                    
                    if (product.status == ProductStatus.FLAGGED) {
                        OutlinedButton(
                            onClick = { /* Already flagged */ },
                            modifier = Modifier.weight(1f),
                            enabled = false,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE53935)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Flagged",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Flagged")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onFlag,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Flag",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Flag")
                        }
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductStatusBadge(
    status: ProductStatus,
    modifier: Modifier = Modifier
) {
    val statusInfo = when (status) {
        ProductStatus.PENDING -> Triple("Pending", Color(0xFFFFF9C4), Color(0xFFFF9800))
        ProductStatus.APPROVED -> Triple("Approved", Color(0xFFE8F5E9), Color(0xFF4CAF50))
        ProductStatus.FLAGGED -> Triple("Flagged", Color(0xFFFFEBEE), Color(0xFFE53935))
    }
    
    val (text, bgColor, textColor) = statusInfo
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

