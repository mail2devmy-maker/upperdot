package com.mail2dev.upperdot.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contactId"])]
)
data class NoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val contactId: String,
    val title: String,
    val content: String,
    val attachmentPaths: List<String> = emptyList(), // Stored as JSON string via TypeConverter
    val voiceRecordingPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
