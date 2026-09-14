package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.BankCardDao
import com.mail2dev.upperdot.data.local.entity.BankCardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

class BankCardRepository(private val bankCardDao: BankCardDao) {

    val allCards: Flow<List<BankCardEntity>> = bankCardDao.getAllCards()
    val cardCount: Flow<Int> = bankCardDao.getCardCount()

    suspend fun insertCard(card: BankCardEntity) {
        bankCardDao.insertCard(card)
    }

    suspend fun insertCards(cards: List<BankCardEntity>) {
        bankCardDao.insertCards(cards)
    }

    suspend fun updateCard(card: BankCardEntity) {
        bankCardDao.updateCard(card)
    }

    suspend fun deleteCard(card: BankCardEntity) {
        deleteCardFiles(card)
        bankCardDao.deleteCard(card)
    }

    suspend fun deleteAll() {
        val cards = bankCardDao.getAllCards().firstOrNull()
        cards?.forEach { deleteCardFiles(it) }
        bankCardDao.deleteAll()
    }

    private fun deleteCardFiles(card: BankCardEntity) {
        try {
            card.qrImagePath?.let { path ->
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