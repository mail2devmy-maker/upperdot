package com.mail2dev.upperdot.data.local.dao

import androidx.room.*
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY fullName ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE sanitizedPrimaryPhone = :sanitizedPhone")
    suspend fun getContactByPhone(sanitizedPhone: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("SELECT COUNT(*) FROM contacts")
    fun getContactCount(): Flow<Int>
    
    @Query("SELECT * FROM contacts WHERE fullName LIKE '%' || :query || '%' OR sanitizedPrimaryPhone LIKE '%' || :query || '%'")
    fun searchContacts(query: String): Flow<List<ContactEntity>>
}
