package com.mail2dev.upperdot.ui.connections_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.PreferenceRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ContactSummary(
    val id: Long,
    val fullName: String,
    val nicknames: List<String>,
    val primaryPhone: String,
    val group: String? = null,
    val tag: String? = null
)

sealed class ConnectionsUIState {
    object Loading : ConnectionsUIState()
    object Empty : ConnectionsUIState()
    data class Success(val contacts: List<ContactSummary>) : ConnectionsUIState()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
class ConnectionsListViewModel(
    private val repository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _showAddNoteSheet = MutableStateFlow(false)
    val showAddNoteSheet: StateFlow<Boolean> = _showAddNoteSheet.asStateFlow()

    private val _showAddTransactionSheet = MutableStateFlow(false)
    val showAddTransactionSheet: StateFlow<Boolean> = _showAddTransactionSheet.asStateFlow()

    private val _preSelectedContact = MutableStateFlow<ContactSummary?>(null)
    val preSelectedContact: StateFlow<ContactSummary?> = _preSelectedContact.asStateFlow()

    private val _contactSearchQuery = MutableStateFlow("")
    val contactSearchQuery: StateFlow<String> = _contactSearchQuery.asStateFlow()

    private val _selectedAttachments = MutableStateFlow<List<String>>(emptyList())
    val selectedAttachments: StateFlow<List<String>> = _selectedAttachments.asStateFlow()

    val currencySymbol: StateFlow<String> = preferenceRepository.preferences
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")

    val searchedContacts: StateFlow<List<ContactSummary>> = _contactSearchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                repository.allContacts
            } else {
                repository.searchContacts(query)
            }
        }.map { entities ->
            entities.map { it.toSummary() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ConnectionsUIState> = combine(_searchQuery, _selectedFilter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        val contactsFlow = if (query.isEmpty()) {
            repository.allContacts
        } else {
            repository.searchContacts(query)
        }
        
        contactsFlow.map { list ->
            if (filter == "All") {
                list
            } else {
                list.filter { it.groupName == filter }
            }
        }
    }.map { entities ->
        if (entities.isEmpty()) {
            ConnectionsUIState.Empty
        } else {
            ConnectionsUIState.Success(entities.map { it.toSummary() })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConnectionsUIState.Loading
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onContactSearchQueryChanged(query: String) {
        _contactSearchQuery.value = query
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
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

    fun clearMedia() {
        _selectedAttachments.value = emptyList()
    }

    fun onDialContact(contact: ContactSummary) {
        // UI implementation
    }

    fun onAddNote(contactId: Long) {
        viewModelScope.launch {
            val contact = repository.getContactById(contactId)?.toSummary()
            _preSelectedContact.value = contact
            _showAddNoteSheet.value = true
        }
    }

    fun onAddTransaction(contactId: Long) {
        viewModelScope.launch {
            val contact = repository.getContactById(contactId)?.toSummary()
            _preSelectedContact.value = contact
            _showAddTransactionSheet.value = true
        }
    }

    fun dismissAddNoteSheet() {
        _showAddNoteSheet.value = false
        _preSelectedContact.value = null
        _contactSearchQuery.value = ""
        clearMedia()
    }

    fun dismissAddTransactionSheet() {
        _showAddTransactionSheet.value = false
        _preSelectedContact.value = null
        _contactSearchQuery.value = ""
        clearMedia()
    }

    fun saveNote(contactId: Long, title: String, content: String, attachments: List<String>, voice: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = NoteEntity(
                contactId = contactId,
                title = title,
                content = content,
                attachmentPaths = attachments,
                voiceRecordingPath = voice
            )
            noteRepository.insertNote(note)
            withContext(Dispatchers.Main) {
                dismissAddNoteSheet()
            }
        }
    }

    fun saveTransaction(contactId: Long, isRevenue: Boolean, title: String, amount: String, detail: String, attachments: List<String>, voice: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = TransactionEntity(
                contactId = contactId,
                title = title,
                amount = amount.toDoubleOrNull() ?: 0.0,
                isRevenue = isRevenue,
                detail = detail,
                attachmentPaths = attachments,
                voiceRecordingPath = voice
            )
            transactionRepository.insertTransaction(transaction)
            withContext(Dispatchers.Main) {
                dismissAddTransactionSheet()
            }
        }
    }
}

private fun ContactEntity.toSummary() = ContactSummary(
    id = id,
    fullName = fullName,
    nicknames = nicknames,
    primaryPhone = sanitizedPrimaryPhone,
    group = groupName,
    tag = tagName
)
