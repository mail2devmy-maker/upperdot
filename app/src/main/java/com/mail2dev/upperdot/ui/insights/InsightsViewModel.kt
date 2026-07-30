package com.mail2dev.upperdot.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

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

    private val _transactions = MutableStateFlow<List<TransactionEntry>>(
        listOf(
            TransactionEntry(
                id = "1",
                contactId = "test_id",
                contactName = "test",
                title = "payment",
                detail = "shop 1",
                amount = "10.00",
                isRevenue = false,
                timestamp = "Jul 28, 2026 • 11:28 AM",
                attachmentCount = 1
            ),
            TransactionEntry(
                id = "2",
                contactId = "test_id",
                contactName = "test",
                title = "income",
                detail = "gift",
                amount = "20.00",
                isRevenue = true,
                timestamp = "Jul 22, 2026 • 12:29 AM",
                attachmentCount = 1
            )
        )
    )
    val transactions: StateFlow<List<TransactionEntry>> = _transactions.asStateFlow()

    val totalRevenue = _transactions.map { list ->
        list.filter { it.isRevenue }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val totalExpenses = _transactions.map { list ->
        list.filter { !it.isRevenue }.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val netProfit = combine(totalRevenue, totalExpenses) { rev, exp ->
        rev - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

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
