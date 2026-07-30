package com.mail2dev.upperdot.ui.connections_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    object SearchEmpty : ConnectionsUIState()
    data class Success(val contacts: List<ContactSummary>) : ConnectionsUIState()
}

class ConnectionsListViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _uiState = MutableStateFlow<ConnectionsUIState>(ConnectionsUIState.Empty)
    val uiState: StateFlow<ConnectionsUIState> = _uiState.asStateFlow()

    init {
        // Initial load
        loadContacts()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterContacts()
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        filterContacts()
    }

    private fun loadContacts() {
        // TODO: Load from Room DB
        _uiState.value = ConnectionsUIState.Empty
    }

    private fun filterContacts() {
        // TODO: Implement filtering logic
    }

    fun onDialContact(contact: ContactSummary) {
        // Triggers Intent.ACTION_DIAL
    }

    fun onAddNote(contactId: String) {
        // Navigation to New Note Bottom Sheet
    }

    fun onAddTransaction(contactId: String) {
        // Navigation to New Transaction Bottom Sheet
    }
}
