package com.mail2dev.upperdot.data.local.dao

import androidx.room.*
import com.mail2dev.upperdot.data.local.entity.GroupEntity
import com.mail2dev.upperdot.data.local.entity.TagEntity
import com.mail2dev.upperdot.data.local.model.GroupWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Transaction
    @Query("SELECT * FROM contact_groups ORDER BY sortOrder ASC, name ASC")
    fun getGroupsWithTags(): Flow<List<GroupWithTags>>

    @Query("SELECT * FROM contact_groups ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllGroupsList(): List<GroupEntity>

    @Query("SELECT * FROM contact_tags")
    suspend fun getAllTagsList(): List<TagEntity>

    @Query("SELECT COUNT(*) FROM contact_groups")
    suspend fun getGroupCount(): Int

    @Query("SELECT * FROM contact_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Query("UPDATE contact_groups SET name = :newName WHERE id = :groupId")
    suspend fun renameGroup(groupId: String, newName: String)

    @Query("DELETE FROM contact_groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    @Query("DELETE FROM contact_tags WHERE groupId = :groupId")
    suspend fun deleteTagsForGroup(groupId: String)

    @Query("UPDATE contact_tags SET name = :newName WHERE id = :tagId")
    suspend fun renameTag(tagId: String, newName: String)

    @Query("DELETE FROM contact_tags WHERE id = :tagId")
    suspend fun deleteTag(tagId: String)

    @Query("DELETE FROM contact_groups")
    suspend fun deleteAllGroups()

    @Query("DELETE FROM contact_tags")
    suspend fun deleteAllTags()
}
