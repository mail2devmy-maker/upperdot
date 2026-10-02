package com.mail2dev.upperdot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mail2dev.upperdot.data.local.converter.ComplexTypeConverters
import com.mail2dev.upperdot.data.local.converter.ListConverter
import com.mail2dev.upperdot.data.local.dao.BankCardDao
import com.mail2dev.upperdot.data.local.dao.ContactDao
import com.mail2dev.upperdot.data.local.dao.GroupDao
import com.mail2dev.upperdot.data.local.dao.NoteDao
import com.mail2dev.upperdot.data.local.dao.PreferenceDao
import com.mail2dev.upperdot.data.local.dao.SavedBankDao
import com.mail2dev.upperdot.data.local.dao.TransactionDao
import com.mail2dev.upperdot.data.local.entity.BankCardEntity
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.local.entity.GroupEntity
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.PreferenceEntity
import com.mail2dev.upperdot.data.local.entity.SavedBankEntity
import com.mail2dev.upperdot.data.local.entity.TagEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity

@Database(
    entities = [
        ContactEntity::class,
        NoteEntity::class,
        TransactionEntity::class,
        BankCardEntity::class,
        PreferenceEntity::class,
        SavedBankEntity::class,
        GroupEntity::class,
        TagEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(ListConverter::class, ComplexTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun groupDao(): GroupDao
    abstract fun noteDao(): NoteDao
    abstract fun transactionDao(): TransactionDao
    abstract fun bankCardDao(): BankCardDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun savedBankDao(): SavedBankDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `contacts_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `nicknames` TEXT NOT NULL,
                        `phoneNumbers` TEXT NOT NULL,
                        `sanitizedPrimaryPhone` TEXT NOT NULL,
                        `emails` TEXT NOT NULL,
                        `avatarPath` TEXT,
                        `thumbnailPath` TEXT,
                        `groupId` TEXT,
                        `tagId` TEXT,
                        `socialProfiles` TEXT NOT NULL,
                        `companyName` TEXT,
                        `businessCategory` TEXT NOT NULL,
                        `physicalAddress` TEXT,
                        `bankAccounts` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `lastModifiedAt` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        `isWhitelisted` INTEGER NOT NULL,
                        `remark` TEXT
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO `contacts_new` (
                        `id`, `fullName`, `nicknames`, `phoneNumbers`, `sanitizedPrimaryPhone`,
                        `emails`, `avatarPath`, `thumbnailPath`, `groupId`, `tagId`,
                        `socialProfiles`, `companyName`, `businessCategory`, `physicalAddress`,
                        `bankAccounts`, `createdAt`, `lastModifiedAt`, `isSynced`, `isWhitelisted`, `remark`
                    )
                    SELECT 
                        c.`id`, c.`fullName`, c.`nicknames`, c.`phoneNumbers`, c.`sanitizedPrimaryPhone`,
                        c.`emails`, c.`avatarPath`, c.`thumbnailPath`,
                        COALESCE(g.`id`, 'una') AS `groupId`,
                        t.`id` AS `tagId`,
                        c.`socialProfiles`, c.`companyName`, c.`businessCategory`, c.`physicalAddress`,
                        c.`bankAccounts`, c.`createdAt`, c.`lastModifiedAt`, c.`isSynced`, c.`isWhitelisted`, c.`remark`
                    FROM `contacts` c
                    LEFT JOIN `contact_groups` g ON c.`groupName` = g.`name`
                    LEFT JOIN `contact_tags` t ON c.`tagName` = t.`name` AND g.`id` = t.`groupId`
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE `contacts`")
                db.execSQL("ALTER TABLE `contacts_new` RENAME TO `contacts`")
            }
        }
    }
}
