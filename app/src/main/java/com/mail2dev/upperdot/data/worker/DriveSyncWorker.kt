package com.mail2dev.upperdot.data.worker

import android.content.Context
import androidx.work.*
import com.mail2dev.upperdot.UpperDotApp
import com.mail2dev.upperdot.data.local.DatabaseBackup
import com.mail2dev.upperdot.utils.BackupUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import androidx.room.withTransaction
import kotlinx.serialization.encodeToString
import java.util.*

class DriveSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    companion object {
        const val EXTRA_SYNC_ACTION = "sync_action"
        const val ACTION_RESTORE = "RESTORE"
        const val ACTION_BACKUP = "BACKUP"
        const val ACTION_AUTO = "AUTO"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as UpperDotApp
        val driveService = app.googleDriveService
        val action = inputData.getString(EXTRA_SYNC_ACTION) ?: ACTION_AUTO
        
        try {
            val cloudFile = driveService.getBackupFile()
            
            val localContactsCount = app.contactRepository.contactCount.first()
            val localNotesCount = app.noteRepository.noteCount.first()
            val localTransactionsCount = app.transactionRepository.transactionCount.first()
            val totalLocalRecords = localContactsCount + localNotesCount + localTransactionsCount

            when (action) {
                ACTION_RESTORE -> {
                    if (cloudFile != null) {
                        downloadAndRestore(app, cloudFile.id)
                    } else {
                        return@withContext Result.failure()
                    }
                }
                ACTION_BACKUP -> {
                    uploadLocalState(app)
                }
                else -> {
                    // AUTO Logic (Existing Two-Way heuristic)
                    if (cloudFile != null && totalLocalRecords == 0) {
                        downloadAndRestore(app, cloudFile.id)
                    } else if (cloudFile == null || totalLocalRecords > 0) {
                        uploadLocalState(app)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun downloadAndRestore(app: UpperDotApp, fileId: String) {
        val tempFile = File(app.cacheDir, "temp_backup.zip")
        try {
            setProgress(workDataOf("progress_msg" to "⏳ Downloading cloud vault..."))
            app.googleDriveService.downloadFile(fileId, tempFile.outputStream())
            
            setProgress(workDataOf("progress_msg" to "🗜️ Extracting media files..."))
            tempFile.inputStream().use { input ->
                val jsonStr = BackupUtils.restoreZipBackup(app.filesDir, input)
                if (jsonStr != null) {
                    val backup = jsonSerializer.decodeFromString<DatabaseBackup>(jsonStr)
                    
                    app.database.withTransaction {
                        // Clear existing
                        app.contactRepository.deleteAll()
                        app.noteRepository.deleteAll()
                        app.transactionRepository.deleteAll()
                        app.bankCardRepository.deleteAll()

                        // Restore Contacts (Preserves PKs)
                        app.contactRepository.insertContacts(backup.contacts)
                        
                        // Create a set of valid contact IDs for orphan guarding
                        val contactIds = backup.contacts.map { contact -> contact.id }.toSet()

                        // Restore Notes (Filtering orphans to prevent FK failures)
                        val validNotes = backup.notes.filter { note -> contactIds.contains(note.contactId) }
                        app.noteRepository.insertNotes(validNotes)

                        // Restore Transactions (Filtering orphans to prevent FK failures)
                        val validTransactions = backup.transactions.filter { trans -> contactIds.contains(trans.contactId) }
                        app.transactionRepository.insertTransactions(validTransactions)

                        app.bankCardRepository.insertCards(backup.bankCards)
                    }
                    
                    // Update last sync time
                    val prefs = app.preferenceRepository.preferences.first()
                    app.preferenceRepository.savePreferences(prefs.copy(lastSyncTime = System.currentTimeMillis()))
                }
            }
        } finally {
            tempFile.delete()
        }
    }

    private suspend fun uploadLocalState(app: UpperDotApp) {
        setProgress(workDataOf("progress_msg" to "📦 Packaging local workspace..."))
        val backup = DatabaseBackup(
            contacts = app.contactRepository.allContacts.first(),
            notes = app.noteRepository.allNotes.first(),
            transactions = app.transactionRepository.allTransactions.first(),
            bankCards = app.bankCardRepository.allCards.first(),
            preferences = app.preferenceRepository.preferences.first()
        )
        val jsonStr = jsonSerializer.encodeToString(backup)
        val tempFile = File(app.cacheDir, "upload_backup.zip")
        try {
            tempFile.outputStream().use { 
                BackupUtils.createZipBackup(app.filesDir, jsonStr, it)
            }
            
            setProgress(workDataOf("progress_msg" to "⬆️ Uploading to Google Drive..."))
            val cloudFile = app.googleDriveService.getBackupFile()
            app.googleDriveService.uploadFile(tempFile, cloudFile?.id)
            
            // Update last sync time
            val prefs = app.preferenceRepository.preferences.first()
            app.preferenceRepository.savePreferences(prefs.copy(lastSyncTime = System.currentTimeMillis()))
        } finally {
            tempFile.delete()
        }
    }
}
