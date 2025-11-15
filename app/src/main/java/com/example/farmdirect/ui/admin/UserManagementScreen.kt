package com.example.farmdirect.ui.admin

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
import androidx.compose.runtime.remember
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
fun UserManagementRoute(
    viewModel: UserManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Compute filtered users reactively
    val filteredUsers = remember(uiState.searchQuery, uiState.users) {
        val query = uiState.searchQuery.lowercase()
        if (query.isBlank()) {
            uiState.users
        } else {
            uiState.users.filter {
                it.name.lowercase().contains(query) ||
                it.email.lowercase().contains(query) ||
                it.role.lowercase().contains(query)
            }
        }
    }
    
    UserManagementScreen(
        uiState = uiState,
        filteredUsers = filteredUsers,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onUpdateUserStatus = viewModel::updateUserStatus,
        onDeleteUser = viewModel::deleteUser
    )
}

@Composable
fun UserManagementScreen(
    uiState: UserManagementUiState,
    filteredUsers: List<AdminUser>,
    onSearchQueryChanged: (String) -> Unit,
    onUpdateUserStatus: (String, UserStatus) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
    ) {
        // Header
        AdminHeader(
            title = "FarmDirect",
            notificationCount = 3
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title and Add User Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Management",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Button(
                        onClick = { /* Add User */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add User")
                    }
                }
            }
            
            // Statistics Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = uiState.totalUsers.toString(),
                        label = "Total Users",
                        bgColor = Color(0xFFFFF9C4),
                        textColor = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = uiState.farmers.toString(),
                        label = "Farmers",
                        bgColor = Color(0xFFE8F5E9),
                        textColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = uiState.consumers.toString(),
                        label = "Consumers",
                        bgColor = Color(0xFFE3F2FD),
                        textColor = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Search and Filter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search users...") },
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
                    IconButton(
                        onClick = { /* Filter */ },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Filter"
                        )
                    }
                }
            }
            
            // User List
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                }
            } else if (filteredUsers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No users found",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            if (uiState.users.isEmpty()) {
                                Text(
                                    text = "No users in database",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = "Try adjusting your search",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredUsers) { user ->
                    UserItem(
                        user = user,
                        onStatusChange = { status -> onUpdateUserStatus(user.id, status) },
                        onDelete = { onDeleteUser(user.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    bgColor: Color,
    textColor: Color,
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
                color = textColor
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun UserItem(
    user: AdminUser,
    onStatusChange: (UserStatus) -> Unit,
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
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // User Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (user.role == "farmer") {
                        Image(
                            painter = painterResource(id = R.drawable.ic_seed),
                            contentDescription = user.role,
                            modifier = Modifier.size(16.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF4CAF50))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = user.role,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF2196F3)
                        )
                    }
                    Text(
                        text = user.role.capitalize(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = user.joinDate,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = user.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // Actions and Status
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Edit */ }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
                StatusChip(status = user.status)
            }
        }
    }
}

@Composable
fun StatusChip(status: UserStatus) {
    val (text, color) = when (status) {
        UserStatus.ACTIVE -> "Active" to Color(0xFFE8F5E9)
        UserStatus.SUSPENDED -> "Suspended" to Color(0xFFFFEBEE)
        UserStatus.PENDING -> "Pending" to Color(0xFFFFF9C4)
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = when (status) {
                UserStatus.ACTIVE -> Color(0xFF4CAF50)
                UserStatus.SUSPENDED -> Color(0xFFE53935)
                UserStatus.PENDING -> Color(0xFFFF9800)
            }
        )
    }
}

