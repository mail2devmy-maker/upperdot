package com.mail2dev.upperdot.ui.call_history

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.telephony.CallLogRepository
import com.mail2dev.upperdot.telecom.UpperDotInCallService
import com.mail2dev.upperdot.util.TelephonyUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CallLogEntry(
    val id: String,
    val contactId: Long?,
    val name: String?,
    val avatarPath: String? = null,
    val number: String,
    val timestamp: Long,
    val type: Int
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
    private val contactRepository: ContactRepository,
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

    private val _searchResults = MutableStateFlow<List<ContactEntity>>(emptyList())
    val searchResults: StateFlow<List<ContactEntity>> = _searchResults.asStateFlow()

    val favoriteContacts: StateFlow<List<ContactEntity>> = contactRepository.favoriteContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _availableSims = MutableStateFlow<List<SubscriptionInfo>>(emptyList())
    val availableSims: StateFlow<List<SubscriptionInfo>> = _availableSims.asStateFlow()

    private val _selectedSim = MutableStateFlow<SubscriptionInfo?>(null)
    val selectedSim: StateFlow<SubscriptionInfo?> = _selectedSim.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshSimInfo()
    }

    @SuppressLint("MissingPermission")
    fun refreshSimInfo() {
        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val infoList = subscriptionManager.activeSubscriptionInfoList ?: emptyList()
            _availableSims.value = infoList
            if (_selectedSim.value == null || !infoList.contains(_selectedSim.value)) {
                _selectedSim.value = infoList.firstOrNull()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleSim() {
        val current = _selectedSim.value
        val sims = _availableSims.value
        if (sims.size > 1) {
            val nextIndex = (sims.indexOf(current) + 1) % sims.size
            _selectedSim.value = sims[nextIndex]
        }
    }

    fun updatePermissionState(granted: Boolean) {
        _hasPermission.value = granted
        if (granted) {
            refreshCallLogs()
            clearMissedCallNotifications()
            refreshSimInfo()
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                val contacts = contactRepository.getAllContactsList()
                val filtered = contacts.filter { contact ->
                    // 1. Exclude system fallbacks or disabled entries
                    val isSystemDisabled = contact.fullName.contains("DISABLE_CALL", ignoreCase = true) || 
                                         contact.fullName.startsWith("DISABLE_", ignoreCase = true)
                    if (isSystemDisabled) return@filter false
                    
                    // 2. Lacks a valid phone number
                    if (contact.phoneNumbers.isEmpty()) return@filter false

                    // 3. T9 Logic
                    val t9Name = nameToT9(contact.fullName)
                    val phoneMatches = contact.phoneNumbers.any { it.contains(query) }
                    val nameMatches = t9Name.contains(query)
                    
                    phoneMatches || nameMatches
                }.distinctBy { it.sanitizedPrimaryPhone } // 4. Filter out duplicate user contacts

                _searchResults.value = filtered
            }
        } else {
            _searchResults.value = emptyList()
        }
    }

    private fun nameToT9(name: String): String {
        return name.uppercase().map { char ->
            when (char) {
                'A', 'B', 'C' -> '2'
                'D', 'E', 'F' -> '3'
                'G', 'H', 'I' -> '4'
                'J', 'K', 'L' -> '5'
                'M', 'N', 'O' -> '6'
                'P', 'Q', 'R', 'S' -> '7'
                'T', 'U', 'V' -> '8'
                'W', 'X', 'Y', 'Z' -> '9'
                else -> null
            }
        }.filterNotNull().joinToString("")
    }

    @SuppressLint("MissingPermission")
    private fun clearMissedCallNotifications() {
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            telecomManager?.cancelMissedCallsNotification()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(UpperDotInCallService.MISSED_CALL_NOTIFICATION_ID)
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
            if (com.mail2dev.upperdot.util.ContactUtils.isSamePhoneNumber(log.number, lastNumber)) {
                currentGroup.add(log)
            } else {
                if (currentGroup.isNotEmpty()) {
                    val first = currentGroup.first()
                    result.add(GroupedCallLog(first.name, first.avatarPath, first.number, first.contactId, currentGroup.toList()))
                }
                currentGroup = mutableListOf(log)
                lastNumber = log.number
            }
        }
        if (currentGroup.isNotEmpty()) {
            val first = currentGroup.first()
            result.add(GroupedCallLog(first.name, first.avatarPath, first.number, first.contactId, currentGroup.toList()))
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

    @SuppressLint("MissingPermission")
    fun makeCall(phoneNumber: String) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val selectedHandle = _selectedSim.value?.let { info ->
            val accounts = telecomManager.callCapablePhoneAccounts
            accounts.find { it.id == info.iccId || it.id == info.subscriptionId.toString() }
        }
        TelephonyUtils.placeOutgoingCall(context, phoneNumber, selectedHandle)
    }
}
