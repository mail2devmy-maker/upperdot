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

    fun onAddCardClicked(onSuccess: () -> Unit, onLimitExceeded: () -> Unit) {
        if (!_isPremium.value && _bankCards.value.size >= 1) {
            onLimitExceeded()
        } else {
            onSuccess()
        }
    }

    fun onDeleteCard(cardId: String) {
        _bankCards.value = _bankCards.value.filter { it.id != cardId }
    }
}
