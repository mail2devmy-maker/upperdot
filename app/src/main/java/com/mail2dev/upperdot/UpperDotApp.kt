package com.mail2dev.upperdot

import android.app.Application
import androidx.room.Room
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.mail2dev.upperdot.data.local.AppDatabase
import com.mail2dev.upperdot.data.repository.BankSuggestionRepository
import com.mail2dev.upperdot.data.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository

class UpperDotApp : Application(), ImageLoaderFactory {

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of the app's available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Use 2% of the disk's available space
                    .build()
            }
            .crossfade(true) // Smooth image transitions
            .respectCacheHeaders(false) // Cache images even if headers say otherwise (good for local files)
            .build()
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "upperdot_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val contactRepository: ContactRepository by lazy {
        ContactRepository(database.contactDao())
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }


    val bankSuggestionRepository: BankSuggestionRepository by lazy {
        BankSuggestionRepository(database.savedBankDao())
    }

    override fun onCreate() {
        super.onCreate()
        initializeSync()
    }

    private fun initializeSync() {
        applicationScope.launch {
            val prefs = preferenceRepository.preferences.first()
            if (prefs.syncFrequency != "Manual") {
                val interval = when (prefs.syncFrequency) {
                    "1h" -> 1L
                    "6h" -> 6L
                    "12h" -> 12L
                    "24h" -> 24L
                    else -> 1L
                }
                syncManager.schedulePeriodicSync(interval, prefs.syncOverWifi)
            }
        }
    }

    val hierarchyRepository: com.mail2dev.upperdot.data.repository.HierarchyRepository by lazy {
        com.mail2dev.upperdot.data.repository.HierarchyRepository()
    }

    val bankCardRepository: com.mail2dev.upperdot.data.repository.BankCardRepository by lazy {
        com.mail2dev.upperdot.data.repository.BankCardRepository(database.bankCardDao())
    }

    val preferenceRepository: com.mail2dev.upperdot.data.repository.PreferenceRepository by lazy {
        com.mail2dev.upperdot.data.repository.PreferenceRepository(database.preferenceDao())
    }

    val googleAuthService: com.mail2dev.upperdot.data.network.GoogleAuthService by lazy {
        com.mail2dev.upperdot.data.network.GoogleAuthService(this)
    }

    val googleDriveService: com.mail2dev.upperdot.data.network.GoogleDriveService by lazy {
        com.mail2dev.upperdot.data.network.GoogleDriveService(this)
    }

    val syncManager: com.mail2dev.upperdot.data.sync.SyncManager by lazy {
        com.mail2dev.upperdot.data.sync.SyncManager(this)
    }

    val callLogRepository: com.mail2dev.upperdot.data.repository.telephony.CallLogRepository by lazy {
        com.mail2dev.upperdot.data.repository.telephony.CallLogRepository(this, contactRepository)
    }

    val audioHandler: com.mail2dev.upperdot.util.AudioHandler by lazy {
        com.mail2dev.upperdot.util.AudioHandlerImpl(this)
    }
}