package com.example.farmdirect.ui.admin

import androidx.lifecycle.ViewModel
import com.example.farmdirect.utils.FirebaseUtils
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserManagementUiState(
    val totalUsers: Int = 0,
    val farmers: Int = 0,
    val consumers: Int = 0,
    val admins: Int = 0,
    val users: List<AdminUser> = emptyList(),
    val searchQuery: String = "",
    val selectedRoleFilter: String? = null,
    val selectedStatusFilter: UserStatus? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UserManagementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    private var usersListener: ListenerRegistration? = null

    init {
        observeUsers()
    }

    private fun observeUsers() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        usersListener?.remove()
        usersListener = FirebaseUtils.firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load users"
                    )
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { doc ->
                    val role = doc.getString("role") ?: "consumer"
                    val statusString = doc.getString("status") ?: "ACTIVE"
                    val status = runCatching { UserStatus.valueOf(statusString.uppercase()) }
                        .getOrElse { UserStatus.ACTIVE }
                    val createdAt = doc.getTimestamp("createdAt")?.toDate()
                    val joinDate = if (createdAt != null) {
                        val month = java.text.SimpleDateFormat(
                            "MMM",
                            java.util.Locale.getDefault()
                        ).format(createdAt)
                        val year = java.text.SimpleDateFormat(
                            "yyyy",
                            java.util.Locale.getDefault()
                        ).format(createdAt)
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
                        status = status
                    )
                }.orEmpty()

                val farmers = users.count { it.role.equals("farmer", ignoreCase = true) }
                val consumers = users.count { it.role.equals("consumer", ignoreCase = true) }
                val admins = users.count { it.role.equals("admin", ignoreCase = true) }

                _uiState.value = _uiState.value.copy(
                    users = users,
                    totalUsers = users.size,
                    farmers = farmers,
                    consumers = consumers,
                    admins = admins,
                    isLoading = false
                )
            }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setRoleFilter(role: String?) {
        _uiState.value = _uiState.value.copy(selectedRoleFilter = role)
    }

    fun setStatusFilter(status: UserStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatusFilter = status)
    }

    fun updateUserStatus(userId: String, status: UserStatus) {
        FirebaseUtils.firestore.collection("users").document(userId)
            .update("status", status.name)
    }

    fun deleteUser(userId: String) {
        FirebaseUtils.firestore.collection("users").document(userId)
            .delete()
    }

    override fun onCleared() {
        super.onCleared()
        usersListener?.remove()
    }
}

