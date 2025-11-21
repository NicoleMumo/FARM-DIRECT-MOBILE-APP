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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmdirect.R

data class OrderDetails(
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val deliveryDate: String,
    val timeline: List<OrderTimelineItem>,
    val items: List<OrderItem>,
    val paymentInfo: PaymentInfo,
    val deliveryInfo: DeliveryInfo,
    val customerInfo: CustomerInfo
)

data class OrderTimelineItem(
    val status: String,
    val date: String,
    val time: String,
    val description: String,
    val isActive: Boolean
)

data class OrderItem(
    val name: String,
    val category: String,
    val description: String,
    val quantity: Int,
    val price: Double,
    val iconRes: Int
)

data class PaymentInfo(
    val method: String,
    val maskedNumber: String,
    val transactionId: String,
    val status: String,
    val subtotal: Double,
    val shipping: Double,
    val tax: Double,
    val total: Double
)

data class DeliveryInfo(
    val option: String,
    val address: Address,
    val contact: String,
    val courier: String,
    val trackingNumber: String
)

data class Address(
    val name: String,
    val location: String,
    val details: String
)

data class CustomerInfo(
    val customerId: String,
    val email: String,
    val specialInstructions: String
)

@Composable
fun OrderDetailsScreen(
    order: Order,
    orderDetails: OrderDetails? = null,
    onBack: () -> Unit,
    onTrackPackage: () -> Unit,
    onReorder: () -> Unit
) {
    val details = orderDetails ?: getDefaultOrderDetails(order)
    
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Order Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Order Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order #${details.orderNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        StatusBadge(status = details.status)
                    }
                    Text(
                        text = "Ksh ${details.totalAmount.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Delivered on ${details.deliveryDate}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Order Timeline
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Order Timeline",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    details.timeline.forEachIndexed { index, item ->
                        OrderTimelineItem(item = item, isLast = index == details.timeline.size - 1)
                    }
                }
            }
            
            // Items Ordered
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Items Ordered",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    details.items.forEach { item ->
                        OrderItemRow(item = item)
                    }
                }
            }
            
            // Payment Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Payment Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    InfoRow("Payment Method", "${details.paymentInfo.method} ${details.paymentInfo.maskedNumber}")
                    InfoRow("Transaction ID", details.paymentInfo.transactionId)
                    InfoRow("Payment Status", details.paymentInfo.status, isSuccess = true)
                    Divider()
                    InfoRow("Subtotal", "Ksh ${details.paymentInfo.subtotal.toInt()}")
                    InfoRow("Shipping", "Ksh ${details.paymentInfo.shipping.toInt()}")
                    InfoRow("Tax", "Ksh ${details.paymentInfo.tax.toInt()}")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Ksh ${details.paymentInfo.total.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
            
            // Delivery Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Delivery Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    InfoRow("Delivery Option", details.deliveryInfo.option)
                    Column {
                        Text(
                            text = "Shipping Address",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = details.deliveryInfo.address.name,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = details.deliveryInfo.address.location,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = details.deliveryInfo.address.details,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    InfoRow("Contact", details.deliveryInfo.contact)
                    InfoRow("Courier", details.deliveryInfo.courier)
                    InfoRow("Tracking Number", details.deliveryInfo.trackingNumber)
                }
            }
            
            // Customer Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customer Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    InfoRow("Customer ID", details.customerInfo.customerId)
                    InfoRow("Email", details.customerInfo.email)
                    InfoRow("Special Instructions", details.customerInfo.specialInstructions)
                }
            }
        }
        
        // Bottom Action Buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onTrackPackage,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Track Package", fontSize = 14.sp)
                }
                Button(
                    onClick = onReorder,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reorder Items", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun OrderTimelineItem(item: OrderTimelineItem, isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (item.isActive) Color(0xFF4CAF50) else Color.Gray,
                        CircleShape
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(if (item.isActive) Color(0xFF4CAF50) else Color.Gray)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.status,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isActive) Color(0xFF4CAF50) else Color.Gray
            )
            Text(
                text = "${item.date} - ${item.time}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = item.description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFFFF3D8), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.name,
                modifier = Modifier.size(32.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = item.category,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = item.description,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "Qty: ${item.quantity}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            text = "Ksh ${item.price.toInt()}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, isSuccess: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        if (isSuccess) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Text(
                        text = value,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        } else {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

fun getDefaultOrderDetails(order: Order): OrderDetails {
    return OrderDetails(
        orderNumber = order.orderNumber,
        status = order.status,
        totalAmount = order.price,
        deliveryDate = order.orderDate,
        timeline = listOf(
            OrderTimelineItem("Delivered", "March 15, 2025", "2:30 PM", "Package delivered to front door", true),
            OrderTimelineItem("Out for Delivery", "March 15, 2025", "9:15 AM", "Package is on the delivery truck", false),
            OrderTimelineItem("Shipped", "March 13, 2025", "10:45 AM", "Package dispatched from farm", false),
            OrderTimelineItem("Order Confirmed", "March 12, 2025", "3:20 PM", "Order placed successfully", false)
        ),
        items = listOf(
            OrderItem(
                name = order.productName,
                category = "Grains",
                description = "Fresh, corn grown without pesticides...",
                quantity = 1,
                price = order.price,
                iconRes = R.drawable.grain_icon
            )
        ),
        paymentInfo = PaymentInfo(
            method = "M-Pesa",
            maskedNumber = "**** **** 8981",
            transactionId = "TXN-789456123",
            status = "Successful",
            subtotal = order.price - 50.0,
            shipping = 50.0,
            tax = 0.0,
            total = order.price
        ),
        deliveryInfo = DeliveryInfo(
            option = "Standard Delivery",
            address = Address(
                name = "John Kip",
                location = "Kilimani, Nairobi",
                details = "Building 10, Apt 48"
            ),
            contact = "+2547123456789",
            courier = "ENN",
            trackingNumber = "12999AA1234567890"
        ),
        customerInfo = CustomerInfo(
            customerId = "CUST-789123",
            email = "j***@email.com",
            specialInstructions = "Leave package at front door"
        )
    )
}

