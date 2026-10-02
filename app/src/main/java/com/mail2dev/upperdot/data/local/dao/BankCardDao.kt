package com.mail2dev.upperdot.data.local.dao

import androidx.room.*
import com.mail2dev.upperdot.data.local.entity.BankCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankCardDao {
    @Query("SELECT * FROM bank_cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<BankCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: BankCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<BankCardEntity>)

    @Update
    suspend fun updateCard(card: BankCardEntity)

    @Delete
    suspend fun deleteCard(card: BankCardEntity)

    @Query("DELETE FROM bank_cards")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM bank_cards")
    fun getCardCount(): Flow<Int>
}
