package com.mail2dev.upperdot.ui.profile_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.network.GoogleAuthService
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context

import com.mail2dev.upperdot.data.network.GoogleDriveService
import com.mail2dev.upperdot.data.repository.PreferenceRepository
import com.mail2dev.upperdot.data.worker.DriveSyncWorker
import androidx.work.*
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

data class CloudBackupMetadata(
    val lastModified: String = "No Backup Found",
    val size: String = "0 KB",
    val exists: Boolean = false
)

class ProfileSettingsViewModel(
    private val authService: GoogleAuthService,
    private val driveService: GoogleDriveService,
    private val contactRepository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    private val preferenceRepository: PreferenceRepository,
    context: Context
) : ViewModel() {

    private val _userSummary = MutableStateFlow(UserSummary())
    val userSummary: StateFlow<UserSummary> = _userSummary.asStateFlow()

    private val _cloudMetadata = MutableStateFlow(CloudBackupMetadata())
    val cloudMetadata: StateFlow<CloudBackupMetadata> = _cloudMetadata.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncEvent = MutableSharedFlow<String>()
    val syncEvent = _syncEvent.asSharedFlow()

    private val workManager = WorkManager.getInstance(context)
    private var syncWorkId: UUID? = null

    init {
        loadUserData(context)
        fetchCloudMetadata(context)
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

    fun fetchCloudMetadata(context: Context) {
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
        
        syncWorkId = syncRequest.id
        workManager.enqueueUniqueWork("manual_sync", ExistingWorkPolicy.REPLACE, syncRequest)

        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(syncRequest.id).collect { workInfo ->
                if (workInfo != null) {
                    val progressMsg = workInfo.progress.getString("progress_msg")
                    if (progressMsg != null) {
                        _syncEvent.emit(progressMsg)
                    }

                    when (workInfo.state) {
                        WorkInfo.State.RUNNING -> _isSyncing.value = true
                        WorkInfo.State.SUCCEEDED -> {
                            _isSyncing.value = false
                            val now = System.currentTimeMillis()
                            updateLastSyncTime(now)
                            _syncEvent.emit("✓ Sync complete! Backup secured.")
                        }
                        WorkInfo.State.FAILED -> {
                            _isSyncing.value = false
                            _syncEvent.emit("✕ Sync failed. Please try again.")
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun onRefreshSync() {
        triggerSyncAction(DriveSyncWorker.ACTION_AUTO)
    }

    private fun updateLastSyncTime(timestamp: Long) {
        viewModelScope.launch {
            val currentPrefs = preferenceRepository.preferences.first()
            preferenceRepository.savePreferences(currentPrefs.copy(lastSyncTime = timestamp))
        }
    }

    fun onSignOut(onSuccess: () -> Unit) {
        authService.signOut {
            onSuccess()
        }
    }
}
