package com.mail2dev.upperdot.data.local.dao

import androidx.room.*
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY fullName ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE groupName = 'Favorites' ORDER BY fullName ASC")
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

    @Query("SELECT COUNT(*) FROM contacts")
    fun getContactCount(): Flow<Int>
    
    @Query("""
        SELECT * FROM contacts 
        WHERE fullName LIKE '%' || :query || '%' 
        OR sanitizedPrimaryPhone LIKE '%' || :query || '%' 
        OR nicknames LIKE '%' || :query || '%'
    """)
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Query("DELETE FROM contacts")
    suspend fun deleteAll()
}
