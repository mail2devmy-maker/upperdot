package com.mail2dev.upperdot.ui.profile_settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.network.GoogleAuthService
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.PreferenceRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class UserSummary(
    val name: String = "",
    val email: String = "",
    val isPremium: Boolean = true,
    val lastSync: String = "Not Synced",
    val contactCount: Int = 0,
    val noteCount: Int = 0,
    val transactionCount: Int = 0
)

class ProfileSettingsViewModel(
    private val authService: GoogleAuthService,
    private val contactRepository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    private val preferenceRepository: PreferenceRepository,
    context: Context
) : ViewModel() {

    private val _userSummary = MutableStateFlow(UserSummary())
    val userSummary: StateFlow<UserSummary> = _userSummary.asStateFlow()

    init {
        loadUserData(context)
    }

    private fun loadUserData(context: Context) {
        val account = authService.getLastSignedInAccount(context)
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())

        viewModelScope.launch {
            combine(
                contactRepository.contactCount,
                noteRepository.noteCount,
                transactionRepository.transactionCount,
                preferenceRepository.preferences
            ) { contacts, notes, trans, prefs ->
                UserSummary(
                    name = account?.displayName ?: "Guest User",
                    email = account?.email ?: "local.only@upperdot.dev",
                    isPremium = true, // Mocked as premium for now
                    lastSync = if (prefs.lastSyncTime > 0) dateFormatter.format(Date(prefs.lastSyncTime)) else "Not Synced",
                    contactCount = contacts,
                    noteCount = notes,
                    transactionCount = trans
                )
            }.collect {
                _userSummary.value = it
            }
        }
    }

    fun onSignOut(onSuccess: () -> Unit) {
        authService.signOut {
            onSuccess()
        }
    }
}
