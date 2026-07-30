package com.mail2dev.upperdot.ui.app_settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.mail2dev.upperdot.data.sync.SyncManager
import com.mail2dev.upperdot.data.repository.BankCardRepository
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

data class DatabaseDiagnostics(
    val vaultSize: String = "0.00 MB",
    val totalAttachmentUsage: String = "0.0 MB",
    val totalContactsCount: Int = 0,
    val walletCardsCount: Int = 0
)

class AdvancedSettingsViewModel(
    private val contactRepository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    private val bankCardRepository: BankCardRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _syncOverWifi = MutableStateFlow(true)
    val syncOverWifi: StateFlow<Boolean> = _syncOverWifi.asStateFlow()

    private val _syncFrequency = MutableStateFlow("1h")
    val syncFrequency: StateFlow<String> = _syncFrequency.asStateFlow()

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    val diagnostics: StateFlow<DatabaseDiagnostics> = combine(
        contactRepository.contactCount,
        bankCardRepository.cardCount
    ) { contactCount, cardCount ->
        DatabaseDiagnostics(
            totalContactsCount = contactCount,
            walletCardsCount = cardCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DatabaseDiagnostics())

    private val _showClearCacheDialog = MutableStateFlow(false)
    val showClearCacheDialog: StateFlow<Boolean> = _showClearCacheDialog.asStateFlow()

    fun toggleSyncOverWifi(enabled: Boolean) {
        _syncOverWifi.value = enabled
        updateSyncSchedule()
    }

    fun onSyncFrequencySelected(frequency: String) {
        _syncFrequency.value = frequency
        updateSyncSchedule()
    }

    private fun updateSyncSchedule() {
        val interval = when (_syncFrequency.value) {
            "1h" -> 1L
            "6h" -> 6L
            "12h" -> 12L
            "24h" -> 24L
            else -> return
        }
        syncManager.schedulePeriodicSync(interval, _syncOverWifi.value)
    }

    fun onCurrencySelected(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun requestClearCache() {
        _showClearCacheDialog.value = true
    }

    fun dismissClearCacheDialog() {
        _showClearCacheDialog.value = false
    }

    fun confirmClearCache() {
        // TODO: Delete temporary thumbnails
        _showClearCacheDialog.value = false
    }

    fun exportDatabase() {
        // TODO: Serialized JSON snapshot
    }

    fun importDatabase() {
        // TODO: Overwrite local data
    }

    fun importVcf() {
        // TODO: Load .vcf files
    }
}
