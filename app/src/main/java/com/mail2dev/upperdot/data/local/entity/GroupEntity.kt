package com.mail2dev.upperdot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "contact_groups")
@Serializable
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sortOrder: Int = 0
)
