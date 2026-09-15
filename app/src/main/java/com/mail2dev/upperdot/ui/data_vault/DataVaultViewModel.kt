package com.mail2dev.upperdot.ui.data_vault

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.mail2dev.upperdot.data.local.DatabaseBackup
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.network.GoogleDriveService
import com.mail2dev.upperdot.data.repository.BankCardRepository
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.PreferenceRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import com.mail2dev.upperdot.data.worker.DriveSyncWorker
import com.mail2dev.upperdot.ui.add_contact.SocialProfile
import com.mail2dev.upperdot.util.ContactUtils
import com.mail2dev.upperdot.utils.BackupUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

data class CloudBackupMetadata(
    val lastModified: String = "No Backup Found",
    val size: String = "0 KB",
    val exists: Boolean = false
)

sealed class VcfImportState {
    object Idle : VcfImportState()
    data class Conflict(val conflicts: List<Pair<ContactEntity, ContactEntity>>, val nonConflicts: List<ContactEntity>) : VcfImportState()
    object Success : VcfImportState()
}

class DataVaultViewModel(
    private val contactRepository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    private val bankCardRepository: BankCardRepository,
    private val preferenceRepository: PreferenceRepository,
    private val driveService: GoogleDriveService,
    context: Context
) : ViewModel() {

    private val _cloudMetadata = MutableStateFlow(CloudBackupMetadata())
    val cloudMetadata: StateFlow<CloudBackupMetadata> = _cloudMetadata.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _vcfImportState = MutableStateFlow<VcfImportState>(VcfImportState.Idle)
    val vcfImportState: StateFlow<VcfImportState> = _vcfImportState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val workManager = WorkManager.getInstance(context)

    init {
        fetchCloudMetadata()
    }

    fun fetchCloudMetadata() {
        viewModelScope.launch {
            try {
                val file = driveService.getBackupFile()
                if (file != null) {
                    val date = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
                        .format(Date(file.modifiedTime.value))
                    val sizeKB = "${file.size / 1024} KB"
                    _cloudMetadata.value = CloudBackupMetadata(date, sizeKB, true)
                } else {
                    _cloudMetadata.value = CloudBackupMetadata(exists = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onRestoreClicked() {
        triggerSyncAction(DriveSyncWorker.ACTION_RESTORE)
    }

    fun onBackupClicked() {
        triggerSyncAction(DriveSyncWorker.ACTION_BACKUP)
    }

    private fun triggerSyncAction(action: String) {
        if (_isSyncing.value) return

        val syncRequest = OneTimeWorkRequestBuilder<DriveSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(workDataOf(DriveSyncWorker.EXTRA_SYNC_ACTION to action))
            .build()
        
        workManager.enqueueUniqueWork("manual_sync", ExistingWorkPolicy.REPLACE, syncRequest)

        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(syncRequest.id).collect { workInfo ->
                if (workInfo != null) {
                    val progressMsg = workInfo.progress.getString("progress_msg")
                    if (progressMsg != null) {
                        _eventFlow.emit(progressMsg)
                    }

                    when (workInfo.state) {
                        WorkInfo.State.RUNNING -> _isSyncing.value = true
                        WorkInfo.State.SUCCEEDED -> {
                            _isSyncing.value = false
                            val now = System.currentTimeMillis()
                            updateLastSyncTime(now)
                            _eventFlow.emit("✓ Sync complete! Backup secured.")
                            fetchCloudMetadata() // Refresh metadata
                        }
                        WorkInfo.State.FAILED -> {
                            _isSyncing.value = false
                            _eventFlow.emit("✕ Sync failed. Please try again.")
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun updateLastSyncTime(timestamp: Long) {
        viewModelScope.launch {
            val currentPrefs = preferenceRepository.preferences.first()
            preferenceRepository.savePreferences(currentPrefs.copy(lastSyncTime = timestamp))
        }
    }

    suspend fun exportDatabase(filesDir: File, outputStream: OutputStream) {
        _eventFlow.emit("⏳ Generating database backup zip...")
        withContext(Dispatchers.IO) {
            try {
                val backup = DatabaseBackup(
                    contacts = contactRepository.allContacts.first(),
                    notes = noteRepository.allNotes.first(),
                    transactions = transactionRepository.allTransactions.first(),
                    bankCards = bankCardRepository.allCards.first(),
                    preferences = preferenceRepository.preferences.first()
                )
                val json = Json.encodeToString(backup)
                BackupUtils.createZipBackup(filesDir, json, outputStream)
                _eventFlow.emit("✓ Database backup zip generated!")
            } catch (e: Exception) {
                e.printStackTrace()
                _eventFlow.emit("✕ Database backup failed")
            }
        }
    }

    suspend fun importDatabase(filesDir: File, inputStream: InputStream) {
        _eventFlow.emit("⏳ Restoring workspace from ZIP...")
        withContext(Dispatchers.IO) {
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
                    
                    _eventFlow.emit("✓ Complete workspace restored successfully!")
                } else {
                    _eventFlow.emit("✕ Restore failed: Invalid backup file")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _eventFlow.emit("✕ Restore failed")
            }
        }
    }

    fun onVcfSelected(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val text = inputStream.bufferedReader().use { it.readText() }
                val incomingContacts = parseVcf(text)

                if (incomingContacts.isEmpty()) {
                    _eventFlow.emit("✕ No valid contacts found in VCF")
                    return@launch
                }

                val allExisting = contactRepository.allContacts.first()
                val conflicts = mutableListOf<Pair<ContactEntity, ContactEntity>>()
                val nonConflicts = mutableListOf<ContactEntity>()

                for (incoming in incomingContacts) {
                    val existing = allExisting.find { ex ->
                        // 1. Phone Match
                        val phoneMatch = incoming.phoneNumbers.any { inPh ->
                            ex.phoneNumbers.any { exPh ->
                                ContactUtils.isSamePhoneNumber(inPh, exPh)
                            }
                        }
                        if (phoneMatch) return@find true

                        // 2. Email Match (Exact, case-insensitive)
                        val emailMatch = incoming.emails.any { inEm ->
                            ex.emails.any { exEm ->
                                inEm.equals(exEm, ignoreCase = true)
                            }
                        }
                        if (emailMatch) return@find true

                        // 3. Exact Name Match (case-insensitive)
                        ex.fullName.equals(incoming.fullName, ignoreCase = true)
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
                    _eventFlow.emit("✓ VCF Contacts imported successfully!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _eventFlow.emit("✕ VCF Import failed")
            }
        }
    }

    fun resolveVcfConflicts(strategy: String) {
        val currentState = _vcfImportState.value
        if (currentState !is VcfImportState.Conflict) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                for (contact in currentState.nonConflicts) {
                    contactRepository.insertContact(contact)
                }

                when (strategy) {
                    "OVERWRITE" -> {
                        for ((existing, incoming) in currentState.conflicts) {
                            val updated = incoming.copy(
                                id = existing.id,
                                isWhitelisted = existing.isWhitelisted,
                                avatarPath = existing.avatarPath,
                                createdAt = existing.createdAt,
                                lastModifiedAt = System.currentTimeMillis(),
                                isSynced = false // Trigger re-sync
                            )
                            contactRepository.updateContact(updated)
                        }
                    }
                    "DUPLICATE" -> {
                        for ((_, incoming) in currentState.conflicts) {
                            contactRepository.insertContact(incoming)
                        }
                    }
                    "SKIP" -> {}
                }
                _vcfImportState.value = VcfImportState.Success
                _eventFlow.emit("✓ VCF Contacts imported successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
                _eventFlow.emit("✕ VCF Import resolution failed")
            }
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
            var remark: String? = null
            val socialProfiles = mutableListOf<SocialProfile>()

            val lines = vcard.lines()
            var i = 0
            while (i < lines.size) {
                var line = lines[i].trim()
                if (line.isEmpty()) { i++; continue }

                // Handle multi-line folded values (standard in VCF)
                while (i + 1 < lines.size && (lines[i+1].startsWith(" ") || lines[i+1].startsWith("\t"))) {
                    line += lines[++i].substring(1)
                }

                when {
                    line.startsWith("FN:", ignoreCase = true) || line.startsWith("FN;", ignoreCase = true) -> {
                        name = line.substringAfter(":").trim()
                    }
                    line.startsWith("TEL", ignoreCase = true) -> {
                        phones.add(line.substringAfter(":").trim())
                    }
                    line.startsWith("EMAIL", ignoreCase = true) -> {
                        emails.add(line.substringAfter(":").trim())
                    }
                    line.startsWith("NOTE", ignoreCase = true) -> {
                        val isQuotedPrintable = line.contains("ENCODING=QUOTED-PRINTABLE", ignoreCase = true)
                        var rawNote = line.substringAfter(":")
                        
                        // Handle multi-line Quoted-Printable notes (ending with =)
                        if (isQuotedPrintable) {
                            while (rawNote.endsWith("=") && i + 1 < lines.size) {
                                rawNote = rawNote.dropLast(1) + lines[++i].trim()
                            }
                            remark = decodeQuotedPrintable(rawNote)
                        } else {
                            remark = rawNote.trim()
                        }
                    }
                    line.startsWith("URL", ignoreCase = true) -> {
                        val url = line.substringAfter(":").trim()
                        if (url.isNotEmpty()) {
                            val platform = when {
                                url.contains("facebook.com", true) -> "Facebook"
                                url.contains("instagram.com", true) -> "Instagram"
                                url.contains("twitter.com", true) || url.contains("x.com", true) -> "X"
                                url.contains("tiktok.com", true) -> "TikTok"
                                url.contains("youtube.com", true) -> "YouTube"
                                url.contains("linkedin.com", true) -> "LinkedIn"
                                url.contains("wa.me", true) || url.contains("whatsapp.com", true) -> "WhatsApp"
                                url.contains("t.me", true) || url.contains("telegram.org", true) -> "Telegram"
                                else -> "Custom"
                            }
                            socialProfiles.add(SocialProfile(platform = platform, handle = url))
                        }
                    }
                }
                i++
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
                        remark = remark,
                        socialProfiles = socialProfiles,
                        bankAccounts = emptyList()
                    )
                )
            }
        }
        return contacts
    }

    private fun decodeQuotedPrintable(input: String): String {
        return try {
            val bytes = mutableListOf<Byte>()
            var j = 0
            while (j < input.length) {
                val c = input[j]
                if (c == '=' && j + 2 < input.length) {
                    val hex = input.substring(j + 1, j + 3)
                    bytes.add(hex.toInt(16).toByte())
                    j += 3
                } else {
                    bytes.add(c.code.toByte())
                    j++
                }
            }
            String(bytes.toByteArray(), Charsets.UTF_8)
        } catch (e: Exception) {
            input // Fallback to raw if decoding fails
        }
    }
}
