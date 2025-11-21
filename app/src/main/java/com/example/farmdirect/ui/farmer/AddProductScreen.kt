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
fun AddProductRoute(
    viewModel: AddProductViewModel = viewModel(),
    onUpClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AddProductScreen(
        uiState = uiState,
        onAddProduct = viewModel::addProduct,
        onUpClick = onUpClick
    )
}

@Composable
fun AddProductScreen(
    uiState: AddProductUiState,
    onAddProduct: (Product) -> Unit,
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

    // Navigate back when product is added
    LaunchedEffect(uiState.isProductAdded) {
        if (uiState.isProductAdded) {
            onUpClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        FarmerHeader(title = "Add New Product", onUpClick = onUpClick)

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
                    val product = Product(
                        name = productName,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        unit = unit,
                        stock = stock.toDoubleOrNull() ?: 0.0,
                        category = category
                    )
                    onAddProduct(product)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(
                        text = "Save Product",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDropDown(category: String, onCategoryChange: (String) -> Unit, colors: TextFieldColors) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Vegetables", "Fruits", "Grains", "Dairy")

    Box {
        OutlinedTextField(
            value = category,
            onValueChange = { onCategoryChange(it) },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    Modifier.clickable { expanded = !expanded })
            },
            colors = colors
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { category ->
                DropdownMenuItem(onClick = {
                    onCategoryChange(category)
                    expanded = false
                }, text = { Text(text = category) })
            }
        }
    }
}
