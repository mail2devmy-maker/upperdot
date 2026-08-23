package com.mail2dev.upperdot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_banks")
data class SavedBankEntity(
    @PrimaryKey val bankName: String
)
