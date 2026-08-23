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

    private val _editingNote = MutableStateFlow<NoteEntity?>(null)
    val editingNote: StateFlow<NoteEntity?> = _editingNote.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction: StateFlow<TransactionEntity?> = _editingTransaction.asStateFlow()

    private val _selectedAttachments = MutableStateFlow<List<String>>(emptyList())
    val selectedAttachments: StateFlow<List<String>> = _selectedAttachments.asStateFlow()

    private val _contactSearchQuery = MutableStateFlow("")
    val contactSearchQuery: StateFlow<String> = _contactSearchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchedContacts: StateFlow<List<com.mail2dev.upperdot.ui.insights.ContactSummary>> = _contactSearchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                contactRepository.allContacts
            } else {
                contactRepository.searchContacts(query)
            }
        }.map { entities ->
            entities.map { com.mail2dev.upperdot.ui.insights.ContactSummary(it.id, it.fullName) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadContact(id: Long) {
        _contactId.value = id
    }

    fun toggleNotes() {
        _isNotesExpanded.value = !_isNotesExpanded.value
    }

    fun toggleTransactions() {
        _isTransactionsExpanded.value = !_isTransactionsExpanded.value
    }

    fun onContactSearchQueryChanged(query: String) {
        _contactSearchQuery.value = query
    }

    fun addAttachmentPath(path: String) {
        _selectedAttachments.value = _selectedAttachments.value + path
    }

    fun removeAttachmentPath(index: Int) {
        val list = _selectedAttachments.value.toMutableList()
        if (index < list.size) {
            list.removeAt(index)
            _selectedAttachments.value = list
        }
    }

    fun onEditNote(note: NoteEntity) {
        _editingNote.value = note
        _selectedAttachments.value = note.attachmentPaths
    }

    fun dismissEditNote() {
        _editingNote.value = null
        _selectedAttachments.value = emptyList()
    }

    fun onEditTransaction(transaction: TransactionEntity) {
        _editingTransaction.value = transaction
        _selectedAttachments.value = transaction.receiptPaths
    }

    fun dismissEditTransaction() {
        _editingTransaction.value = null
        _selectedAttachments.value = emptyList()
    }

    fun updateNote(noteId: Long, contactId: Long, title: String, content: String, attachments: List<String>, voice: String?, createdAt: Long? = null) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = noteId,
                contactId = contactId,
                title = title,
                content = content,
                attachmentPaths = attachments,
                voiceRecordingPath = voice,
                createdAt = createdAt ?: System.currentTimeMillis(),
                lastModifiedAt = System.currentTimeMillis()
            )
            noteRepository.updateNote(note)
            _editingNote.value = null
            _selectedAttachments.value = emptyList()
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun updateTransaction(noteId: Long, contactId: Long, isRevenue: Boolean, title: String, amount: String, detail: String, attachments: List<String>, voice: String?, createdAt: Long? = null) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                id = noteId,
                contactId = contactId,
                title = title,
                amount = amount.toDoubleOrNull() ?: 0.0,
                isRevenue = isRevenue,
                detail = detail,
                receiptPaths = attachments,
                voiceRecordingPath = voice,
                createdAt = createdAt ?: System.currentTimeMillis(),
                lastModifiedAt = System.currentTimeMillis()
            )
            transactionRepository.updateTransaction(transaction)
            _editingTransaction.value = null
            _selectedAttachments.value = emptyList()
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
