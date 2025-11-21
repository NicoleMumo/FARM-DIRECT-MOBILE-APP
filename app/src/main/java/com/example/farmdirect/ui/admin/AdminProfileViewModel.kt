package com.example.farmdirect.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirect.utils.FirebaseUtils
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlin.math.max

data class AdminProfileUiState(
    val name: String = "Administrator",
    val role: String = "Platform Administrator",
    val email: String = "",
    val phoneNumber: String = "",
    val region: String = "Kenya",
    val lastLogin: String = "—",
    val accountStatus: String = "ACTIVE",
    val tenureMonths: Int = 0,
    val escalationsHandled: Int = 0,
    val partnerFarmers: Int = 0,
    val pendingOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val notificationsEnabled: Boolean = true,
    val biometricsEnabled: Boolean = false,
    val twoFactorEnabled: Boolean = true,
    val auditLogCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminProfileUiState())
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseUtils.firestore
    private val auth = FirebaseUtils.auth
    private val timeFormatter = SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault())

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Please sign in as an admin to view profile details."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val preferences = userDoc.get("preferences") as? Map<*, *>
                val userName = userDoc.getString("name")
                    ?: auth.currentUser?.displayName
                    ?: _uiState.value.name
                val roleRaw = userDoc.getString("role") ?: "admin"
                val role = roleRaw.replace("_", " ").lowercase(Locale.getDefault())
                val roleDisplay = role.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
                val email = userDoc.getString("email") ?: auth.currentUser?.email.orEmpty()
                val phone = userDoc.getString("phone")
                    ?: userDoc.getString("phoneNumber")
                    ?: _uiState.value.phoneNumber
                val region = userDoc.getString("region")
                    ?: userDoc.getString("location")
                    ?: "Kenya"
                val statusRaw = userDoc.getString("status") ?: "ACTIVE"
                val status = statusRaw.lowercase(Locale.getDefault())
                val createdAtMillis = userDoc.getTimestamp("createdAt")?.toDate()?.time
                    ?: auth.currentUser?.metadata?.creationTimestamp
                    ?: System.currentTimeMillis()
                val lastLoginMillis = auth.currentUser?.metadata?.lastSignInTimestamp
                    ?: userDoc.getTimestamp("lastLogin")?.toDate()?.time
                val ordersSnapshot = firestore.collection("orders").get().await()
                val orders = ordersSnapshot.documents
                val pendingOrders = orders.count { doc ->
                    doc.getString("status").equals("PENDING", ignoreCase = true)
                }
                val deliveredOrders = orders.count { doc ->
                    val statusValue = doc.getString("status")?.uppercase(Locale.getDefault())
                    statusValue == "DELIVERED" || statusValue == "COMPLETED" || statusValue == "CONFIRMED"
                }
                val escalationsHandled = orders.count { doc ->
                    val statusValue = doc.getString("status")?.uppercase(Locale.getDefault())
                    statusValue == "CANCELLED"
                }
                val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                val auditLogCount = orders.count { doc ->
                    val created = doc.getTimestamp("createdAt")?.toDate()?.time ?: return@count false
                    created >= sevenDaysAgo
                }
                val farmersSnapshot = firestore.collection("users")
                    .whereEqualTo("role", "farmer")
                    .whereEqualTo("status", "ACTIVE")
                    .get()
                    .await()
                val partnerFarmers = farmersSnapshot.size()

                _uiState.value = _uiState.value.copy(
                    name = userName,
                    role = roleDisplay,
                    email = email,
                    phoneNumber = phone,
                    region = region,
                    accountStatus = status.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                    lastLogin = lastLoginMillis?.let { timeFormatter.format(Date(it)) } ?: "—",
                    tenureMonths = calculateTenureMonths(createdAtMillis),
                    pendingOrders = pendingOrders,
                    deliveredOrders = deliveredOrders,
                    escalationsHandled = escalationsHandled,
                    partnerFarmers = partnerFarmers,
                    auditLogCount = auditLogCount,
                    notificationsEnabled = preferences?.get("notificationsEnabled") as? Boolean
                        ?: _uiState.value.notificationsEnabled,
                    biometricsEnabled = preferences?.get("biometricsEnabled") as? Boolean
                        ?: _uiState.value.biometricsEnabled,
                    twoFactorEnabled = preferences?.get("twoFactorEnabled") as? Boolean
                        ?: _uiState.value.twoFactorEnabled,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load admin profile."
                )
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        persistPreference("notificationsEnabled", enabled)
    }

    fun toggleBiometrics(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(biometricsEnabled = enabled)
        persistPreference("biometricsEnabled", enabled)
    }

    fun toggleTwoFactor(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(twoFactorEnabled = enabled)
        persistPreference("twoFactorEnabled", enabled)
    }

    private fun persistPreference(key: String, value: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .set(
                        mapOf(
                            "preferences" to mapOf(key to value)
                        ),
                        SetOptions.merge()
                    )
            } catch (_: Exception) {
                // Ignored – UI already optimistic
            }
        }
    }

    private fun calculateTenureMonths(createdAtMillis: Long): Int {
        val diff = System.currentTimeMillis() - createdAtMillis
        val months = diff / (1000L * 60 * 60 * 24 * 30)
        return max(months.toInt(), 0)
    }
}

