package com.example.farmdirect.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirect.R
import java.util.Locale

@Composable
fun UserManagementRoute(
    viewModel: UserManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Compute filtered users reactively
    val filteredUsers = remember(
        uiState.searchQuery,
        uiState.users,
        uiState.selectedRoleFilter,
        uiState.selectedStatusFilter
    ) {
        val query = uiState.searchQuery.lowercase()
        uiState.users.filter { user ->
            val matchesQuery = query.isBlank() ||
                user.name.lowercase().contains(query) ||
                user.email.lowercase().contains(query) ||
                user.role.lowercase().contains(query)
            val matchesRole = uiState.selectedRoleFilter == null ||
                user.role.equals(uiState.selectedRoleFilter, ignoreCase = true)
            val matchesStatus = uiState.selectedStatusFilter == null ||
                user.status == uiState.selectedStatusFilter
            matchesQuery && matchesRole && matchesStatus
        }
    }
    
    UserManagementScreen(
        uiState = uiState,
        filteredUsers = filteredUsers,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onRoleFilterChanged = viewModel::setRoleFilter,
        onStatusFilterChanged = viewModel::setStatusFilter,
        onUpdateUserStatus = viewModel::updateUserStatus,
        onDeleteUser = viewModel::deleteUser
    )
}

@Composable
fun UserManagementScreen(
    uiState: UserManagementUiState,
    filteredUsers: List<AdminUser>,
    onSearchQueryChanged: (String) -> Unit,
    onRoleFilterChanged: (String?) -> Unit,
    onStatusFilterChanged: (UserStatus?) -> Unit,
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
            title = "FarmDirect"
        )
        uiState.errorMessage?.let {
            ErrorMessageCard(
                message = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            value = uiState.consumers.toString(),
                            label = "Consumers",
                            bgColor = Color(0xFFE3F2FD),
                            textColor = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = uiState.admins.toString(),
                            label = "Admins",
                            bgColor = Color(0xFFFFEBEE),
                            textColor = Color(0xFFE53935),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Search and Filter
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search users by name, email, or role...") },
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
                    RoleFilterRow(
                        selectedRole = uiState.selectedRoleFilter,
                        onRoleSelected = onRoleFilterChanged
                    )
                    StatusFilterRow(
                        selectedStatus = uiState.selectedStatusFilter,
                        onStatusSelected = onStatusFilterChanged
                    )
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

@OptIn(ExperimentalLayoutApi::class)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name
                            .split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2)
                            .joinToString(""),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = user.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        maxLines = 1
                    )
                    Text(
                        text = user.email,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                StatusChip(status = user.status)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoPill(
                    label = user.role.replaceFirstChar { it.uppercase(Locale.getDefault()) },
                    leadingText = user.role.firstOrNull()?.uppercase()?.toString()
                )
                InfoPill(
                    label = "Joined ${user.joinDate}",
                    leadingText = "J"
                )
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Hook into messaging */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Email user")
                }
                StatusActionButton(
                    status = user.status,
                    onStatusChange = onStatusChange,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun RoleFilterRow(
    selectedRole: String?,
    onRoleSelected: (String?) -> Unit
) {
    val roles = listOf(
        "All" to null,
        "Farmers" to "farmer",
        "Consumers" to "consumer",
        "Admins" to "admin"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        roles.forEach { (label, roleValue) ->
            FilterChip(
                selected = selectedRole == roleValue,
                onClick = { onRoleSelected(roleValue) },
                label = { Text(label) },
                shape = RoundedCornerShape(24.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2E7D32),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun StatusFilterRow(
    selectedStatus: UserStatus?,
    onStatusSelected: (UserStatus?) -> Unit
) {
    val statuses = listOf(
        "All Statuses" to null,
        "Active" to UserStatus.ACTIVE,
        "Suspended" to UserStatus.SUSPENDED,
        "Pending" to UserStatus.PENDING
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statuses.forEach { (label, statusValue) ->
            FilterChip(
                selected = selectedStatus == statusValue,
                onClick = { onStatusSelected(statusValue) },
                label = { Text(label) },
                shape = RoundedCornerShape(24.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun StatusActionButton(
    status: UserStatus,
    onStatusChange: (UserStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val (label, nextStatus, containerColor) = when (status) {
        UserStatus.ACTIVE -> Triple("Suspend", UserStatus.SUSPENDED, Color(0xFFFFE0B2))
        UserStatus.SUSPENDED -> Triple("Activate", UserStatus.ACTIVE, Color(0xFFE8F5E9))
        UserStatus.PENDING -> Triple("Approve", UserStatus.ACTIVE, Color(0xFFE3F2FD))
    }
    FilledTonalButton(
        onClick = { onStatusChange(nextStatus) },
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = Color(0xFF2E7D32)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label)
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

@Composable
fun InfoPill(
    label: String,
    leadingText: String? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F3F4)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingText?.let { text ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.LightGray.copy(alpha = 0.6f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFEBEE)
    ) {
        Text(
            text = message,
            color = Color(0xFFD32F2F),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

