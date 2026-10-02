package com.mail2dev.upperdot.ui.connections_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.HierarchyRepository
import com.mail2dev.upperdot.data.repository.NoteRepository
import com.mail2dev.upperdot.data.repository.PreferenceRepository
import com.mail2dev.upperdot.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.mail2dev.upperdot.data.local.model.SearchMatch
import com.mail2dev.upperdot.util.SearchUtils

data class ContactSummary(
    val id: Long,
    val fullName: String,
    val nicknames: List<String>,
    val primaryPhone: String,
    val phoneNumbers: List<String> = emptyList(),
    val avatarPath: String? = null,
    val thumbnailPath: String? = null,
    val socialProfiles: List<com.mail2dev.upperdot.ui.add_contact.SocialProfile> = emptyList(),
    val group: String? = null,
    val tag: String? = null,
    val matches: List<SearchMatch> = emptyList()
)

sealed class ConnectionsUIState {
    object Loading : ConnectionsUIState()
    object Empty : ConnectionsUIState()
    data class Success(val contacts: List<ContactSummary>) : ConnectionsUIState()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ConnectionsListViewModel(
    private val repository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val transactionRepository: TransactionRepository,
    private val preferenceRepository: PreferenceRepository,
    private val billingRepository: com.mail2dev.upperdot.data.repository.BillingRepository,
    private val hierarchyRepository: HierarchyRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _selectedTagFilter = MutableStateFlow("All Tags")
    val selectedTagFilter: StateFlow<String> = _selectedTagFilter.asStateFlow()

    val availableFilters: StateFlow<List<String>> = hierarchyRepository.groups
        .map { groups ->
            listOf("All") + groups.map { it.name }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            listOf("All", "Favorites", "Work", "Family", "Vendor", "Unassigned")
        )

    val availableTagFilters: StateFlow<List<String>> = combine(
        hierarchyRepository.groups,
        _selectedFilter
    ) { groups, selectedGroup ->
        if (selectedGroup == "All") {
            emptyList()
        } else {
            val matchingGroup = groups.find { it.name == selectedGroup || it.id == selectedGroup }
            if (matchingGroup != null && matchingGroup.tags.isNotEmpty()) {
                listOf("All Tags") + matchingGroup.tags.map { it.name }
            } else {
                emptyList()
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

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

    val isMediaCompressionEnabled: StateFlow<Boolean> = preferenceRepository.preferences
        .map { it.isMediaCompressionEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val searchedContacts: StateFlow<List<ContactSummary>> = combine(
        _contactSearchQuery.debounce(300),
        hierarchyRepository.groups
    ) { query, groupsList ->
        query to groupsList
    }.flatMapLatest { (query, groupsList) ->
        val groupsMap = groupsList.associateBy { it.id }
        val tagsMap = groupsList.flatMap { it.tags }.associateBy { it.id }
        val contactsFlow = if (query.isEmpty()) {
            repository.allContacts
        } else {
            repository.searchContacts(query)
        }
        contactsFlow.map { entities ->
            entities.map { contact ->
                val groupName = groupsMap[contact.groupId]?.name ?: "Unassigned"
                val tagName = tagsMap[contact.tagId]?.name
                contact.toSummary(groupName, tagName)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

private data class FilterParams(
    val query: String,
    val groupFilter: String,
    val tagFilter: String,
    val groupsList: List<com.mail2dev.upperdot.ui.relationship_hierarchy.HierarchyGroup>
)

    val uiState: StateFlow<ConnectionsUIState> = combine(
        _searchQuery,
        _selectedFilter,
        _selectedTagFilter,
        hierarchyRepository.groups
    ) { query, groupFilter, tagFilter, groupsList ->
        FilterParams(query, groupFilter, tagFilter, groupsList)
    }.flatMapLatest { (query, groupFilter, tagFilter, groupsList) ->
        val groupsMap = groupsList.associateBy { it.id }
        val tagsMap = groupsList.flatMap { it.tags }.associateBy { it.id }

        val contactsFlow = if (query.isEmpty()) {
            repository.allContacts
        } else {
            repository.searchContacts(query)
        }
        
        contactsFlow.map { list ->
            val resolvedList = list.map { contact ->
                val groupName = groupsMap[contact.groupId]?.name ?: "Unassigned"
                val tagName = tagsMap[contact.tagId]?.name
                contact to (groupName to tagName)
            }

            val filteredList = resolvedList.filter { (contact, names) ->
                val (groupName, tagName) = names
                val groupMatches = if (groupFilter == "All") {
                    true
                } else {
                    groupName == groupFilter || contact.groupId == groupFilter
                }

                val tagMatches = if (tagFilter == "All Tags") {
                    true
                } else {
                    tagName == tagFilter || contact.tagId == tagFilter
                }

                groupMatches && tagMatches
            }

            filteredList.map { (contact, names) ->
                val (groupName, tagName) = names
                val matches = if (query.isNotBlank()) {
                    SearchUtils.extractSearchMatches(contact, query, groupName, tagName)
                } else {
                    emptyList()
                }
                contact.toSummary(groupName, tagName, matches)
            }
        }
    }.map { summaries ->
        if (summaries.isEmpty()) {
            ConnectionsUIState.Empty
        } else {
            ConnectionsUIState.Success(summaries)
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
        _selectedTagFilter.value = "All Tags"
    }

    fun onTagFilterSelected(tagFilter: String) {
        _selectedTagFilter.value = tagFilter
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

    private suspend fun ContactEntity.toSummaryResolved(): ContactSummary {
        val groupsList = hierarchyRepository.groups.first()
        val groupsMap = groupsList.associateBy { it.id }
        val tagsMap = groupsList.flatMap { it.tags }.associateBy { it.id }
        val groupName = groupsMap[groupId]?.name ?: "Unassigned"
        val tagName = tagsMap[tagId]?.name
        return toSummary(groupName, tagName)
    }

    fun onAddNote(contactId: Long, onLimitExceeded: () -> Unit) {
        viewModelScope.launch {
            val isPremiumUser = billingRepository.isPremium.value
            val count = noteRepository.noteCount.first()
            if (!isPremiumUser && count >= 20) {
                onLimitExceeded()
            } else {
                val contact = repository.getContactById(contactId)?.toSummaryResolved()
                _preSelectedContact.value = contact
                _showAddNoteSheet.value = true
            }
        }
    }

    fun onAddNoteByPhone(phone: String, onLimitExceeded: () -> Unit) {
        viewModelScope.launch {
            val isPremiumUser = billingRepository.isPremium.value
            val count = noteRepository.noteCount.first()
            if (!isPremiumUser && count >= 20) {
                onLimitExceeded()
            } else {
                val contact = repository.findContactByPhone(phone)?.toSummaryResolved()
                _preSelectedContact.value = contact
                _showAddNoteSheet.value = true
            }
        }
    }

    fun onAddTransaction(contactId: Long, onLimitExceeded: () -> Unit) {
        viewModelScope.launch {
            val isPremiumUser = billingRepository.isPremium.value
            val count = transactionRepository.transactionCount.first()
            if (!isPremiumUser && count >= 20) {
                onLimitExceeded()
            } else {
                val contact = repository.getContactById(contactId)?.toSummaryResolved()
                _preSelectedContact.value = contact
                _showAddTransactionSheet.value = true
            }
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

    fun saveNote(contactId: Long, title: String, content: String, attachments: List<String>, voice: String?, noteId: Long? = null, createdAt: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = if (noteId == null) {
                NoteEntity(
                    contactId = contactId,
                    title = title,
                    content = content,
                    attachmentPaths = attachments,
                    voiceRecordingPath = voice,
                    createdAt = createdAt ?: System.currentTimeMillis()
                )
            } else {
                NoteEntity(
                    id = noteId,
                    contactId = contactId,
                    title = title,
                    content = content,
                    attachmentPaths = attachments,
                    voiceRecordingPath = voice,
                    createdAt = createdAt ?: System.currentTimeMillis(),
                    lastModifiedAt = System.currentTimeMillis()
                )
            }

            if (noteId == null) {
                noteRepository.insertNote(note)
            } else {
                noteRepository.updateNote(note)
            }

            withContext(Dispatchers.Main) {
                dismissAddNoteSheet()
            }
        }
    }

    fun saveTransaction(contactId: Long, isRevenue: Boolean, title: String, amount: String, detail: String, attachments: List<String>, voice: String?, transactionId: Long? = null, createdAt: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = if (transactionId == null) {
                TransactionEntity(
                    contactId = contactId,
                    title = title,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    isRevenue = isRevenue,
                    detail = detail,
                    receiptPaths = attachments,
                    voiceRecordingPath = voice,
                    createdAt = createdAt ?: System.currentTimeMillis()
                )
            } else {
                TransactionEntity(
                    id = transactionId,
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
            }
            
            if (transactionId == null) {
                transactionRepository.insertTransaction(transaction)
            } else {
                transactionRepository.updateTransaction(transaction)
            }

            withContext(Dispatchers.Main) {
                dismissAddTransactionSheet()
            }
        }
    }
}

private fun ContactEntity.toSummary(
    groupName: String? = null,
    tagName: String? = null,
    matches: List<SearchMatch> = emptyList()
) = ContactSummary(
    id = id,
    fullName = fullName,
    nicknames = nicknames,
    primaryPhone = sanitizedPrimaryPhone,
    phoneNumbers = phoneNumbers,
    avatarPath = avatarPath,
    thumbnailPath = thumbnailPath,
    socialProfiles = socialProfiles,
    group = groupName,
    tag = tagName,
    matches = matches
)
