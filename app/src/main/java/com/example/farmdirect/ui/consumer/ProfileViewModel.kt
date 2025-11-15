package com.example.farmdirect.ui.consumer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.models.User
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val userName: String = "",
    val userHandle: String = "",
    val accountMenuItems: List<ProfileMenuItem> = emptyList(),
    val moreMenuItems: List<ProfileMenuItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val userId = FirebaseUtils.auth.currentUser?.uid ?: ""
    
    init {
        fetchUserProfile()
        initializeMenuItems()
    }
    
    private fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userDoc = FirebaseUtils.firestore
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val user = userDoc.toObject(User::class.java)
                user?.let {
                    val handle = "@${it.email.split("@").firstOrNull() ?: "user"}"
                    _uiState.value = _uiState.value.copy(
                        userName = it.name,
                        userHandle = handle,
                        isLoading = false
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        userName = "User",
                        userHandle = "@user",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }
    
    private fun initializeMenuItems() {
        val accountItems = listOf(
            ProfileMenuItem(
                id = "my_account",
                title = "My Account",
                subtitle = "Make changes to your account",
                iconRes = com.example.farmdirect.R.drawable.ic_seed, // TODO: Add proper icons
                hasWarning = true,
                onClick = {}
            ),
            ProfileMenuItem(
                id = "saved_beneficiary",
                title = "Saved Beneficiary",
                subtitle = "Manage your saved account",
                iconRes = com.example.farmdirect.R.drawable.ic_seed,
                onClick = {}
            ),
            ProfileMenuItem(
                id = "face_id",
                title = "Face ID / Touch ID",
                subtitle = "Manage your device security",
                iconRes = com.example.farmdirect.R.drawable.ic_seed,
                hasToggle = true,
                toggleState = false,
                onClick = {}
            ),
            ProfileMenuItem(
                id = "two_factor",
                title = "Two-Factor Authentication",
                subtitle = "Further secure your account for safety",
                iconRes = com.example.farmdirect.R.drawable.ic_seed,
                onClick = {}
            ),
            ProfileMenuItem(
                id = "logout",
                title = "Log out",
                subtitle = "Sign out from your account",
                iconRes = com.example.farmdirect.R.drawable.ic_seed,
                onClick = {}
            )
        )
        
        val moreItems = listOf(
            ProfileMenuItem(
                id = "help",
                title = "Help & Support",
                subtitle = "Get help and contact support",
                iconRes = com.example.farmdirect.R.drawable.ic_seed,
                onClick = {}
            ),
            ProfileMenuItem(
                id = "about",
                title = "About App",
                subtitle = "Learn more about FarmDirect",
                iconRes = com.example.farmdirect.R.drawable.ic_seed,
                onClick = {}
            )
        )
        
        _uiState.value = _uiState.value.copy(
            accountMenuItems = accountItems,
            moreMenuItems = moreItems
        )
    }
    
    fun toggleFaceId(enabled: Boolean) {
        val updatedItems = _uiState.value.accountMenuItems.map {
            if (it.id == "face_id") {
                it.copy(toggleState = enabled)
            } else {
                it
            }
        }
        _uiState.value = _uiState.value.copy(accountMenuItems = updatedItems)
    }
    
    fun toggleTwoFactor() {
        // TODO: Implement two-factor authentication setup
        android.util.Log.d("ProfileViewModel", "Two-factor authentication toggle clicked")
    }
}

