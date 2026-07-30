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
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.id == groupId }
        if (groupIndex != -1) {
            val group = currentGroups[groupIndex]
            val newTag = HierarchyTag(id = System.currentTimeMillis().toString(), name = tagName)
            currentGroups[groupIndex] = group.copy(tags = group.tags + newTag)
            _groups.value = currentGroups
        }
    }

    fun onRenameGroup(groupId: String, newName: String) {
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.id == groupId }
        if (groupIndex != -1) {
            currentGroups[groupIndex] = currentGroups[groupIndex].copy(name = newName)
            _groups.value = currentGroups
        }
    }

    fun onDeleteGroup(groupId: String) {
        _groups.value = _groups.value.filter { it.id != groupId }
        _expandedGroups.value = _expandedGroups.value - groupId
    }

    fun onRenameTag(groupId: String, tagId: String, newName: String) {
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.id == groupId }
        if (groupIndex != -1) {
            val group = currentGroups[groupIndex]
            val currentTags = group.tags.toMutableList()
            val tagIndex = currentTags.indexOfFirst { it.id == tagId }
            if (tagIndex != -1) {
                currentTags[tagIndex] = currentTags[tagIndex].copy(name = newName)
                currentGroups[groupIndex] = group.copy(tags = currentTags)
                _groups.value = currentGroups
            }
        }
    }

    fun onDeleteTag(groupId: String, tagId: String) {
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.id == groupId }
        if (groupIndex != -1) {
            val group = currentGroups[groupIndex]
            currentGroups[groupIndex] = group.copy(tags = group.tags.filter { it.id != tagId })
            _groups.value = currentGroups
        }
    }
}
