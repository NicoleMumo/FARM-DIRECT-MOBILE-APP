package com.example.farmdirect.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserManagementUiState(
    val totalUsers: Int = 156,
    val farmers: Int = 89,
    val consumers: Int = 67,
    val users: List<AdminUser> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class UserManagementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        FirebaseUtils.firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    val role = doc.getString("role") ?: "consumer"
                    val createdAt = doc.getTimestamp("createdAt")?.toDate()
                    val joinDate = if (createdAt != null) {
                        val month = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(createdAt)
                        val year = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(createdAt)
                        "$month $year"
                    } else {
                        "Jan 2024"
                    }
                    
                    AdminUser(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        email = doc.getString("email") ?: "",
                        role = role,
                        joinDate = joinDate,
                        status = UserStatus.ACTIVE // Default, can be enhanced with status field
                    )
                }
                
                val farmers = users.count { it.role == "farmer" }
                val consumers = users.count { it.role == "consumer" }
                
                _uiState.value = _uiState.value.copy(
                    users = users,
                    totalUsers = users.size,
                    farmers = farmers,
                    consumers = consumers,
                    isLoading = false
                )
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredUsers(): List<AdminUser> {
        val query = _uiState.value.searchQuery.lowercase()
        return if (query.isBlank()) {
            _uiState.value.users
        } else {
            _uiState.value.users.filter {
                it.name.lowercase().contains(query) ||
                it.email.lowercase().contains(query) ||
                it.role.lowercase().contains(query)
            }
        }
    }

    fun updateUserStatus(userId: String, status: UserStatus) {
        FirebaseUtils.firestore.collection("users").document(userId)
            .update("status", status.name)
            .addOnSuccessListener {
                loadUsers()
            }
    }

    fun deleteUser(userId: String) {
        FirebaseUtils.firestore.collection("users").document(userId)
            .delete()
            .addOnSuccessListener {
                loadUsers()
            }
    }
}

