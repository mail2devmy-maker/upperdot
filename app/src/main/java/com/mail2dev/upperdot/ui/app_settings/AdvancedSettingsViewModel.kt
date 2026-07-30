package com.mail2dev.upperdot.ui.app_settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DatabaseDiagnostics(
    val vaultSize: String = "0.00 MB",
    val totalAttachmentUsage: String = "0.0 MB",
    val totalContactsCount: Int = 1,
    val walletCardsCount: Int = 0
)

class AdvancedSettingsViewModel : ViewModel() {

    private val _syncOverWifi = MutableStateFlow(true)
    val syncOverWifi: StateFlow<Boolean> = _syncOverWifi.asStateFlow()

    private val _syncFrequency = MutableStateFlow("1h")
    val syncFrequency: StateFlow<String> = _syncFrequency.asStateFlow()

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _diagnostics = MutableStateFlow(DatabaseDiagnostics())
    val diagnostics: StateFlow<DatabaseDiagnostics> = _diagnostics.asStateFlow()

    private val _showClearCacheDialog = MutableStateFlow(false)
    val showClearCacheDialog: StateFlow<Boolean> = _showClearCacheDialog.asStateFlow()

    fun toggleSyncOverWifi(enabled: Boolean) {
        _syncOverWifi.value = enabled
    }

    fun onSyncFrequencySelected(frequency: String) {
        _syncFrequency.value = frequency
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
