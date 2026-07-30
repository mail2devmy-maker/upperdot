package com.mail2dev.upperdot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bank_cards")
data class BankCardEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bankName: String,
    val accountNumber: String,
    val cardHolderName: String,
    val themeColor: Long,
    val swiftBic: String? = null,
    val qrImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis()
)
