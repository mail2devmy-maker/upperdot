package com.mail2dev.upperdot.ui.profile_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.network.GoogleAuthService
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context

data class UserSummary(
    val name: String = "",
    val email: String = "",
    val isPremium: Boolean = true,
    val lastSync: String = "Jul 30, 2026 12:31:15 AM",
    val contactCount: Int = 0,
    val noteCount: Int = 0,
    val transactionCount: Int = 0
)

class ProfileSettingsViewModel(
    private val authService: GoogleAuthService,
    private val contactRepository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    context: Context
) : ViewModel() {

    private val _userSummary = MutableStateFlow(UserSummary())
    val userSummary: StateFlow<UserSummary> = _userSummary.asStateFlow()

    init {
        loadUserData(context)
    }

    private fun loadUserData(context: Context) {
        val account = authService.getLastSignedInAccount(context)
        
        viewModelScope.launch {
            combine(
                contactRepository.contactCount,
                noteRepository.noteCount,
                transactionRepository.transactionCount
            ) { contacts, notes, trans ->
                UserSummary(
                    name = account?.displayName ?: "Guest User",
                    email = account?.email ?: "local.only@upperdot.dev",
                    isPremium = true, // Mocked as premium for now
                    lastSync = "Not Synced", // Placeholder
                    contactCount = contacts,
                    noteCount = notes,
                    transactionCount = trans
                )
            }.collect {
                _userSummary.value = it
            }
        }
    }

    fun onRefreshSync() {
        // TODO: Trigger Google Drive Sync
    }

    fun onSignOut(onSuccess: () -> Unit) {
        authService.signOut {
            onSuccess()
        }
    }
}
