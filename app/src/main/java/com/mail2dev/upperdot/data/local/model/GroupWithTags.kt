package com.mail2dev.upperdot.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mail2dev.upperdot.data.local.entity.GroupEntity
import com.mail2dev.upperdot.data.local.entity.TagEntity

data class GroupWithTags(
    @Embedded val group: GroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val tags: List<TagEntity>
)
