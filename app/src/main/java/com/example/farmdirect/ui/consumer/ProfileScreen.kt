package com.example.farmdirect.ui.consumer

import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirect.R
import com.example.farmdirect.ui.LoginActivity
import com.example.farmdirect.utils.FirebaseUtils

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<ProfileScreenType?>(null) }
    
    when (currentScreen) {
        ProfileScreenType.MY_ACCOUNT -> {
            MyAccountScreen(onBack = { currentScreen = null })
        }
        ProfileScreenType.SAVED_BENEFICIARY -> {
            SavedBeneficiaryScreen(onBack = { currentScreen = null })
        }
        ProfileScreenType.SECURITY -> {
            SecuritySettingsScreen(
                onBack = { currentScreen = null },
                onFaceIdToggle = { enabled ->
                    viewModel.toggleFaceId(enabled)
                },
                onTwoFactorToggle = {
                    viewModel.toggleTwoFactor()
                }
            )
        }
        ProfileScreenType.HELP -> {
            HelpSupportScreen(onBack = { currentScreen = null })
        }
        ProfileScreenType.ABOUT -> {
            AboutAppScreen(onBack = { currentScreen = null })
        }
        null -> {
            ProfileScreen(
                uiState = uiState,
                onMenuItemClick = { menuItem ->
                    when (menuItem.id) {
                        "logout" -> {
                            FirebaseUtils.auth.signOut()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                        "my_account" -> {
                            currentScreen = ProfileScreenType.MY_ACCOUNT
                        }
                        "saved_beneficiary" -> {
                            currentScreen = ProfileScreenType.SAVED_BENEFICIARY
                        }
                        "face_id", "two_factor" -> {
                            currentScreen = ProfileScreenType.SECURITY
                        }
                        "help" -> {
                            currentScreen = ProfileScreenType.HELP
                        }
                        "about" -> {
                            currentScreen = ProfileScreenType.ABOUT
                        }
                        else -> {
                            menuItem.onClick()
                        }
                    }
                },
                onEditProfile = { /* TODO: Navigate to edit profile */ }
            )
        }
    }
}

enum class ProfileScreenType {
    MY_ACCOUNT,
    SAVED_BENEFICIARY,
    SECURITY,
    HELP,
    ABOUT
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onMenuItemClick: (ProfileMenuItem) -> Unit,
    onEditProfile: () -> Unit
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
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Profile Banner
            item {
                ProfileBanner(
                    userName = uiState.userName,
                    userHandle = uiState.userHandle,
                    onEdit = onEditProfile
                )
            }
            
            // My Account Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "My Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            items(uiState.accountMenuItems) { item ->
                ProfileMenuItemCard(
                    item = item,
                    onClick = { onMenuItemClick(item) }
                )
            }
            
            // More Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "More",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            items(uiState.moreMenuItems) { item ->
                ProfileMenuItemCard(
                    item = item,
                    onClick = { onMenuItemClick(item) }
                )
            }
            
            // Logout Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        uiState.accountMenuItems.find { it.id == "logout" }?.let { 
                            onMenuItemClick(it) 
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
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
fun ProfileBanner(
    userName: String,
    userHandle: String,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2E7D32)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // User Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = userHandle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            // Edit Button
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItemCard(
    item: ProfileMenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.title,
                    tint = if (item.id == "help" || item.id == "about") {
                        Color(0xFFFFC107)
                    } else {
                        Color(0xFF4CAF50)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    if (item.hasWarning) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            // Action
            if (item.hasToggle) {
                Switch(
                    checked = item.toggleState,
                    onCheckedChange = { onClick() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4CAF50),
                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Navigate",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

