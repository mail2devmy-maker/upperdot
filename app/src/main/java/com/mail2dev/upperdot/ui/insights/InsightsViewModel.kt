package com.mail2dev.upperdot.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.data.local.model.NoteWithContact
import com.mail2dev.upperdot.data.local.model.TransactionWithContact
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.PreferenceRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import com.mail2dev.upperdot.utils.toFormattedDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

enum class InsightTab {
    NOTES, TRANSACTIONS
}

data class NoteEntry(
    val id: Long,
    val contactId: Long,
    val contactName: String,
    val title: String,
    val content: String,
    val timestamp: String,
    val attachmentCount: Int = 0
)

data class TransactionEntry(
    val id: Long,
    val contactId: Long,
    val contactName: String,
    val title: String,
    val detail: String,
    val amount: String,
    val isRevenue: Boolean,
    val timestamp: String,
    val attachmentCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModel(
    private val contactRepository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(InsightTab.NOTES)
    val selectedTab: StateFlow<InsightTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedContactFilter = MutableStateFlow<String?>(null)
    val selectedContactFilter: StateFlow<String?> = _selectedContactFilter.asStateFlow()

    private val _showAddNoteSheet = MutableStateFlow(false)
    val showAddNoteSheet: StateFlow<Boolean> = _showAddNoteSheet.asStateFlow()

    private val _showAddTransactionSheet = MutableStateFlow(false)
    val showAddTransactionSheet: StateFlow<Boolean> = _showAddTransactionSheet.asStateFlow()

    private val _contactSearchQuery = MutableStateFlow("")
    val contactSearchQuery: StateFlow<String> = _contactSearchQuery.asStateFlow()

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    private val _selectedTransaction = MutableStateFlow<TransactionEntity?>(null)
    val selectedTransaction: StateFlow<TransactionEntity?> = _selectedTransaction.asStateFlow()

    val searchedContacts: StateFlow<List<ContactSummary>> = _contactSearchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                contactRepository.allContacts
            } else {
                contactRepository.searchContacts(query)
            }
        }.map { entities ->
            entities.map { it.toSummary() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Temporary state for new note
    private val _selectedAttachments = MutableStateFlow<List<String>>(emptyList())
    val selectedAttachments: StateFlow<List<String>> = _selectedAttachments.asStateFlow()

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

    fun clearTemporaryNoteState() {
        _selectedAttachments.value = emptyList()
    }

    val contactNames: StateFlow<List<String>> = contactRepository.allContacts
        .map { contacts -> contacts.map { it.fullName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencySymbol: StateFlow<String> = preferenceRepository.preferences
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")

    val notes: StateFlow<List<NoteEntry>> = combine(_searchQuery, _selectedContactFilter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        val flow = if (query.isEmpty()) {
            noteRepository.allNotesWithContact
        } else {
            noteRepository.searchNotesWithContact(query)
        }
        
        if (filter != null) {
            flow.map { list -> list.filter { it.contactName == filter } }
        } else {
            flow
        }
    }.map { entities -> 
        entities.map { it.toEntry() } 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntry>> = combine(_searchQuery, _selectedContactFilter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        val flow = if (query.isEmpty()) {
            transactionRepository.allTransactionsWithContact
        } else {
            transactionRepository.searchTransactionsWithContact(query)
        }

        if (filter != null) {
            flow.map { list -> list.filter { it.contactName == filter } }
        } else {
            flow
        }
    }.map { entities ->
        entities.map { it.toEntry() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRevenue = transactions.map { list ->
        list.filter { it.isRevenue }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses = transactions.map { list ->
        list.filter { !it.isRevenue }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netProfit = combine(totalRevenue, totalExpenses) { rev, exp ->
        rev - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onTabSelected(tab: InsightTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onContactFilterSelected(contactName: String) {
        _selectedContactFilter.value = contactName
    }

    fun onContactSearchQueryChanged(query: String) {
        _contactSearchQuery.value = query
    }

    fun clearFilters() {
        _selectedContactFilter.value = null
    }

    fun onAddNoteClicked() {
        _showAddNoteSheet.value = true
    }

    fun dismissAddNoteSheet() {
        _showAddNoteSheet.value = false
    }

    fun onAddTransactionClicked() {
        _showAddTransactionSheet.value = true
    }

    fun dismissAddTransactionSheet() {
        _showAddTransactionSheet.value = false
    }

    fun selectNote(noteId: Long) {
        viewModelScope.launch {
            _selectedNote.value = noteRepository.getNoteById(noteId)
        }
    }

    fun dismissNoteViewer() {
        _selectedNote.value = null
    }

    fun selectTransaction(transactionId: Long) {
        viewModelScope.launch {
            _selectedTransaction.value = transactionRepository.getTransactionById(transactionId)
        }
    }

    fun dismissTransactionViewer() {
        _selectedTransaction.value = null
    }

    fun saveNote(
        contactId: Long,
        title: String, 
        content: String, 
        attachments: List<String> = emptyList(), 
        voicePath: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = NoteEntity(
                contactId = contactId,
                title = title,
                content = content,
                attachmentPaths = attachments,
                voiceRecordingPath = voicePath
            )
            noteRepository.insertNote(note)
            withContext(Dispatchers.Main) {
                _showAddNoteSheet.value = false
                _contactSearchQuery.value = ""
            }
        }
    }

    fun saveTransaction(
        contactId: Long, 
        isRevenue: Boolean, 
        title: String, 
        amount: String, 
        detail: String,
        attachments: List<String> = emptyList(),
        voicePath: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = TransactionEntity(
                contactId = contactId,
                title = title,
                amount = amount.toDoubleOrNull() ?: 0.0,
                isRevenue = isRevenue,
                detail = detail,
                receiptPaths = attachments,
                voiceRecordingPath = voicePath
            )
            transactionRepository.insertTransaction(transaction)
            withContext(Dispatchers.Main) {
                _showAddTransactionSheet.value = false
                _contactSearchQuery.value = ""
            }
        }
    }
}

data class ContactSummary(
    val id: Long,
    val fullName: String
)

private fun com.mail2dev.upperdot.data.local.entity.ContactEntity.toSummary() = ContactSummary(
    id = id,
    fullName = fullName
)

private fun NoteWithContact.toEntry() = NoteEntry(
    id = note.id,
    contactId = note.contactId,
    contactName = contactName,
    title = note.title,
    content = note.content,
    timestamp = note.createdAt.toFormattedDate(),
    attachmentCount = note.attachmentPaths.size
)

private fun TransactionWithContact.toEntry() = TransactionEntry(
    id = transaction.id,
    contactId = transaction.contactId,
    contactName = contactName,
    title = transaction.title,
    detail = transaction.detail,
    amount = String.format(Locale.getDefault(), "%.2f", transaction.amount),
    isRevenue = transaction.isRevenue,
    timestamp = transaction.createdAt.toFormattedDate(),
    attachmentCount = transaction.receiptPaths.size
)
