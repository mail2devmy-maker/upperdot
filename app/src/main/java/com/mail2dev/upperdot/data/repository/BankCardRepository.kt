package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.BankCardDao
import com.mail2dev.upperdot.data.local.entity.BankCardEntity
import kotlinx.coroutines.flow.Flow

class BankCardRepository(private val bankCardDao: BankCardDao) {

    val allCards: Flow<List<BankCardEntity>> = bankCardDao.getAllCards()
    val cardCount: Flow<Int> = bankCardDao.getCardCount()

    suspend fun insertCard(card: BankCardEntity) {
        bankCardDao.insertCard(card)
    }

    suspend fun insertCards(cards: List<BankCardEntity>) {
        bankCardDao.insertCards(cards)
    }

    suspend fun deleteCard(card: BankCardEntity) {
        bankCardDao.deleteCard(card)
    }

    suspend fun deleteAll() {
        bankCardDao.deleteAll()
    }
}
