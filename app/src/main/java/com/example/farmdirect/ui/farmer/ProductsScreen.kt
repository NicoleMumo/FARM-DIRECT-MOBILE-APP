package com.example.farmdirect.ui.farmer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
fun ProductsRoute(
    viewModel: ProductsViewModel = viewModel(),
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ProductsScreen(
        uiState = uiState,
        onDeleteProduct = viewModel::deleteProduct,
        onRefresh = viewModel::refresh,
        onAddProduct = onAddProduct,
        onEditProduct = onEditProduct
    )
}

@Composable
fun ProductsScreen(
    uiState: ProductsUiState,
    onDeleteProduct: (String) -> Unit,
    onRefresh: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        FarmerHeader(title = "Products")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add New Product Button
            Button(
                onClick = onAddProduct,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = "Add New Product",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // My Products Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Products",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Filter",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Products List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.products) { product ->
                    ProductCard(
                        product = product,
                        onEdit = { onEditProduct(product.id) },
                        onDelete = { onDeleteProduct(product.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: FarmerProduct,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                    .size(64.dp)
                    .background(
                        color = when (product.category) {
                            "Vegetables" -> Color(0xFFE6F8EB)
                            "Fruits" -> Color(0xFFFFE7E7)
                            "Grains" -> Color(0xFFFFF3D8)
                            "Dairy" -> Color(0xFFEAF3FF)
                            else -> Color(0xFFE6F8EB)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = product.iconRes),
                    contentDescription = product.name,
                    modifier = Modifier.size(40.dp)
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
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Ksh ${product.price.toInt()}/${product.unit}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Stock: ${product.stock}${product.unit}",
                    fontSize = 12.sp,
                    color = if (product.status == ProductStatus.OUT_OF_STOCK || product.status == ProductStatus.LOW_STOCK)
                        Color(0xFFE53935) else Color.Gray
                )
            }

            // Status Badge and Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProductStatusBadge(status = product.status)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductStatusBadge(status: ProductStatus) {
    val (text, color) = when (status) {
        ProductStatus.ACTIVE -> "Active" to Color(0xFF4CAF50)
        ProductStatus.LOW_STOCK -> "Low Stock" to Color(0xFFE53935)
        ProductStatus.OUT_OF_STOCK -> "Out of Stock" to Color(0xFFE53935)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
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
