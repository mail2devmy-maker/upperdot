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

    val noteRepository: com.mail2dev.upperdot.data.repository.NoteRepository by lazy {
        com.mail2dev.upperdot.data.repository.NoteRepository(database.noteDao())
    }

    val transactionRepository: com.mail2dev.upperdot.data.repository.TransactionRepository by lazy {
        com.mail2dev.upperdot.data.repository.TransactionRepository(database.transactionDao())
    }

    val bankCardRepository: com.mail2dev.upperdot.data.repository.BankCardRepository by lazy {
        com.mail2dev.upperdot.data.repository.BankCardRepository(database.bankCardDao())
    }

    val googleAuthService: com.mail2dev.upperdot.data.network.GoogleAuthService by lazy {
        com.mail2dev.upperdot.data.network.GoogleAuthService(this)
    }

    val syncManager: com.mail2dev.upperdot.data.sync.SyncManager by lazy {
        com.mail2dev.upperdot.data.sync.SyncManager(this)
    }
}
