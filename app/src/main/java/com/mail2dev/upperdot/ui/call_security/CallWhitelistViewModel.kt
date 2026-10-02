package com.mail2dev.upperdot.ui.call_security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CallWhitelistViewModel(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val contacts = combine(
        contactRepository.allContacts,
        _searchQuery
    ) { allContacts, query ->
        if (query.isBlank()) {
            allContacts
        } else {
            allContacts.filter { 
                it.fullName.contains(query, ignoreCase = true) ||
                it.phoneNumbers.any { num -> num.contains(query) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedCount = contactRepository.whitelistedContacts.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleWhitelistStatus(contact: ContactEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.updateWhitelistStatus(contact.id, !contact.isWhitelisted)
        }
    }
}
