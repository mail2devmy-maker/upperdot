package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.ContactDao
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val contactCount: Flow<Int> = contactDao.getContactCount()

    suspend fun getContactById(id: Long): ContactEntity? = contactDao.getContactById(id)
    
    suspend fun getContactByPhone(sanitizedPhone: String): ContactEntity? = contactDao.getContactByPhone(sanitizedPhone)

    suspend fun insertContact(contact: ContactEntity) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: ContactEntity) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    fun searchContacts(query: String): Flow<List<ContactEntity>> = contactDao.searchContacts(query)
}
