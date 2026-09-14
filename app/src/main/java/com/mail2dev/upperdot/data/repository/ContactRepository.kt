package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.ContactDao
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val favoriteContacts: Flow<List<ContactEntity>> = contactDao.getFavoriteContacts()
    val whitelistedContacts: Flow<List<ContactEntity>> = contactDao.getWhitelistedContacts()
    val contactCount: Flow<Int> = contactDao.getContactCount()

    suspend fun getAllContactsList(): List<ContactEntity> = contactDao.getAllContactsList()

    suspend fun getContactById(id: Long): ContactEntity? = contactDao.getContactById(id)

    fun getContactByIdFlow(id: Long): Flow<ContactEntity?> = contactDao.getContactByIdFlow(id)
    
    suspend fun getContactByPhone(sanitizedPhone: String): ContactEntity? = contactDao.getContactByPhone(sanitizedPhone)

    suspend fun findContactByPhone(rawPhone: String): ContactEntity? {
        val sanitized = com.mail2dev.upperdot.util.ContactUtils.smartSanitize(rawPhone)
        if (sanitized.isEmpty()) return null
        return contactDao.getContactByPhone(sanitized)
    }

    suspend fun insertContact(contact: ContactEntity): Long {
        return contactDao.insertContact(contact)
    }

    suspend fun insertContacts(contacts: List<ContactEntity>) {
        contactDao.insertContacts(contacts)
    }

    suspend fun updateContact(contact: ContactEntity) {
        contactDao.updateContact(contact)
    }

    suspend fun updateWhitelistStatus(contactId: Long, isWhitelisted: Boolean) {
        contactDao.updateWhitelistStatus(contactId, isWhitelisted)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    suspend fun deleteAll() {
        contactDao.deleteAll()
    }

    fun searchContacts(query: String): Flow<List<ContactEntity>> = contactDao.searchContacts(query)
}
