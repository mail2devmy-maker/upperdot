package com.mail2dev.upperdot.ui.relationship_hierarchy

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HierarchyTag(
    val id: String,
    val name: String
)

data class HierarchyGroup(
    val id: String,
    val name: String,
    val contactCount: Int,
    val tags: List<HierarchyTag> = emptyList()
)

class RelationshipHierarchyViewModel : ViewModel() {

    private val _expandedGroups = MutableStateFlow<Set<String>>(emptySet())
    val expandedGroups: StateFlow<Set<String>> = _expandedGroups.asStateFlow()

    private val _groups = MutableStateFlow<List<HierarchyGroup>>(
        listOf(
            HierarchyGroup(
                id = "fav",
                name = "Favorites",
                contactCount = 3,
                tags = listOf(
                    HierarchyTag("hp", "High Priority"),
                    HierarchyTag("fq", "Frequent"),
                    HierarchyTag("nt", "new tag under favorite")
                )
            ),
            HierarchyGroup(id = "fam", name = "Family", contactCount = 2),
            HierarchyGroup(id = "wrk", name = "Work", contactCount = 3),
            HierarchyGroup(id = "ven", name = "Vendor", contactCount = 2),
            HierarchyGroup(id = "una", name = "Unassigned", contactCount = 0)
        )
    )
    val groups: StateFlow<List<HierarchyGroup>> = _groups.asStateFlow()

    fun toggleGroupExpansion(groupId: String) {
        val current = _expandedGroups.value
        if (current.contains(groupId)) {
            _expandedGroups.value = current - groupId
        } else {
            _expandedGroups.value = current + groupId
        }
    }

    fun onAddTag(groupId: String, tagName: String) {
        // TODO: Save to Room
    }

    fun onRenameGroup(groupId: String, newName: String) {
        // TODO: Update in Room
    }

    fun onDeleteGroup(groupId: String) {
        // TODO: Delete from Room
    }
}
