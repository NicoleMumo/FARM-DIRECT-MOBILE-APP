package com.example.farmdirect.ui.consumer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
fun ConsumerHomeRoute(
    homeViewModel: ConsumerHomeViewModel = viewModel(),
    wishlistViewModel: WishlistViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    ordersViewModel: OrdersViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    var selectedBottomNavItem by remember { mutableStateOf("Home") }
    var selectedProduct by remember { mutableStateOf<ProductUi?>(null) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    val cartUiState by cartViewModel.uiState.collectAsState()
    val cartItemCount = cartUiState.items.sumOf { it.quantity }
    
    Scaffold(
        bottomBar = {
            if (selectedProduct == null && selectedOrder == null) {
                BottomNavigationBar(
                    selectedItem = selectedBottomNavItem,
                    onItemSelected = { selectedBottomNavItem = it },
                    cartItemCount = cartItemCount
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                selectedProduct != null -> {
                    val wishlistState by wishlistViewModel.uiState.collectAsState()
                    val isInWishlist = wishlistState.items.any { it.productId == selectedProduct!!.id }
                    ProductDetailsScreen(
                        product = selectedProduct!!,
                        onBack = { selectedProduct = null },
                        onAddToCart = { productId, quantity ->
                            cartViewModel.addToCart(productId, quantity)
                        },
                        onAddToWishlist = { productId ->
                            wishlistViewModel.addToWishlist(productId)
                        },
                        onRemoveFromWishlist = { productId ->
                            val wishlistItem = wishlistState.items.find { it.productId == productId }
                            wishlistItem?.let { wishlistViewModel.removeFromWishlist(it.id) }
                        },
                        isInWishlist = isInWishlist
                    )
                }
                selectedOrder != null -> {
                    OrderDetailsScreen(
                        order = selectedOrder!!,
                        onBack = { selectedOrder = null },
                        onTrackPackage = { /* TODO: Navigate to tracking */ },
                        onReorder = {
                            // Add order items to cart
                            selectedOrder = null
                        }
                    )
                }
                selectedBottomNavItem == "Home" -> {
                    val uiState by homeViewModel.uiState.collectAsState()
    ConsumerHomeScreen(
        uiState = uiState,
        categories = getDefaultCategories(),
                        onSearchChanged = homeViewModel::onSearchChanged,
                        onCategoryClicked = homeViewModel::onCategorySelected,
                        onAddToCart = { productId ->
                            cartViewModel.addToCart(productId)
                        },
                        onProductClick = { product ->
                            selectedProduct = product
                        }
                    )
                }
                selectedBottomNavItem == "Wishlist" -> {
                    WishlistRoute(
                        viewModel = wishlistViewModel,
                        onProductClick = { product ->
                            selectedProduct = product
                        }
                    )
                }
                selectedBottomNavItem == "Cart" -> {
                    CartRoute(viewModel = cartViewModel)
                }
                selectedBottomNavItem == "Orders" -> {
                    OrdersRoute(
                        viewModel = ordersViewModel,
                        onViewDetails = { orderId ->
                            val order = ordersViewModel.uiState.value.filteredOrders.find { it.id == orderId }
                            if (order != null) {
                                selectedOrder = order
                            }
                        }
                    )
                }
                selectedBottomNavItem == "Profile" -> {
                    ProfileRoute(viewModel = profileViewModel)
                }
            }
        }
    }
}

@Composable
fun ConsumerHomeScreen(
    uiState: ConsumerHomeUiState,
    categories: List<CategoryUi>,
    onSearchChanged: (String) -> Unit,
    onCategoryClicked: (String?) -> Unit,
    onAddToCart: (String) -> Unit,
    onProductClick: (ProductUi) -> Unit
) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FA))
        ) {
            // Top App Bar
            TopAppBar(
                categories = categories,
                onCategoryClicked = onCategoryClicked
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search Bar
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchChanged
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Shop by Category Section
                Text(
                    text = "Shop by Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(12.dp))
                CategoryGrid(
                    categories = categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategoryClicked = onCategoryClicked
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Fresh Products Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fresh Products",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.clickable { /* TODO: Navigate to all products */ }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ProductList(
                        products = uiState.filteredProducts,
                        onAddToCart = onAddToCart,
                        onProductClick = onProductClick
                    )
            }
        }
    }
}

