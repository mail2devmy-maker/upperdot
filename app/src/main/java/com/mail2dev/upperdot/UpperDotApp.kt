package com.mail2dev.upperdot

import android.app.Application
import androidx.room.Room
import com.mail2dev.upperdot.data.local.AppDatabase
import com.mail2dev.upperdot.data.repository.ContactRepository

class UpperDotApp : Application() {
    
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "upperdot_db"
        ).build()
    }

    val contactRepository: ContactRepository by lazy {
        ContactRepository(database.contactDao())
    }

    val hierarchyRepository: com.mail2dev.upperdot.data.repository.HierarchyRepository by lazy {
        com.mail2dev.upperdot.data.repository.HierarchyRepository()
    }

    val noteRepository: com.mail2dev.upperdot.data.repository.NoteRepository by lazy {
        com.mail2dev.upperdot.data.repository.NoteRepository(database.noteDao())
    }

    val transactionRepository: com.mail2dev.upperdot.data.repository.TransactionRepository by lazy {
        com.mail2dev.upperdot.data.repository.TransactionRepository(database.transactionDao())
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
}
