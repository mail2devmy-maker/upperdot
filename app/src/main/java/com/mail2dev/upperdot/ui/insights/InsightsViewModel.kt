package com.mail2dev.upperdot.ui.insights

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class InsightsViewModel : ViewModel() {

    private val _selectedTab = MutableStateFlow(InsightTab.NOTES)
    val selectedTab: StateFlow<InsightTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedContactFilter = MutableStateFlow<String?>(null)
    val selectedContactFilter: StateFlow<String?> = _selectedContactFilter.asStateFlow()

    private val _notes = MutableStateFlow<List<NoteEntry>>(
        listOf(
            NoteEntry(
                id = "1",
                contactId = "test_id",
                contactName = "test",
                title = "test note",
                content = "note content",
                timestamp = "Jul 28, 2026 • 02:28 AM",
                attachmentCount = 1
            )
        )
    )
    val notes: StateFlow<List<NoteEntry>> = _notes.asStateFlow()

    fun onTabSelected(tab: InsightTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onContactFilterSelected(contactName: String) {
        _selectedContactFilter.value = contactName
    }

    fun clearFilters() {
        _selectedContactFilter.value = null
    }

    fun onAddNoteClicked() {
        // Trigger Bottom Sheet
    }

    fun onAddTransactionClicked() {
        // Trigger Bottom Sheet
    }
}
