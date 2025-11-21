package com.example.farmdirect.ui.consumer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmdirect.R

@Composable
fun ProductDetailsScreen(
    product: ProductUi,
    onBack: () -> Unit,
    onAddToCart: (String, Int) -> Unit,
    onAddToWishlist: (String) -> Unit,
    onRemoveFromWishlist: (String) -> Unit = {},
    isInWishlist: Boolean = false
) {
    var quantity by remember(product.id) {
        mutableIntStateOf(if (product.stock > 0) 1 else 0)
    }
    var isWishlisted by remember { mutableStateOf(isInWishlist) }
    val isOutOfStock = product.stock <= 0
    val unitLabel = product.unit.ifBlank { "unit" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
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
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Product Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { 
                    val wasWishlisted = isWishlisted
                    isWishlisted = !isWishlisted
                    if (!wasWishlisted) {
                        onAddToWishlist(product.id)
                    } else {
                        onRemoveFromWishlist(product.id)
                    }
                }) {
                    // Custom heart icon
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isWishlisted) "♥" else "♡",
                            fontSize = 20.sp,
                            color = if (isWishlisted) Color(0xFFE53935) else Color.White
                        )
                    }
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Product Image - Category icon on white circle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFFF7F9FA)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = when (product.category) {
                                "Vegetables" -> R.drawable.vegetable_icon
                                "Fruits" -> R.drawable.fruit_icon
                                "Grains" -> R.drawable.grain_icon
                                "Dairy" -> R.drawable.dairy_icon
                                else -> R.drawable.vegetable_icon
                            }
                        ),
                        contentDescription = product.name,
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
            
            // Product Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "KSh ${product.price.toInt()}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "per $unitLabel",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF9C4)
                    ) {
                        Text(
                            text = "Fresh",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                
                // Description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FA))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = product.description.ifBlank {
                                "Fresh, organic ${product.name.lowercase()} sourced directly from ${product.farmName}."
                            },
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
                
                // Key facts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductFactChip(
                        label = "In stock",
                        value = "${product.stock} $unitLabel",
                        modifier = Modifier.weight(1f)
                    )
                    ProductFactChip(
                        label = "Category",
                        value = product.category,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Seller Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FA))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Gray.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Seller",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.farmName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Category",
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Gray
                                    )
                                    Text(
                                        text = product.category,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    repeat(5) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFFFFC107)
                                        )
                                    }
                                    Text(
                                        text = "4.8 (127 reviews)",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = { /* Chat with seller */ }) {
                                    // Chat icon - using text bubble as fallback
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "💬",
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = "Verified",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Quantity Selector
                Text(
                    text = "Quantity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Text(
                            text = "−",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = quantity.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = unitLabel,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(
                        onClick = { if (quantity < product.stock) quantity++ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (quantity >= product.stock) Color.Gray.copy(alpha = 0.3f) else Color(
                                    0xFF4CAF50
                                ),
                                CircleShape
                            ),
                        enabled = quantity < product.stock
                    ) {
                        Text(
                            text = "+",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (quantity >= product.stock) Color.Gray else Color.White
                        )
                    }
                }
                Text(
                    text = if (isOutOfStock) "Currently unavailable" else "Only ${product.stock} $unitLabel left",
                    fontSize = 13.sp,
                    color = if (isOutOfStock) Color(0xFFE53935) else Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Bottom Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onAddToCart(product.id, quantity) },
                        enabled = !isOutOfStock && quantity > 0,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOutOfStock) Color.Gray else Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = "Add to Cart",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            val wasWishlisted = isWishlisted
                            isWishlisted = !isWishlisted
                            if (!wasWishlisted) {
                                onAddToWishlist(product.id)
                            } else {
                                onRemoveFromWishlist(product.id)
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    ) {
                        // Custom heart icon
                        Text(
                            text = if (isWishlisted) "♥" else "♡",
                            fontSize = 24.sp,
                            color = if (isWishlisted) Color(0xFFE53935) else Color.Gray
                        )
                    }
                }
                Text(
                    text = "Total: KSh ${(product.price * quantity).toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ProductFactChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

