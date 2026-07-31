package com.mail2dev.upperdot.data.local.dao

import androidx.room.*
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

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
        SELECT * FROM transactions 
        WHERE title LIKE '%' || :query || '%' 
        OR detail LIKE '%' || :query || '%' 
        OR contactId IN (SELECT id FROM contacts WHERE fullName LIKE '%' || :query || '%')
    """)
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>
}
