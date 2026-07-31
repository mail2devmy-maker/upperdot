package com.mail2dev.upperdot.data.local

import com.mail2dev.upperdot.data.local.entity.BankCardEntity
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.PreferenceEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseBackup(
    val contacts: List<ContactEntity>,
    val notes: List<NoteEntity>,
    val transactions: List<TransactionEntity>,
    val bankCards: List<BankCardEntity>,
    val preferences: PreferenceEntity? = null,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val version: Int = 1
)
