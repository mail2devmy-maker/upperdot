package com.mail2dev.upperdot.ui.app_settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.PreferenceEntity
import com.mail2dev.upperdot.data.repository.*
import com.mail2dev.upperdot.data.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

data class DatabaseDiagnostics(
    val vaultSize: String = "0.00 MB",
    val totalAttachmentUsage: String = "0.00 MB",
    val totalContactsCount: Int = 0,
    val walletCardsCount: Int = 0
)

sealed class SettingsUiEvent {
    object Loading : SettingsUiEvent()
    data class Success(val message: String) : SettingsUiEvent()
    data class Error(val message: String) : SettingsUiEvent()
}

class AdvancedSettingsViewModel(
    private val contactRepository: ContactRepository,
    private val bankCardRepository: BankCardRepository,
    private val syncManager: SyncManager,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _syncOverWifi = MutableStateFlow(true)
    val syncOverWifi: StateFlow<Boolean> = _syncOverWifi.asStateFlow()

    private val _syncFrequency = MutableStateFlow("1h")
    val syncFrequency: StateFlow<String> = _syncFrequency.asStateFlow()

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _vaultSize = MutableStateFlow("0.00 MB")
    private val _attachmentUsage = MutableStateFlow("0.00 MB")

    init {
        viewModelScope.launch {
            preferenceRepository.preferences.collectLatest { prefs ->
                _syncOverWifi.value = prefs.syncOverWifi
                _syncFrequency.value = prefs.syncFrequency
                _currencySymbol.value = prefs.currencySymbol
            }
        }
    }

    val diagnostics: StateFlow<DatabaseDiagnostics> = combine(
        contactRepository.contactCount,
        bankCardRepository.cardCount,
        _vaultSize,
        _attachmentUsage
    ) { contactCount, cardCount, vaultSize, attachmentUsage ->
        DatabaseDiagnostics(
            totalContactsCount = contactCount,
            walletCardsCount = cardCount,
            vaultSize = vaultSize,
            totalAttachmentUsage = attachmentUsage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DatabaseDiagnostics())

    private val _showClearCacheDialog = MutableStateFlow(false)
    val showClearCacheDialog: StateFlow<Boolean> = _showClearCacheDialog.asStateFlow()

    private val _showFrequencyDialog = MutableStateFlow(false)
    val showFrequencyDialog: StateFlow<Boolean> = _showFrequencyDialog.asStateFlow()

    private val _showCurrencyDialog = MutableStateFlow(false)
    val showCurrencyDialog: StateFlow<Boolean> = _showCurrencyDialog.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SettingsUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun updateStorageDiagnostics(filesDir: File, dbFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Calculate Vault Size (DB)
                val dbSize = if (dbFile.exists()) dbFile.length() else 0L
                val vaultSizeMB = String.format(Locale.getDefault(), "%.2f MB", dbSize.toDouble() / (1024 * 1024))

                // Calculate Total Attachment Usage (Scan filesDir tree)
                var totalBytes = 0L
                if (filesDir.exists()) {
                    filesDir.walkTopDown().forEach { file ->
                        if (file.isFile) {
                            totalBytes += file.length()
                        }
                    }
                }
                val attachmentMB = String.format(Locale.getDefault(), "%.2f MB", totalBytes.toDouble() / (1024 * 1024))

                _vaultSize.value = vaultSizeMB
                _attachmentUsage.value = attachmentMB
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleSyncOverWifi(enabled: Boolean) {
        _syncOverWifi.value = enabled
        savePreferences()
        updateSyncSchedule()
    }

    fun onSyncFrequencySelected(frequency: String) {
        _syncFrequency.value = frequency
        _showFrequencyDialog.value = false
        savePreferences()
        updateSyncSchedule()
    }

    private fun savePreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = PreferenceEntity(
                syncOverWifi = _syncOverWifi.value,
                syncFrequency = _syncFrequency.value,
                currencySymbol = _currencySymbol.value
            )
            preferenceRepository.savePreferences(prefs)
        }
    }

    private fun updateSyncSchedule() {
        if (_syncFrequency.value == "Manual") {
            syncManager.cancelPeriodicSync()
            return
        }
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
        _showCurrencyDialog.value = false
        savePreferences()
    }

    fun requestClearCache() {
        _showClearCacheDialog.value = true
    }

    fun dismissClearCacheDialog() {
        _showClearCacheDialog.value = false
    }

    fun requestFrequencyChange() {
        _showFrequencyDialog.value = true
    }

    fun dismissFrequencyDialog() {
        _showFrequencyDialog.value = false
    }

    fun requestCurrencyChange() {
        _showCurrencyDialog.value = true
    }

    fun dismissCurrencyDialog() {
        _showCurrencyDialog.value = false
    }

    fun confirmClearCache(filesDir: File, dbFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            updateStorageDiagnostics(filesDir, dbFile)
            withContext(Dispatchers.Main) {
                _showClearCacheDialog.value = false
            }
        }
    }
}
