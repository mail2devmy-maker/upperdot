package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.ui.relationship_hierarchy.HierarchyGroup
import com.mail2dev.upperdot.ui.relationship_hierarchy.HierarchyTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HierarchyRepository {
    private val _groups = MutableStateFlow<List<HierarchyGroup>>(
        listOf(
            HierarchyGroup(
                id = "fav",
                name = "Favorites",
                contactCount = 0,
                tags = listOf(
                    HierarchyTag("hp", "High Priority"),
                    HierarchyTag("fq", "Frequent"),
                    HierarchyTag("nt", "new tag under favorite")
                )
            ),
            HierarchyGroup(id = "fam", name = "Family", contactCount = 0),
            HierarchyGroup(id = "wrk", name = "Work", contactCount = 0),
            HierarchyGroup(id = "ven", name = "Vendor", contactCount = 0),
            HierarchyGroup(id = "una", name = "Unassigned", contactCount = 0)
        )
    )
    val groups: StateFlow<List<HierarchyGroup>> = _groups.asStateFlow()

    fun addGroup(name: String) {
        val newGroup = HierarchyGroup(
            id = System.currentTimeMillis().toString(),
            name = name,
            contactCount = 0
        )
        _groups.value = _groups.value + newGroup
    }

    fun addTag(groupId: String, tagName: String) {
        val currentGroups = _groups.value.toMutableList()
        val index = currentGroups.indexOfFirst { it.id == groupId }
        if (index != -1) {
            val group = currentGroups[index]
            val newTag = HierarchyTag(id = System.currentTimeMillis().toString(), name = tagName)
            currentGroups[index] = group.copy(tags = group.tags + newTag)
            _groups.value = currentGroups
        }
    }

    fun renameGroup(groupId: String, newName: String) {
        val currentGroups = _groups.value.toMutableList()
        val index = currentGroups.indexOfFirst { it.id == groupId }
        if (index != -1) {
            currentGroups[index] = currentGroups[index].copy(name = newName)
            _groups.value = currentGroups
        }
    }

    fun deleteGroup(groupId: String) {
        _groups.value = _groups.value.filter { it.id != groupId }
    }

    fun renameTag(groupId: String, tagId: String, newName: String) {
        val currentGroups = _groups.value.toMutableList()
        val index = currentGroups.indexOfFirst { it.id == groupId }
        if (index != -1) {
            val group = currentGroups[index]
            val tags = group.tags.toMutableList()
            val tagIndex = tags.indexOfFirst { it.id == tagId }
            if (tagIndex != -1) {
                tags[tagIndex] = tags[tagIndex].copy(name = newName)
                currentGroups[index] = group.copy(tags = tags)
                _groups.value = currentGroups
            }
        }
    }

    fun deleteTag(groupId: String, tagId: String) {
        val currentGroups = _groups.value.toMutableList()
        val index = currentGroups.indexOfFirst { it.id == groupId }
        if (index != -1) {
            val group = currentGroups[index]
            currentGroups[index] = group.copy(tags = group.tags.filter { it.id != tagId })
            _groups.value = currentGroups
        }
    }
}
