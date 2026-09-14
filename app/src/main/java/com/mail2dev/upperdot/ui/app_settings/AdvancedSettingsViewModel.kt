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
    val walletCardsCount: Int = 0,
    val lastSyncTime: String = "Never"
)

sealed class SettingsUiEvent {
    object Loading : SettingsUiEvent()
    data class Success(val message: String) : SettingsUiEvent()
    data class Error(val message: String) : SettingsUiEvent()
}

class AdvancedSettingsViewModel(
    private val contactRepository: ContactRepository,
    private val bankCardRepository: BankCardRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    private val syncManager: SyncManager,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _syncOverWifi = MutableStateFlow(false)
    val syncOverWifi: StateFlow<Boolean> = _syncOverWifi.asStateFlow()

    private val _isMediaCompressionEnabled = MutableStateFlow(true)
    val isMediaCompressionEnabled: StateFlow<Boolean> = _isMediaCompressionEnabled.asStateFlow()

    private val _syncFrequency = MutableStateFlow("1h")
    val syncFrequency: StateFlow<String> = _syncFrequency.asStateFlow()

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)

    private val _blockUnknownNumbers = MutableStateFlow(false)
    val blockUnknownNumbers: StateFlow<Boolean> = _blockUnknownNumbers.asStateFlow()

    private val _strictPrivacyMode = MutableStateFlow(false)
    val strictPrivacyMode: StateFlow<Boolean> = _strictPrivacyMode.asStateFlow()

    private val _vaultSize = MutableStateFlow("0.00 MB")
    private val _attachmentUsage = MutableStateFlow("0.00 MB")

    private val _showClearCacheDialog = MutableStateFlow(false)
    val showClearCacheDialog: StateFlow<Boolean> = _showClearCacheDialog.asStateFlow()

    private val _showFrequencyDialog = MutableStateFlow(false)
    val showFrequencyDialog: StateFlow<Boolean> = _showFrequencyDialog.asStateFlow()

    private val _showCurrencyDialog = MutableStateFlow(false)
    val showCurrencyDialog: StateFlow<Boolean> = _showCurrencyDialog.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SettingsUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            preferenceRepository.preferences.collectLatest { prefs ->
                _syncOverWifi.value = prefs.syncOverWifi
                _syncFrequency.value = prefs.syncFrequency
                _currencySymbol.value = prefs.currencySymbol
                _isMediaCompressionEnabled.value = prefs.isMediaCompressionEnabled
                _blockUnknownNumbers.value = prefs.blockUnknownNumbers
                _strictPrivacyMode.value = prefs.strictPrivacyMode
                _lastSyncTime.value = prefs.lastSyncTime
            }
        }
    }

    val diagnostics: StateFlow<DatabaseDiagnostics> = combine(
        contactRepository.contactCount,
        bankCardRepository.cardCount,
        _vaultSize,
        _attachmentUsage,
        _lastSyncTime
    ) { contactCount, cardCount, vaultSize, attachmentUsage, lastSync ->
        val syncStr = if (lastSync == 0L) "Never" else {
            java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(lastSync))
        }
        DatabaseDiagnostics(
            totalContactsCount = contactCount,
            walletCardsCount = cardCount,
            vaultSize = vaultSize,
            totalAttachmentUsage = attachmentUsage,
            lastSyncTime = syncStr
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DatabaseDiagnostics())

    // Currency Dialog Controls
    fun showCurrencyDialog() {
        _showCurrencyDialog.value = true
    }

    fun dismissCurrencyDialog() {
        _showCurrencyDialog.value = false
    }

    fun updateCurrencySymbol(symbol: String) {
        val formattedSymbol = symbol.trim()
        if (formattedSymbol.isNotEmpty()) {
            _currencySymbol.value = formattedSymbol
            dismissCurrencyDialog()
            savePreferences()
        }
    }

    // Sync Frequency Dialog Controls
    fun showSyncFrequencyDialog() {
        _showFrequencyDialog.value = true
    }

    fun dismissSyncFrequencyDialog() {
        _showFrequencyDialog.value = false
    }

    fun updateSyncFrequency(frequency: String) {
        _syncFrequency.value = frequency
        dismissSyncFrequencyDialog()
        savePreferences()
        updateSyncSchedule()
    }

    fun updateStorageDiagnostics(filesDir: File, dbFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dbSize = if (dbFile.exists()) dbFile.length() else 0L
                val vaultSizeMB = String.format(Locale.getDefault(), "%.2f MB", dbSize.toDouble() / (1024 * 1024))

                val activeNotes = noteRepository.allNotes.firstOrNull() ?: emptyList()
                val activeTransactions = transactionRepository.allTransactions.firstOrNull() ?: emptyList()
                val activeCards = bankCardRepository.allCards.firstOrNull() ?: emptyList()
                val activeContacts = contactRepository.allContacts.firstOrNull() ?: emptyList()

                val activePaths = mutableSetOf<String>()

                activeNotes.forEach { note ->
                    activePaths.addAll(note.attachmentPaths)
                    note.voiceRecordingPath?.let { activePaths.add(it) }
                }
                activeTransactions.forEach { tx ->
                    activePaths.addAll(tx.receiptPaths)
                    tx.voiceRecordingPath?.let { activePaths.add(it) }
                }
                activeCards.forEach { card ->
                    card.qrImagePath?.let { activePaths.add(it) }
                }
                activeContacts.forEach { contact ->
                    contact.avatarPath?.let { activePaths.add(it) }
                }

                var totalBytes = 0L
                activePaths.forEach { path ->
                    if (path.isNotBlank()) {
                        val file = File(path)
                        if (file.exists() && file.isFile) {
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

    fun toggleMediaCompression(enabled: Boolean) {
        _isMediaCompressionEnabled.value = enabled
        savePreferences()
    }

    fun toggleBlockUnknownNumbers(enabled: Boolean) {
        _blockUnknownNumbers.value = enabled
        savePreferences()
    }

    fun toggleStrictPrivacyMode(enabled: Boolean) {
        _strictPrivacyMode.value = enabled
        savePreferences()
    }

    private fun savePreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = PreferenceEntity(
                syncOverWifi = _syncOverWifi.value,
                syncFrequency = _syncFrequency.value,
                currencySymbol = _currencySymbol.value,
                isMediaCompressionEnabled = _isMediaCompressionEnabled.value,
                blockUnknownNumbers = _blockUnknownNumbers.value,
                strictPrivacyMode = _strictPrivacyMode.value
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

    fun requestClearCache() {
        _showClearCacheDialog.value = true
    }

    fun dismissClearCacheDialog() {
        _showClearCacheDialog.value = false
    }

    fun confirmClearCache(filesDir: File, dbFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val activeNotes = noteRepository.allNotes.firstOrNull() ?: emptyList()
                val activeTransactions = transactionRepository.allTransactions.firstOrNull() ?: emptyList()
                val activeCards = bankCardRepository.allCards.firstOrNull() ?: emptyList()
                val activeContacts = contactRepository.allContacts.firstOrNull() ?: emptyList()

                val activePaths = mutableSetOf<String>()

                activeNotes.forEach { note ->
                    activePaths.addAll(note.attachmentPaths)
                    note.voiceRecordingPath?.let { activePaths.add(it) }
                }
                activeTransactions.forEach { tx ->
                    activePaths.addAll(tx.receiptPaths)
                    tx.voiceRecordingPath?.let { activePaths.add(it) }
                }
                activeCards.forEach { card ->
                    card.qrImagePath?.let { activePaths.add(it) }
                }
                activeContacts.forEach { contact ->
                    contact.avatarPath?.let { activePaths.add(it) }
                }

                if (filesDir.exists()) {
                    filesDir.walkTopDown().forEach { file ->
                        if (file.isFile && !activePaths.contains(file.absolutePath)) {
                            file.delete()
                        }
                    }
                }

                updateStorageDiagnostics(filesDir, dbFile)

                withContext(Dispatchers.Main) {
                    _showClearCacheDialog.value = false
                    _eventFlow.emit(SettingsUiEvent.Success("Cache cleared successfully"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _showClearCacheDialog.value = false
                    _eventFlow.emit(SettingsUiEvent.Error("Failed to clear cache"))
                }
            }
        }
    }
}