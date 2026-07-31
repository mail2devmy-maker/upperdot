package com.mail2dev.upperdot.data.local.dao

import androidx.room.*
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.data.local.model.TransactionWithContact
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("""
        SELECT transactions.*, contacts.fullName as contactName 
        FROM transactions 
        INNER JOIN contacts ON transactions.contactId = contacts.id 
        ORDER BY transactions.createdAt DESC
    """)
    fun getAllTransactionsWithContact(): Flow<List<TransactionWithContact>>

    @Query("SELECT * FROM transactions WHERE contactId = :contactId ORDER BY createdAt DESC")
    fun getTransactionsForContact(contactId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Flow<Int>

    @Query("""
        SELECT transactions.*, contacts.fullName as contactName 
        FROM transactions 
        INNER JOIN contacts ON transactions.contactId = contacts.id 
        WHERE transactions.title LIKE '%' || :query || '%' 
        OR transactions.detail LIKE '%' || :query || '%' 
        OR contacts.fullName LIKE '%' || :query || '%'
        ORDER BY transactions.createdAt DESC
    """)
    fun searchTransactionsWithContact(query: String): Flow<List<TransactionWithContact>>

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE title LIKE '%' || :query || '%' 
        OR detail LIKE '%' || :query || '%' 
        OR contactId IN (SELECT id FROM contacts WHERE fullName LIKE '%' || :query || '%')
    """)
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>
}
