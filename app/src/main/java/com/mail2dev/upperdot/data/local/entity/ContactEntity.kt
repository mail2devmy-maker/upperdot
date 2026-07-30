package com.mail2dev.upperdot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mail2dev.upperdot.ui.add_contact.BankAccount
import com.mail2dev.upperdot.ui.add_contact.SocialProfile

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val nicknames: List<String>,
    val phoneNumbers: List<String>,
    val sanitizedPrimaryPhone: String, // For duplicate prevention
    val email: String,
    val avatarPath: String? = null,
    
    // Step 2: Identity / Relational
    val groupName: String = "Unassigned",
    val tagName: String? = null,
    val socialProfiles: List<SocialProfile>,
    
    // Step 3: Corporate
    val companyName: String? = null,
    val businessCategory: String = "General",
    val physicalAddress: String? = null,
    
    // Step 4: Financial
    val bankAccounts: List<BankAccount>,
    
    // Meta
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
