package com.mail2dev.upperdot.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "transactions",
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
@Serializable
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long,
    val title: String,
    val amount: Double,
    val isRevenue: Boolean,
    val detail: String,
    val receiptPaths: List<String> = emptyList(),
    val voiceRecordingPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
