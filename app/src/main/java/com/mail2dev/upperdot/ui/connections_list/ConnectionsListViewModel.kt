package com.mail2dev.upperdot.ui.connections_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class ContactSummary(
    val id: String,
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

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionsListViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val uiState: StateFlow<ConnectionsUIState> = combine(_searchQuery, _selectedFilter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        if (query.isEmpty()) {
            repository.allContacts
        } else {
            repository.searchContacts(query)
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

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }

    fun onDialContact(contact: ContactSummary) {
        // Implementation remains in UI via Intent
    }

    fun onAddNote(contactId: String) {
        // Implementation remains in UI navigation
    }

    fun onAddTransaction(contactId: String) {
        // Implementation remains in UI navigation
    }
}

private fun ContactEntity.toSummary() = ContactSummary(
    id = id.toString(),
    fullName = fullName,
    nicknames = nicknames,
    primaryPhone = sanitizedPrimaryPhone,
    group = groupName,
    tag = tagName
)
