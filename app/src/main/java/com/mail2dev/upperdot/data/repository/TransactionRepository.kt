package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.TransactionDao
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val transactionCount: Flow<Int> = transactionDao.getTransactionCount()

    fun getTransactionsForContact(contactId: String): Flow<List<TransactionEntity>> = 
        transactionDao.getTransactionsForContact(contactId)

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    fun searchTransactions(query: String): Flow<List<TransactionEntity>> = 
        transactionDao.searchTransactions(query)
}
