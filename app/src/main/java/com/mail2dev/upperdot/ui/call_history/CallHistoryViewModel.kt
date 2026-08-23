package com.mail2dev.upperdot.ui.call_history

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.telecom.TelecomManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.repository.telephony.CallLogRepository
import com.mail2dev.upperdot.telecom.UpperDotInCallService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CallLogEntry(
    val id: String,
    val contactId: Long?,
    val name: String?,
    val avatarPath: String? = null,
    val number: String,
    val timestamp: Long,
    val type: Int // Incoming, Outgoing, Missed
)

data class GroupedCallLog(
    val name: String?,
    val avatarPath: String? = null,
    val number: String,
    val contactId: Long?,
    val calls: List<CallLogEntry>,
    val isExpanded: Boolean = false
)

class CallHistoryViewModel(
    private val repository: CallLogRepository,
    private val context: Context
) : ViewModel() {

    companion object {
        private var cachedLogs: List<GroupedCallLog> = emptyList()
    }

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _groupedCallLogs = MutableStateFlow<List<GroupedCallLog>>(cachedLogs)
    val groupedCallLogs: StateFlow<List<GroupedCallLog>> = _groupedCallLogs.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun updatePermissionState(granted: Boolean) {
        _hasPermission.value = granted
        if (granted) {
            refreshCallLogs()
            clearMissedCallNotifications()
        }
    }

    @SuppressLint("MissingPermission")
    private fun clearMissedCallNotifications() {
        try {
            // 1. Clear system missed call notifications
            // Note: cancelMissedCallsNotification() requires the app to be the default dialer.
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            telecomManager?.cancelMissedCallsNotification()

            // 2. Clear UpperDot custom missed call notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(UpperDotInCallService.MISSED_CALL_NOTIFICATION_ID)
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun refreshCallLogs() {
        if (_isRefreshing.value) return
        
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val logs = repository.getCallLogs()
                val grouped = groupCallLogs(logs)
                cachedLogs = grouped
                _groupedCallLogs.value = grouped
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun groupCallLogs(logs: List<CallLogEntry>): List<GroupedCallLog> {
        val result = mutableListOf<GroupedCallLog>()
        if (logs.isEmpty()) return result

        var currentGroup = mutableListOf<CallLogEntry>()
        var lastNumber = ""

        for (log in logs) {
            // Group consecutive calls from the same contact/number
            if (com.mail2dev.upperdot.util.ContactUtils.isSamePhoneNumber(log.number, lastNumber)) {
                currentGroup.add(log)
            } else {
                if (currentGroup.isNotEmpty()) {
                    val first = currentGroup.first()
                    result.add(
                        GroupedCallLog(
                            name = first.name,
                            avatarPath = first.avatarPath,
                            number = first.number,
                            contactId = first.contactId,
                            calls = currentGroup.toList()
                        )
                    )
                }
                currentGroup = mutableListOf(log)
                lastNumber = log.number
            }
        }

        if (currentGroup.isNotEmpty()) {
            val first = currentGroup.first()
            result.add(
                GroupedCallLog(
                    name = first.name,
                    avatarPath = first.avatarPath,
                    number = first.number,
                    contactId = first.contactId,
                    calls = currentGroup.toList()
                )
            )
        }

        return result
    }

    fun toggleExpand(group: GroupedCallLog) {
        _groupedCallLogs.value = _groupedCallLogs.value.map {
            if ((it.number == group.number) && (it.calls.first().timestamp == group.calls.first().timestamp)) {
                it.copy(isExpanded = !it.isExpanded)
            } else it
        }
    }

    fun onAddContactClicked(number: String) {
        // Handled via onNavigateToAddContact in Screen
    }
}
