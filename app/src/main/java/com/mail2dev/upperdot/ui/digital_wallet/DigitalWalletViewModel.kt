package com.mail2dev.upperdot.ui.digital_wallet

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BankCard(
    val id: String,
    val bankName: String,
    val accountNumber: String,
    val cardHolderName: String,
    val themeColor: Long, // Hex color for the card
    val qrImagePath: String? = null
)

class DigitalWalletViewModel : ViewModel() {

    private val _isPremium = MutableStateFlow(true) // Placeholder
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _bankCards = MutableStateFlow<List<BankCard>>(
        listOf(
            BankCard(
                id = "1",
                bankName = "maybank",
                accountNumber = "123456",
                cardHolderName = "card holder name",
                themeColor = 0xFF00C8FF
            )
        )
    )
    val bankCards: StateFlow<List<BankCard>> = _bankCards.asStateFlow()

    private val _showAddCardSheet = MutableStateFlow(false)
    val showAddCardSheet: StateFlow<Boolean> = _showAddCardSheet.asStateFlow()

    private val _showQuickWalletSheet = MutableStateFlow(false)
    val showQuickWalletSheet: StateFlow<Boolean> = _showQuickWalletSheet.asStateFlow()

    fun onAddCardClicked(onSuccess: () -> Unit, onLimitExceeded: () -> Unit) {
        if (!_isPremium.value && _bankCards.value.size >= 1) {
            onLimitExceeded()
        } else {
            _showAddCardSheet.value = true
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

    fun saveCard(bankName: String, holderName: String, accountNumber: String, color: Long) {
        // TODO: Save to Room
        _showAddCardSheet.value = false
    }

    fun onDeleteCard(cardId: String) {
        _bankCards.value = _bankCards.value.filter { it.id != cardId }
    }
}
