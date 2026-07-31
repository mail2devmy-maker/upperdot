package com.mail2dev.upperdot.ui.profile_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.PreferenceRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import com.mail2dev.upperdot.ui.add_contact.BankAccount
import com.mail2dev.upperdot.ui.add_contact.SocialProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
    val bankAccounts: List<BankAccount> = emptyList()
)

class ClientProfileDetailViewModel(
    private val contactRepository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _contactId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val contactProfile: StateFlow<FullContactProfile?> = _contactId
        .filterNotNull()
        .flatMapLatest { id ->
            flow {
                val contact = contactRepository.getContactById(id)
                if (contact != null) {
                    emit(FullContactProfile(
                        id = contact.id,
                        fullName = contact.fullName,
                        nicknames = contact.nicknames.joinToString(", "),
                        phoneNumbers = contact.phoneNumbers,
                        emails = contact.emails,
                        socialProfiles = contact.socialProfiles,
                        group = contact.groupName,
                        tag = contact.tagName ?: "",
                        companyName = contact.companyName ?: "",
                        businessCategory = contact.businessCategory,
                        officeAddress = contact.physicalAddress ?: "",
                        bankAccounts = contact.bankAccounts
                    ))
                } else {
                    emit(null)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<NoteEntity>> = _contactId
        .filterNotNull()
        .flatMapLatest { id -> noteRepository.getNotesForContact(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<TransactionEntity>> = _contactId
        .filterNotNull()
        .flatMapLatest { id -> transactionRepository.getTransactionsForContact(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencySymbol: StateFlow<String> = preferenceRepository.preferences
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")

    private val _isNotesExpanded = MutableStateFlow(true)
    val isNotesExpanded: StateFlow<Boolean> = _isNotesExpanded.asStateFlow()

    private val _isTransactionsExpanded = MutableStateFlow(true)
    val isTransactionsExpanded: StateFlow<Boolean> = _isTransactionsExpanded.asStateFlow()

    fun loadContact(id: Long) {
        _contactId.value = id
    }

    fun toggleNotes() {
        _isNotesExpanded.value = !_isNotesExpanded.value
    }

    fun toggleTransactions() {
        _isTransactionsExpanded.value = !_isTransactionsExpanded.value
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }

    fun deleteContact(onSuccess: () -> Unit) {
        val id = _contactId.value ?: return
        viewModelScope.launch {
            val contact = contactRepository.getContactById(id)
            if (contact != null) {
                contactRepository.deleteContact(contact)
                onSuccess()
            }
        }
    }
}
