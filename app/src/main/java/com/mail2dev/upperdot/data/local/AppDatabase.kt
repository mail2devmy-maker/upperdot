package com.mail2dev.upperdot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mail2dev.upperdot.data.local.converter.ComplexTypeConverters
import com.mail2dev.upperdot.data.local.converter.ListConverter
import com.mail2dev.upperdot.data.local.dao.ContactDao
import com.mail2dev.upperdot.data.local.dao.NoteDao
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.local.entity.NoteEntity

@Database(
    entities = [
        ContactEntity::class,
        NoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListConverter::class, ComplexTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun noteDao(): NoteDao
}
