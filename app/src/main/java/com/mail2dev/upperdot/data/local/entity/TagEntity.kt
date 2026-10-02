package com.mail2dev.upperdot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "contact_tags")
@Serializable
data class TagEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val name: String
)
