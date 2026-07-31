package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.TransactionDao
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.data.local.model.TransactionWithContact
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allTransactionsWithContact: Flow<List<TransactionWithContact>> = transactionDao.getAllTransactionsWithContact()
    val transactionCount: Flow<Int> = transactionDao.getTransactionCount()

    fun getTransactionsForContact(contactId: Long): Flow<List<TransactionEntity>> = 
        transactionDao.getTransactionsForContact(contactId)

    suspend fun getTransactionById(id: Long): TransactionEntity? = 
        transactionDao.getTransactionById(id)

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun insertTransactions(transactions: List<TransactionEntity>) {
        transactionDao.insertTransactions(transactions)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteAll() {
        transactionDao.deleteAll()
    }

    fun searchTransactions(query: String): Flow<List<TransactionEntity>> = 
        transactionDao.searchTransactions(query)

    fun searchTransactionsWithContact(query: String): Flow<List<TransactionWithContact>> = 
        transactionDao.searchTransactionsWithContact(query)
}
