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
}
