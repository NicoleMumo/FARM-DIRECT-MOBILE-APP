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
fun ConsumerHomeRoute(
    homeViewModel: ConsumerHomeViewModel = viewModel(),
    wishlistViewModel: WishlistViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    ordersViewModel: OrdersViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    var selectedBottomNavItem by remember { mutableStateOf("Home") }
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedBottomNavItem,
                onItemSelected = { selectedBottomNavItem = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedBottomNavItem) {
                "Home" -> {
                    val uiState by homeViewModel.uiState.collectAsState()
                    ConsumerHomeScreen(
                        uiState = uiState,
                        categories = getDefaultCategories(),
                        onSearchChanged = homeViewModel::onSearchChanged,
                        onCategoryClicked = homeViewModel::onCategorySelected,
                        onAddToCart = { productId ->
                            // Add to cart logic
                            cartViewModel.addToCart(productId)
                        }
                    )
                }
                "Wishlist" -> {
                    WishlistRoute(viewModel = wishlistViewModel)
                }
                "Cart" -> {
                    CartRoute(viewModel = cartViewModel)
                }
                "Orders" -> {
                    OrdersRoute(viewModel = ordersViewModel)
                }
                "Profile" -> {
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
    onAddToCart: (String) -> Unit
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
                        onAddToCart = onAddToCart
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
            
            // Icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = { /* TODO: Notifications */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF2E7D32)
                    )
                }
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
    onAddToCart: (String) -> Unit
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
                    onAddToCart = { onAddToCart(product.id) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductUi,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Product icon/placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = when (product.category) {
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
                // You can add product image here when available
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
                    modifier = Modifier.size(48.dp)
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(12.dp),
                    tint = Color.Gray
                )
                Text(
                    text = product.farmName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ksh${product.price.toInt()}/kg",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
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
                Icon(
                    imageVector = if (selectedItem == "Wishlist") Icons.Filled.Favorite else Icons.Outlined.Favorite,
                    contentDescription = "Wishlist"
                )
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
                Icon(
                    imageVector = if (selectedItem == "Cart") Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                    contentDescription = "Cart"
                )
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

