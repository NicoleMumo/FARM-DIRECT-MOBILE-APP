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
fun WishlistRoute(
    viewModel: WishlistViewModel = viewModel(),
    onProductClick: (ProductUi) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    WishlistScreen(
        uiState = uiState,
        onRemoveItem = viewModel::removeFromWishlist,
        onAddToCart = viewModel::moveItemToCart,
        onSearchChanged = viewModel::onSearchChanged,
        onProductClick = onProductClick
    )
}

@Composable
fun WishlistScreen(
    uiState: WishlistUiState,
    onRemoveItem: (String) -> Unit,
    onAddToCart: (WishlistItem) -> Unit,
    onSearchChanged: (String) -> Unit,
    onProductClick: (ProductUi) -> Unit
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Wishlist",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search fresh products...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wishlist Items
            if (uiState.filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your wishlist is empty",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredItems) { item ->
                        WishlistItemCard(
                            item = item,
                            onRemove = { onRemoveItem(item.id) },
                            onAddToCart = { onAddToCart(item) },
                            onClick = {
                                onProductClick(
                                    ProductUi(
                                        id = item.productId,
                                        name = item.name,
                                        price = item.price,
                                        farmName = item.farmName,
                                        category = item.category,
                                        imageUrl = item.imageUrl,
                                        unit = item.unit,
                                        stock = item.stock
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistItemCard(
    item: WishlistItem,
    onRemove: () -> Unit,
    onAddToCart: () -> Unit,
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = when (item.category) {
                            "Vegetables" -> Color(0xFFE6F8EB)
                            "Fruits" -> Color(0xFFFFE7E7)
                            "Grains" -> Color(0xFFFFF3D8)
                            "Dairy" -> Color(0xFFEAF3FF)
                            else -> Color(0xFFE6F8EB)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = when (item.category) {
                            "Vegetables" -> R.drawable.vegetable_icon
                            "Fruits" -> R.drawable.fruit_icon
                            "Grains" -> R.drawable.grain_icon
                            "Dairy" -> R.drawable.dairy_icon
                            else -> R.drawable.vegetable_icon
                        }
                    ),
                    contentDescription = item.name,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            // Product Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Farmer",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = item.farmName,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                
                Text(
                    text = "KSh ${item.price.toInt()}/${item.unit}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = if (item.stock > 0) "Stock: ${item.stock}" else "Out of stock",
                    fontSize = 12.sp,
                    color = if (item.stock > 0) Color.Gray else Color(0xFFE53935),
                    fontWeight = if (item.stock > 0) FontWeight.Normal else FontWeight.SemiBold
                )
            }
            
            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                TextButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Remove", fontSize = 12.sp)
                }
                
                Button(
                    onClick = onAddToCart,
                    enabled = item.stock > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.stock > 0) Color(0xFF4CAF50) else Color.LightGray,
                        contentColor = if (item.stock > 0) Color.White else Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Add to Cart",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (item.stock > 0) "Add to Cart" else "Unavailable",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

