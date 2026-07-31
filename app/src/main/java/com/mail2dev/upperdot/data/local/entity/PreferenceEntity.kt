package com.mail2dev.upperdot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "app_preferences")
@Serializable
data class PreferenceEntity(
    @PrimaryKey val id: Int = 0,
    val syncOverWifi: Boolean = true,
    val syncFrequency: String = "1h",
    val currencySymbol: String = "$",
    val lastSyncTime: Long = 0L
)
