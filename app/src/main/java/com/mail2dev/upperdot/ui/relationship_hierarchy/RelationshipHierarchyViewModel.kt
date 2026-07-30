package com.mail2dev.upperdot.ui.relationship_hierarchy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.HierarchyRepository
import kotlinx.coroutines.flow.*

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

class RelationshipHierarchyViewModel(
    private val contactRepository: ContactRepository,
    private val hierarchyRepository: HierarchyRepository
) : ViewModel() {

    private val _expandedGroups = MutableStateFlow<Set<String>>(emptySet())
    val expandedGroups: StateFlow<Set<String>> = _expandedGroups.asStateFlow()

    val groups: StateFlow<List<HierarchyGroup>> = combine(hierarchyRepository.groups, contactRepository.allContacts) { base, contacts ->
        base.map { group ->
            group.copy(contactCount = contacts.count { it.groupName == group.name })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleGroupExpansion(groupId: String) {
        val current = _expandedGroups.value
        if (current.contains(groupId)) {
            _expandedGroups.value = current - groupId
        } else {
            _expandedGroups.value = current + groupId
        }
    }

    fun onAddTag(groupId: String, tagName: String) {
        hierarchyRepository.addTag(groupId, tagName)
    }

    fun onRenameGroup(groupId: String, newName: String) {
        hierarchyRepository.renameGroup(groupId, newName)
    }

    fun onDeleteGroup(groupId: String) {
        hierarchyRepository.deleteGroup(groupId)
        _expandedGroups.value = _expandedGroups.value - groupId
    }

    fun onRenameTag(groupId: String, tagId: String, newName: String) {
        hierarchyRepository.renameTag(groupId, tagId, newName)
    }

    fun onDeleteTag(groupId: String, tagId: String) {
        hierarchyRepository.deleteTag(groupId, tagId)
    }
}
