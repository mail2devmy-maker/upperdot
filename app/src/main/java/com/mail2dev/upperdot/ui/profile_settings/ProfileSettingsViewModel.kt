package com.mail2dev.upperdot.ui.profile_settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSummary(
    val name: String = "Beta Tester",
    val email: String = "beta.tester@upperdot.dev",
    val isPremium: Boolean = true,
    val lastSync: String = "Jul 30, 2026 12:31:15 AM",
    val contactCount: Int = 2,
    val noteCount: Int = 1,
    val transactionCount: Int = 2
)

class ProfileSettingsViewModel : ViewModel() {

    private val _userSummary = MutableStateFlow(UserSummary())
    val userSummary: StateFlow<UserSummary> = _userSummary.asStateFlow()

    fun onRefreshSync() {
        // TODO: Trigger Google Drive Sync
    }

    fun onSignOut(onSuccess: () -> Unit) {
        // TODO: Clear local session tokens and invalidate Drive session
        onSuccess()
    }
}
