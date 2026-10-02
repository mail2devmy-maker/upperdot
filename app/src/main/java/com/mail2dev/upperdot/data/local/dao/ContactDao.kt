package com.mail2dev.upperdot.data.local.dao

import androidx.room.*
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY fullName ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE groupId = 'fav' ORDER BY fullName ASC")
    fun getFavoriteContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isWhitelisted = 1 ORDER BY fullName ASC")
    fun getWhitelistedContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY fullName ASC")
    suspend fun getAllContactsList(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE id = :id")
    fun getContactByIdFlow(id: Long): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE sanitizedPrimaryPhone = :sanitizedPhone OR phoneNumbers LIKE '%' || :sanitizedPhone || '%'")
    suspend fun getContactByPhone(sanitizedPhone: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isWhitelisted = :isWhitelisted WHERE id = :contactId")
    suspend fun updateWhitelistStatus(contactId: Long, isWhitelisted: Boolean)

    @Query("UPDATE contacts SET groupId = 'una', tagId = NULL WHERE groupId = :groupId")
    suspend fun resetGroupIdForContacts(groupId: String)

    @Query("UPDATE contacts SET tagId = NULL WHERE tagId = :tagId")
    suspend fun resetTagIdForContacts(tagId: String)

    @Query("SELECT COUNT(*) FROM contacts")
    fun getContactCount(): Flow<Int>
    
    @Query("""
        SELECT DISTINCT contacts.* FROM contacts 
        LEFT JOIN notes ON contacts.id = notes.contactId 
        LEFT JOIN transactions ON contacts.id = transactions.contactId 
        LEFT JOIN contact_groups ON contacts.groupId = contact_groups.id
        LEFT JOIN contact_tags ON contacts.tagId = contact_tags.id
        WHERE contacts.fullName LIKE '%' || :query || '%' 
        OR contacts.sanitizedPrimaryPhone LIKE '%' || :query || '%' 
        OR contacts.phoneNumbers LIKE '%' || :query || '%' 
        OR contacts.nicknames LIKE '%' || :query || '%' 
        OR contacts.emails LIKE '%' || :query || '%' 
        OR contacts.remark LIKE '%' || :query || '%' 
        OR contacts.companyName LIKE '%' || :query || '%' 
        OR contacts.businessCategory LIKE '%' || :query || '%' 
        OR contacts.physicalAddress LIKE '%' || :query || '%' 
        OR contact_groups.name LIKE '%' || :query || '%' 
        OR contact_tags.name LIKE '%' || :query || '%'
        OR contacts.socialProfiles LIKE '%' || :query || '%' 
        OR contacts.bankAccounts LIKE '%' || :query || '%' 
        OR notes.title LIKE '%' || :query || '%' 
        OR notes.content LIKE '%' || :query || '%' 
        OR transactions.title LIKE '%' || :query || '%' 
        OR transactions.detail LIKE '%' || :query || '%' 
        ORDER BY contacts.fullName ASC
    """)
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Query("DELETE FROM contacts")
    suspend fun deleteAll()
}
