package com.example.farmdirect.ui.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirect.model.Product

@Composable
fun EditProductRoute(
    viewModel: EditProductViewModel = viewModel(),
    onUpClick: () -> Unit,
    productId: String
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        // Load product only if it's not already loaded or a different one is needed
        if (uiState.product?.id != productId) {
            viewModel.loadProduct(productId)
        }
    }

    EditProductScreen(
        uiState = uiState,
        onUpdateProduct = viewModel::updateProduct,
        onUpClick = onUpClick
    )
}

@Composable
fun EditProductScreen(
    uiState: EditProductUiState,
    onUpdateProduct: (Product) -> Unit,
    onUpClick: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        unfocusedLabelColor = Color.DarkGray,
        focusedLabelColor = Color(0xFF2E7D32),
        cursorColor = Color(0xFF2E7D32),
    )

    LaunchedEffect(uiState.product) {
        uiState.product?.let {
            productName = it.name
            description = it.description
            price = it.price.toString()
            unit = it.unit
            stock = it.stock.toString()
            category = it.category
        }
    }

    LaunchedEffect(uiState.isProductUpdated) {
        if (uiState.isProductUpdated) {
            onUpClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        FarmerHeader(title = "Edit Product", onUpClick = onUpClick)

        when {
            // Show a loading spinner only when initially loading the product
            uiState.isLoading && uiState.product == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Once the product is loaded, show the editor
            uiState.product != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = textFieldColors
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price") },
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (e.g., kg, L)") },
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors
                        )
                    }

                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("In Stock") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    CategoryDropDown(category = category, onCategoryChange = { category = it }, colors = textFieldColors)

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            // Safely access the product to prevent crashes
                            uiState.product?.let { currentProduct ->
                                val updatedProduct = currentProduct.copy(
                                    name = productName,
                                    description = description,
                                    price = price.toDoubleOrNull() ?: currentProduct.price,
                                    unit = unit.ifBlank { currentProduct.unit },
                                    stock = stock.toIntOrNull() ?: currentProduct.stock,
                                    category = category
                                )
                                onUpdateProduct(updatedProduct)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        enabled = !uiState.isLoading // Disable button while saving
                    ) {
                        Text(
                            text = "Save Changes",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            // If loading is finished but the product is still null, show an error
            !uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Product not found or failed to load.")
                }
            }
        }
    }
}
