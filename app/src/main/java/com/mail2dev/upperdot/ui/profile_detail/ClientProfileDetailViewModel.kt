package com.mail2dev.upperdot.ui.profile_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.ui.add_contact.BankAccount
import com.mail2dev.upperdot.ui.add_contact.SocialProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FullContactProfile(
    val id: Long = 0L,
    val fullName: String = "",
    val nicknames: String = "",
    val phoneNumbers: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val socialProfiles: List<SocialProfile> = emptyList(),
    val group: String = "",
    val tag: String = "",
    val companyName: String = "",
    val businessCategory: String = "",
    val officeAddress: String = "",
    val bankAccounts: List<BankAccount> = emptyList(),
    val noteCount: Int = 0,
    val transactionCount: Int = 0
)

class ClientProfileDetailViewModel : ViewModel() {

    private val _contactProfile = MutableStateFlow<FullContactProfile?>(null)
    val contactProfile: StateFlow<FullContactProfile?> = _contactProfile.asStateFlow()

    private val _isNotesExpanded = MutableStateFlow(true)
    val isNotesExpanded: StateFlow<Boolean> = _isNotesExpanded.asStateFlow()

    private val _isTransactionsExpanded = MutableStateFlow(true)
    val isTransactionsExpanded: StateFlow<Boolean> = _isTransactionsExpanded.asStateFlow()

    fun loadContact(id: Long) {
        viewModelScope.launch {
            // TODO: Load from Room DB
            // For now, setting dummy data matching user screenshots
            _contactProfile.value = FullContactProfile(
                id = id,
                fullName = "hdhdh",
                nicknames = "hdhdg",
                phoneNumbers = listOf("9797999"),
                emails = listOf("phoneaiman@gmail.com"),
                companyName = "gsgsg",
                businessCategory = "GENERAL",
                officeAddress = "hzhdh",
                bankAccounts = listOf(BankAccount("MAYBANK", "ysgsgsg", "6565959")),
                socialProfiles = listOf(SocialProfile("WHATSAPP", "vdhdhdb")),
                group = "Family",
                tag = "Cousin"
            )
        }
    }

    fun toggleNotes() {
        _isNotesExpanded.value = !_isNotesExpanded.value
    }

    fun toggleTransactions() {
        _isTransactionsExpanded.value = !_isTransactionsExpanded.value
    }

    fun deleteContact(onSuccess: () -> Unit) {
        // TODO: Delete from Room and Drive
        onSuccess()
    }
}
