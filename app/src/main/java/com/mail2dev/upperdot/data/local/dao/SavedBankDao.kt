package com.mail2dev.upperdot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mail2dev.upperdot.data.local.entity.SavedBankEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedBankDao {
    @Query("SELECT * FROM saved_banks ORDER BY bankName ASC")
    fun getAllSavedBanks(): Flow<List<SavedBankEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBank(bank: SavedBankEntity)

    @Query("SELECT COUNT(*) FROM saved_banks")
    suspend fun getBankCount(): Int

    @Query("DELETE FROM saved_banks WHERE bankName = :bankName")
    suspend fun deleteBank(bankName: String)

    @Query("UPDATE saved_banks SET bankName = :newName WHERE bankName = :oldName")
    suspend fun updateBankName(oldName: String, newName: String)
}
