package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.TransactionDao
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.data.local.model.TransactionWithContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

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
        // Delete receipt images and voice recording files from disk
        deleteTransactionFiles(transaction)

        // Remove entry from Room database
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteAll() {
        val transactions = transactionDao.getAllTransactions().firstOrNull()
        transactions?.forEach { deleteTransactionFiles(it) }

        transactionDao.deleteAll()
    }

    fun searchTransactions(query: String): Flow<List<TransactionEntity>> =
        transactionDao.searchTransactions(query)

    fun searchTransactionsWithContact(query: String): Flow<List<TransactionWithContact>> =
        transactionDao.searchTransactionsWithContact(query)

    /**
     * Deletes physical receipt images and voice recordings from internal storage.
     */
    private fun deleteTransactionFiles(transaction: TransactionEntity) {
        try {
            // 1. Delete all receipt images/files in receiptPaths
            transaction.receiptPaths.forEach { path ->
                if (path.isNotBlank()) {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }

            // 2. Delete voice recording file if present
            transaction.voiceRecordingPath?.let { path ->
                if (path.isNotBlank()) {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}