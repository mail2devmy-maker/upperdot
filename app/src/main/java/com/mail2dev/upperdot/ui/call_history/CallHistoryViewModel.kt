package com.mail2dev.upperdot.ui.call_history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CallLogEntry(
    val id: String,
    val name: String?,
    val number: String,
    val timestamp: Long,
    val type: Int // Incoming, Outgoing, Missed
)

class CallHistoryViewModel : ViewModel() {

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLogEntry>>(emptyList())
    val callLogs: StateFlow<List<CallLogEntry>> = _callLogs.asStateFlow()

    fun updatePermissionState(granted: Boolean) {
        _hasPermission.value = granted
        if (granted) {
            loadCallLogs()
        }
    }

    private fun loadCallLogs() {
        // TODO: Load from Room DB
    }

    fun onAddContactClicked(number: String) {
        // Navigation to Add Contact Wizard with pre-filled number
    }

    fun onContactClicked(contactId: String) {
        // Navigation to Client Profile
    }
}
