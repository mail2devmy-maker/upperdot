package com.mail2dev.upperdot.ui.digital_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.mail2dev.upperdot.data.local.entity.BankCardEntity
import com.mail2dev.upperdot.data.repository.BankCardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BankCard(
    val id: String,
    val bankName: String,
    val accountNumber: String,
    val cardHolderName: String,
    val themeColor: Long, // Hex color for the card
    val qrImagePath: String? = null
)

class DigitalWalletViewModel(private val repository: BankCardRepository) : ViewModel() {

    private val _isPremium = MutableStateFlow(true) // Placeholder
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    val bankCards: StateFlow<List<BankCard>> = repository.allCards
        .map { entities -> entities.map { it.toModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddCardSheet = MutableStateFlow(false)
    val showAddCardSheet: StateFlow<Boolean> = _showAddCardSheet.asStateFlow()

    private val _showQuickWalletSheet = MutableStateFlow(false)
    val showQuickWalletSheet: StateFlow<Boolean> = _showQuickWalletSheet.asStateFlow()

    fun onAddCardClicked(onSuccess: () -> Unit, onLimitExceeded: () -> Unit) {
        viewModelScope.launch {
            val count = repository.cardCount.first()
            if (!_isPremium.value && count >= 1) {
                onLimitExceeded()
            } else {
                _showAddCardSheet.value = true
            }
        }
    }

    fun dismissAddCardSheet() {
        _showAddCardSheet.value = false
    }

    fun onQuickWalletRequested() {
        _showQuickWalletSheet.value = true
    }

    fun dismissQuickWalletSheet() {
        _showQuickWalletSheet.value = false
    }

    fun saveCard(bankName: String, holderName: String, accountNumber: String, color: Long, swiftBic: String?, qrPath: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = BankCardEntity(
                bankName = bankName,
                accountNumber = accountNumber,
                cardHolderName = holderName,
                themeColor = color,
                swiftBic = swiftBic,
                qrImagePath = qrPath
            )
            repository.insertCard(entity)
            _showAddCardSheet.value = false
        }
    }

    fun onDeleteCard(cardId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Need a full entity to delete
            repository.allCards.first().find { it.id == cardId }?.let {
                repository.deleteCard(it)
            }
        }
    }
}

private fun BankCardEntity.toModel() = BankCard(
    id = id,
    bankName = bankName,
    accountNumber = accountNumber,
    cardHolderName = cardHolderName,
    themeColor = themeColor,
    qrImagePath = qrImagePath
)
