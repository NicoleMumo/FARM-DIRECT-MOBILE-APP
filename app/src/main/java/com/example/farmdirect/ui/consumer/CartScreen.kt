package com.example.farmdirect.ui.consumer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirect.R
import com.example.farmdirect.ui.consumer.CheckoutStatus
import com.example.farmdirect.utils.DistanceCalculator

@Composable
fun CartRoute(
    viewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    CartScreen(
        uiState = uiState,
        onQuantityChange = viewModel::updateQuantity,
        onRemoveItem = viewModel::removeItem,
        onPaymentMethodSelected = viewModel::selectPaymentMethod,
        onChangeAddress = viewModel::showLocationDialog,
        onCheckout = viewModel::checkout,
        onDismissPaymentPrompt = viewModel::dismissPaymentPrompt,
        onUpdateDeliveryAddress = viewModel::updateDeliveryAddress,
        onDismissLocationDialog = viewModel::dismissLocationDialog,
        onProcessCardPayment = viewModel::processCardPayment,
        onDismissCardPaymentDialog = viewModel::dismissCardPaymentDialog
    )
}

@Composable
fun CartScreen(
    uiState: CartUiState,
    onQuantityChange: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onPaymentMethodSelected: (String) -> Unit,
    onChangeAddress: () -> Unit,
    onCheckout: () -> Unit,
    onDismissPaymentPrompt: () -> Unit,
    onUpdateDeliveryAddress: (DeliveryAddress) -> Unit,
    onDismissLocationDialog: () -> Unit,
    onProcessCardPayment: (String, String, String, String) -> Unit,
    onDismissCardPaymentDialog: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2E7D32)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO: Navigate back */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Cart & Checkout",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Your Cart Section
            item {
                Text(
                    text = "Your Cart",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            items(uiState.items) { item ->
                CartItemCard(
                    item = item,
                    onQuantityChange = { quantity -> onQuantityChange(item.id, quantity) },
                    onRemove = { onRemoveItem(item.id) }
                )
            }
            
            // Order Summary Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Order Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            item {
                OrderSummaryCard(
                    subtotal = uiState.subtotal,
                    deliveryFee = uiState.deliveryFee,
                    total = uiState.total
                )
            }
            
            // Payment Method Section
            item {
                Text(
                    text = "Payment Method",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            items(uiState.paymentMethods) { method ->
                PaymentMethodCard(
                    method = method,
                    onSelect = { onPaymentMethodSelected(method.id) }
                )
            }
            
            // Delivery Address Section
            item {
                Text(
                    text = "Delivery Address",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            item {
                DeliveryAddressCard(
                    address = uiState.deliveryAddress,
                    onChange = onChangeAddress
                )
            }
            
            // Checkout Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (uiState.checkoutMessage != null) {
                    Text(
                        text = uiState.checkoutMessage,
                        color = when (uiState.checkoutStatus) {
                            CheckoutStatus.SUCCESS -> Color(0xFF2E7D32)
                            CheckoutStatus.ERROR -> Color.Red
                            else -> Color.Gray
                        },
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.checkoutStatus != CheckoutStatus.PROCESSING && !uiState.isLoading && uiState.items.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        disabledContainerColor = Color(0xFFFFC107)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            tint = Color.White
                        )
                        Text(
                            text = if (uiState.checkoutStatus == CheckoutStatus.PROCESSING) "Processing..." else "Checkout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                if (uiState.checkoutStatus == CheckoutStatus.PROCESSING) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
        }
    }
    
    // Card Payment Dialog
    if (uiState.showCardPaymentDialog) {
        CardPaymentDialog(
            onDismiss = onDismissCardPaymentDialog,
            onProcessPayment = onProcessCardPayment
        )
    }
    
    // Location Input Dialog
    if (uiState.showLocationDialog) {
        LocationInputDialog(
            currentAddress = uiState.deliveryAddress,
            onDismiss = onDismissLocationDialog,
            onConfirm = onUpdateDeliveryAddress
        )
    }
    
    if (uiState.showPaymentPrompt) {
        AlertDialog(
            onDismissRequest = onDismissPaymentPrompt,
            confirmButton = {
                TextButton(onClick = onDismissPaymentPrompt) {
                    Text("Got it")
                }
            },
            title = {
                Text(text = "Confirm STK Push")
            },
            text = {
                Text(
                    text = uiState.paymentPromptMessage
                        ?: "An STK push has been initiated. Approve it on your device to continue."
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = Color(0xFFE6F8EB),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vegetable_icon),
                    contentDescription = item.name,
                    modifier = Modifier.size(32.dp)
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
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "${item.quantity}${item.unit}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "KSh ${item.price.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            
            // Quantity Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { if (item.quantity > 1) onQuantityChange(item.quantity - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "−",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = item.quantity.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    subtotal: Double,
    deliveryFee: Double,
    total: Double
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal:", fontSize = 14.sp, color = Color.Gray)
                Text("KSh ${subtotal.toInt()}", fontSize = 14.sp, color = Color.Black)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Delivery Fee:", fontSize = 14.sp, color = Color.Gray)
                Text("KSh ${deliveryFee.toInt()}", fontSize = 14.sp, color = Color.Black)
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "KSh ${total.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (method.isSelected) Color(0xFFE6F8EB) else Color.White
        ),
        border = if (method.isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50))
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = method.iconRes),
                    contentDescription = method.name,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = method.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = method.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            if (method.isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                RadioButton(
                    selected = false,
                    onClick = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DeliveryAddressCard(
    address: DeliveryAddress,
    onChange: () -> Unit
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
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = address.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = address.location,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = address.details,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            TextButton(onClick = onChange) {
                Text(
                    text = "Change",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CardPaymentDialog(
    onDismiss: () -> Unit,
    onProcessPayment: (String, String, String, String) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Card Payment",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
                
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { 
                        // Format card number (add spaces every 4 digits)
                        val cleaned = it.filter { it.isDigit() }
                        cardNumber = cleaned.chunked(4).joinToString(" ").take(19)
                    },
                    label = { Text("Card Number") },
                    placeholder = { Text("1234 5678 9012 3456") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { 
                            // Format expiry date (MM/YY)
                            val cleaned = it.filter { it.isDigit() }
                            expiryDate = when {
                                cleaned.length <= 2 -> cleaned
                                else -> "${cleaned.take(2)}/${cleaned.drop(2).take(2)}"
                            }
                        },
                        label = { Text("Expiry") },
                        placeholder = { Text("MM/YY") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { 
                            cvv = it.filter { it.isDigit() }.take(4)
                        },
                        label = { Text("CVV") },
                        placeholder = { Text("123") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    label = { Text("Cardholder Name") },
                    placeholder = { Text("John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    text = "Note: This is a test payment. Any card details will be accepted.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cardNumber.length < 13) {
                        errorMessage = "Please enter a valid card number"
                    } else if (!expiryDate.matches(Regex("\\d{2}/\\d{2}"))) {
                        errorMessage = "Please enter expiry date in MM/YY format"
                    } else if (cvv.length < 3) {
                        errorMessage = "Please enter a valid CVV"
                    } else if (cardholderName.isBlank()) {
                        errorMessage = "Please enter cardholder name"
                    } else {
                        errorMessage = null
                        onProcessPayment(cardNumber, expiryDate, cvv, cardholderName)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Pay Now", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LocationInputDialog(
    currentAddress: DeliveryAddress,
    onDismiss: () -> Unit,
    onConfirm: (DeliveryAddress) -> Unit
) {
    var locationName by remember { mutableStateOf(currentAddress.location) }
    var details by remember { mutableStateOf(currentAddress.details) }
    var label by remember { mutableStateOf(currentAddress.label) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delivery Location",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter your delivery location to calculate delivery fee",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Address Label") },
                    placeholder = { Text("Home, Office, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Location") },
                    placeholder = { Text("Kilimani, Westlands, Karen, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details") },
                    placeholder = { Text("Building, Street, Apartment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    text = "Delivery fee will be calculated automatically based on distance from our warehouse.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val coordinates = DistanceCalculator.getCoordinatesForLocation(locationName)
                    val newAddress = currentAddress.copy(
                        label = label,
                        location = locationName,
                        details = details,
                        latitude = coordinates.first,
                        longitude = coordinates.second
                    )
                    onConfirm(newAddress)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

