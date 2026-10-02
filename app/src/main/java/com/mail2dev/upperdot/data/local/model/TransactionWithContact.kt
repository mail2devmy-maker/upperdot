package com.mail2dev.upperdot.data.local.model

import androidx.room.Embedded
import com.mail2dev.upperdot.data.local.entity.TransactionEntity

data class TransactionWithContact(
    @Embedded val transaction: TransactionEntity,
    val contactName: String
)