@Composable
fun TopAppBar(
    categories: List<CategoryUi>,
    onCategoryClicked: (String?) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_seed),
                    contentDescription = "FarmDirect Logo",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "FarmDirect",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
            
            // Profile Icon
                IconButton(onClick = { /* TODO: Profile */ }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF2E7D32)
                    )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
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
}

@Composable
fun CategoryGrid(
    categories: List<CategoryUi>,
    selectedCategory: String?,
    onCategoryClicked: (String?) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryCard(
                category = category,
                isSelected = selectedCategory == category.name,
                onClick = { 
                    if (selectedCategory == category.name) {
                        onCategoryClicked(null)
                    } else {
                        onCategoryClicked(category.name)
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryUi,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                category.bgColor.copy(alpha = 0.8f)
            } else {
                category.bgColor
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Image(
                painter = painterResource(id = category.iconRes),
                contentDescription = category.name,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ProductList(
    products: List<ProductUi>,
    onAddToCart: (String) -> Unit,
    onProductClick: (ProductUi) -> Unit
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("No products found", color = Color.Gray)
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onAddToCart = { onAddToCart(product.id) },
                    onClick = { onProductClick(product) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductUi,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    val isOutOfStock = product.stock <= 0
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        color = when (product.category) {
                            "Vegetables" -> Color(0xFFE6F8EB)
                            "Fruits" -> Color(0xFFFFEAEA)
                            "Grains" -> Color(0xFFFFF4E0)
                            "Dairy" -> Color(0xFFEAF3FF)
                            else -> Color(0xFFECEFF1)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.take(1).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
            
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Farmer location",
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = product.farmName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "Ksh ${product.price.toInt()}/${product.unit}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOutOfStock) "Out of stock" else "Stock: ${product.stock}",
                        fontSize = 12.sp,
                        color = if (isOutOfStock) Color(0xFFE53935) else Color.Gray,
                        fontWeight = if (isOutOfStock) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (!isOutOfStock) {
                        Text(
                            text = "Unit: ${product.unit}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                Button(
                    onClick = onAddToCart,
                    enabled = !isOutOfStock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOutOfStock) Color.LightGray else Color(0xFFFFC107),
                        contentColor = if (isOutOfStock) Color.DarkGray else Color.White,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add to cart",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White.takeIf { !isOutOfStock } ?: Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isOutOfStock) "Unavailable" else "Add")
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    cartItemCount: Int
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color(0xFF2E7D32)
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (selectedItem == "Home") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") },
            selected = selectedItem == "Home",
            onClick = { onItemSelected("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = {
                // Custom heart icon for wishlist
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedItem == "Wishlist") "♥" else "♡",
                        fontSize = 18.sp,
                        color = if (selectedItem == "Wishlist") Color(0xFF4CAF50) else Color.Gray
                    )
                }
            },
            label = { Text("Wishlist") },
            selected = selectedItem == "Wishlist",
            onClick = { onItemSelected("Wishlist") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = Color(0xFFFFC107)
                            ) {
                                Text(cartItemCount.toString())
                            }
                        }
                    }
                ) {
                Icon(
                    imageVector = if (selectedItem == "Cart") Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                    contentDescription = "Cart"
                )
                }
            },
            label = { Text("Cart") },
            selected = selectedItem == "Cart",
            onClick = { onItemSelected("Cart") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = {
                if (selectedItem == "Orders") {
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
                            text = "O",
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
                            text = "O",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
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
                Icon(
                    imageVector = if (selectedItem == "Profile") Icons.Filled.Person else Icons.Outlined.Person,
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

fun getDefaultCategories(): List<CategoryUi> {
    return listOf(
        CategoryUi(
            name = "Vegetables",
            iconRes = R.drawable.vegetable_icon,
            bgColor = Color(0xFFE6F8EB)
        ),
        CategoryUi(
            name = "Fruits",
            iconRes = R.drawable.fruit_icon,
            bgColor = Color(0xFFFFE7E7)
        ),
        CategoryUi(
            name = "Grains",
            iconRes = R.drawable.grain_icon,
            bgColor = Color(0xFFFFF3D8)
        ),
        CategoryUi(
            name = "Dairy",
            iconRes = R.drawable.dairy_icon,
            bgColor = Color(0xFFEAF3FF)
        )
    )
}

