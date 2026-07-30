package com.mail2dev.upperdot.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

enum class InsightTab {
    NOTES, TRANSACTIONS
}

data class NoteEntry(
    val id: String,
    val contactId: String,
    val contactName: String,
    val title: String,
    val content: String,
    val timestamp: String,
    val attachmentCount: Int = 0
)

data class TransactionEntry(
    val id: String,
    val contactId: String,
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
    private val transactionRepository: TransactionRepository
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

    val notes: StateFlow<List<NoteEntry>> = combine(_searchQuery, _selectedContactFilter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, _) ->
        if (query.isEmpty()) {
            noteRepository.allNotes
        } else {
            noteRepository.searchNotes(query)
        }
    }.map { entities -> 
        entities.map { it.toEntry() } 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntry>> = combine(_searchQuery, _selectedContactFilter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, _) ->
        if (query.isEmpty()) {
            transactionRepository.allTransactions
        } else {
            transactionRepository.searchTransactions(query)
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

    fun saveNote(
        contactId: String,
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
            _showAddNoteSheet.value = false
            _contactSearchQuery.value = ""
        }
    }

    fun saveTransaction(
        contactId: String, 
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
                attachmentPaths = attachments,
                voiceRecordingPath = voicePath
            )
            transactionRepository.insertTransaction(transaction)
            _showAddTransactionSheet.value = false
            _contactSearchQuery.value = ""
        }
    }
}

data class ContactSummary(
    val id: String,
    val fullName: String
)

private fun com.mail2dev.upperdot.data.local.entity.ContactEntity.toSummary() = ContactSummary(
    id = id.toString(),
    fullName = fullName
)

private fun NoteEntity.toEntry() = NoteEntry(
    id = id,
    contactId = contactId,
    contactName = "test", // Resolver needed
    title = title,
    content = content,
    timestamp = "Jul 28, 2026 • 02:28 AM", // Formatter needed
    attachmentCount = attachmentPaths.size
)

private fun TransactionEntity.toEntry() = TransactionEntry(
    id = id,
    contactId = contactId,
    contactName = "test", // Resolver needed
    title = title,
    detail = detail,
    amount = String.format(Locale.getDefault(), "%.2f", amount),
    isRevenue = isRevenue,
    timestamp = "Jul 28, 2026 • 11:28 AM", // Formatter needed
    attachmentCount = attachmentPaths.size
)
