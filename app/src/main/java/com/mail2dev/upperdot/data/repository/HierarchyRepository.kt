package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.ContactDao
import com.mail2dev.upperdot.data.local.dao.GroupDao
import com.mail2dev.upperdot.data.local.entity.GroupEntity
import com.mail2dev.upperdot.data.local.entity.TagEntity
import com.mail2dev.upperdot.ui.relationship_hierarchy.HierarchyGroup
import com.mail2dev.upperdot.ui.relationship_hierarchy.HierarchyTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class HierarchyRepository(
    private val groupDao: GroupDao,
    private val contactDao: ContactDao
) {
    val groups: Flow<List<HierarchyGroup>> = groupDao.getGroupsWithTags()
        .onStart { seedDefaultsIfEmpty() }
        .map { list ->
            list.map { gwt ->
                HierarchyGroup(
                    id = gwt.group.id,
                    name = gwt.group.name,
                    contactCount = 0,
                    tags = gwt.tags.map { HierarchyTag(it.id, it.name) }
                )
            }
        }

    suspend fun seedDefaultsIfEmpty() {
        if (groupDao.getGroupCount() == 0) {
            val defaultGroups = listOf(
                GroupEntity("fav", "Favorites", 0),
                GroupEntity("fam", "Family", 1),
                GroupEntity("wrk", "Work", 2),
                GroupEntity("ven", "Vendor", 3),
                GroupEntity("una", "Unassigned", 4)
            )
            val defaultTags = listOf(
                TagEntity("hp", "fav", "High Priority"),
                TagEntity("fq", "fav", "Frequent"),
                TagEntity("nt", "fav", "new tag under favorite")
            )
            groupDao.insertGroups(defaultGroups)
            groupDao.insertTags(defaultTags)
        }
    }

    suspend fun addGroup(name: String) {
        val newGroup = GroupEntity(
            id = System.currentTimeMillis().toString(),
            name = name,
            sortOrder = (System.currentTimeMillis() % 100000).toInt()
        )
        groupDao.insertGroup(newGroup)
    }

    suspend fun addTag(groupId: String, tagName: String) {
        val newTag = TagEntity(
            id = System.currentTimeMillis().toString(),
            groupId = groupId,
            name = tagName
        )
        groupDao.insertTag(newTag)
    }

    suspend fun renameGroup(groupId: String, newName: String) {
        groupDao.renameGroup(groupId, newName)
    }

    suspend fun deleteGroup(groupId: String) {
        groupDao.deleteTagsForGroup(groupId)
        groupDao.deleteGroup(groupId)
        contactDao.resetGroupIdForContacts(groupId)
    }

    suspend fun renameTag(groupId: String, tagId: String, newName: String) {
        groupDao.renameTag(tagId, newName)
    }

    suspend fun deleteTag(groupId: String, tagId: String) {
        groupDao.deleteTag(tagId)
        contactDao.resetTagIdForContacts(tagId)
    }

    suspend fun getAllGroupsList(): List<GroupEntity> = groupDao.getAllGroupsList()
    suspend fun getAllTagsList(): List<TagEntity> = groupDao.getAllTagsList()
    suspend fun insertGroups(groups: List<GroupEntity>) = groupDao.insertGroups(groups)
    suspend fun insertTags(tags: List<TagEntity>) = groupDao.insertTags(tags)
    suspend fun deleteAll() {
        groupDao.deleteAllTags()
        groupDao.deleteAllGroups()
    }
}
