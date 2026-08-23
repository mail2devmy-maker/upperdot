package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.SavedBankDao
import com.mail2dev.upperdot.data.local.entity.SavedBankEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BankSuggestionRepository(private val savedBankDao: SavedBankDao) {
    
    val savedBanks: Flow<List<String>> = savedBankDao.getAllSavedBanks().map { entities ->
        entities.map { it.bankName }
    }

    suspend fun saveBankName(name: String) {
        if (name.isNotBlank()) {
            savedBankDao.insertBank(SavedBankEntity(name.trim().uppercase()))
        }
    }

    suspend fun deleteBank(name: String) {
        savedBankDao.deleteBank(name)
    }

    suspend fun renameBank(oldName: String, newName: String) {
        if (newName.isNotBlank()) {
            savedBankDao.updateBankName(oldName, newName.trim().uppercase())
        }
    }
}
