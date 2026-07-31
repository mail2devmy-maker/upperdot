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

import com.mail2dev.upperdot.data.local.DatabaseBackup
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.local.entity.PreferenceEntity
import com.mail2dev.upperdot.data.repository.PreferenceRepository
import com.mail2dev.upperdot.util.ContactUtils
import com.mail2dev.upperdot.utils.BackupUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

data class DatabaseDiagnostics(
    val vaultSize: String = "0.00 MB",
    val totalAttachmentUsage: String = "0.0 MB",
    val totalContactsCount: Int = 0,
    val walletCardsCount: Int = 0
)

sealed class VcfImportState {
    object Idle : VcfImportState()
    data class Conflict(val conflicts: List<Pair<ContactEntity, ContactEntity>>, val nonConflicts: List<ContactEntity>) : VcfImportState()
    object Success : VcfImportState()
}

class AdvancedSettingsViewModel(
    private val contactRepository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
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
        bankCardRepository.cardCount
    ) { contactCount, cardCount ->
        DatabaseDiagnostics(
            totalContactsCount = contactCount,
            walletCardsCount = cardCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DatabaseDiagnostics())

    private val _showClearCacheDialog = MutableStateFlow(false)
    val showClearCacheDialog: StateFlow<Boolean> = _showClearCacheDialog.asStateFlow()

    private val _showFrequencyDialog = MutableStateFlow(false)
    val showFrequencyDialog: StateFlow<Boolean> = _showFrequencyDialog.asStateFlow()

    private val _showCurrencyDialog = MutableStateFlow(false)
    val showCurrencyDialog: StateFlow<Boolean> = _showCurrencyDialog.asStateFlow()

    private val _vcfImportState = MutableStateFlow<VcfImportState>(VcfImportState.Idle)
    val vcfImportState: StateFlow<VcfImportState> = _vcfImportState.asStateFlow()

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

    fun confirmClearCache() {
        // TODO: Delete temporary thumbnails
        _showClearCacheDialog.value = false
    }

    fun exportDatabase(filesDir: File, outputStream: OutputStream, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val backup = DatabaseBackup(
                contacts = contactRepository.allContacts.first(),
                notes = noteRepository.allNotes.first(),
                transactions = transactionRepository.allTransactions.first(),
                bankCards = bankCardRepository.allCards.first(),
                preferences = preferenceRepository.preferences.first()
            )
            val json = Json.encodeToString(backup)
            BackupUtils.createZipBackup(filesDir, json, outputStream)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun importDatabase(filesDir: File, inputStream: InputStream, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = BackupUtils.restoreZipBackup(filesDir, inputStream)
                if (json != null) {
                    val backup = Json.decodeFromString<DatabaseBackup>(json)
                    
                    // Clear existing
                    contactRepository.deleteAll()
                    noteRepository.deleteAll()
                    transactionRepository.deleteAll()
                    bankCardRepository.deleteAll()

                    // Restore
                    contactRepository.insertContacts(backup.contacts)
                    noteRepository.insertNotes(backup.notes)
                    transactionRepository.insertTransactions(backup.transactions)
                    bankCardRepository.insertCards(backup.bankCards)
                    backup.preferences?.let { preferenceRepository.savePreferences(it) }

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importVcf() {
        // Triggered from UI via picker
    }

    fun onVcfSelected(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            val text = inputStream.bufferedReader().use { it.readText() }
            val incomingContacts = parseVcf(text)

            val allExisting = contactRepository.allContacts.first()
            val conflicts = mutableListOf<Pair<ContactEntity, ContactEntity>>()
            val nonConflicts = mutableListOf<ContactEntity>()

            for (incoming in incomingContacts) {
                val existing = allExisting.find { ex ->
                    incoming.phoneNumbers.any { inPh ->
                        ex.phoneNumbers.any { exPh ->
                            ContactUtils.isSamePhoneNumber(inPh, exPh)
                        }
                    }
                }
                if (existing != null) {
                    conflicts.add(existing to incoming)
                } else {
                    nonConflicts.add(incoming)
                }
            }

            if (conflicts.isNotEmpty()) {
                _vcfImportState.value = VcfImportState.Conflict(conflicts, nonConflicts)
            } else {
                for (contact in nonConflicts) {
                    contactRepository.insertContact(contact)
                }
                _vcfImportState.value = VcfImportState.Success
            }
        }
    }

    fun resolveVcfConflicts(strategy: String) {
        val currentState = _vcfImportState.value
        if (currentState !is VcfImportState.Conflict) return

        viewModelScope.launch(Dispatchers.IO) {
            // Always insert non-conflicts
            for (contact in currentState.nonConflicts) {
                contactRepository.insertContact(contact)
            }

            when (strategy) {
                "OVERWRITE" -> {
                    for ((existing, incoming) in currentState.conflicts) {
                        val updated = incoming.copy(id = existing.id) // Preserve ID to keep linked records
                        contactRepository.updateContact(updated)
                    }
                }
                "DUPLICATE" -> {
                    for ((_, incoming) in currentState.conflicts) {
                        contactRepository.insertContact(incoming)
                    }
                }
                "SKIP" -> {
                    // Do nothing for conflicts
                }
            }
            _vcfImportState.value = VcfImportState.Success
        }
    }

    fun dismissVcfDialog() {
        _vcfImportState.value = VcfImportState.Idle
    }

    private fun parseVcf(text: String): List<ContactEntity> {
        val contacts = mutableListOf<ContactEntity>()
        val vcards = text.split("BEGIN:VCARD")
        for (vcard in vcards) {
            if (vcard.isBlank() || !vcard.contains("END:VCARD")) continue
            var name = ""
            val phones = mutableListOf<String>()
            val emails = mutableListOf<String>()

            vcard.lines().forEach { line ->
                when {
                    line.startsWith("FN:") || line.startsWith("FN;") -> {
                        name = line.substringAfter(":").trim()
                    }
                    line.startsWith("TEL") -> {
                        phones.add(line.substringAfter(":").trim())
                    }
                    line.startsWith("EMAIL") -> {
                        emails.add(line.substringAfter(":").trim())
                    }
                }
            }

            if (name.isNotEmpty()) {
                contacts.add(
                    ContactEntity(
                        id = 0L,
                        fullName = name,
                        nicknames = emptyList(),
                        phoneNumbers = phones,
                        sanitizedPrimaryPhone = phones.firstOrNull()?.let { ContactUtils.smartSanitize(it) } ?: "",
                        emails = emails,
                        socialProfiles = emptyList(),
                        bankAccounts = emptyList()
                    )
                )
            }
        }
        return contacts
    }
}
