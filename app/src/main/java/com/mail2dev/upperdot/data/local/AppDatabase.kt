package com.mail2dev.upperdot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mail2dev.upperdot.data.local.converter.ComplexTypeConverters
import com.mail2dev.upperdot.data.local.converter.ListConverter
import com.mail2dev.upperdot.data.local.dao.BankCardDao
import com.mail2dev.upperdot.data.local.dao.ContactDao
import com.mail2dev.upperdot.data.local.dao.NoteDao
import com.mail2dev.upperdot.data.local.dao.PreferenceDao
import com.mail2dev.upperdot.data.local.dao.TransactionDao
import com.mail2dev.upperdot.data.local.entity.BankCardEntity
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.PreferenceEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity

@Database(
    entities = [
        ContactEntity::class,
        NoteEntity::class,
        TransactionEntity::class,
        BankCardEntity::class,
        PreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListConverter::class, ComplexTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun noteDao(): NoteDao
    abstract fun transactionDao(): TransactionDao
    abstract fun bankCardDao(): BankCardDao
    abstract fun preferenceDao(): PreferenceDao
}
