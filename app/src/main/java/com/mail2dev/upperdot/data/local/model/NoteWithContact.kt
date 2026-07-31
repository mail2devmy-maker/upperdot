package com.mail2dev.upperdot.data.local.model

import androidx.room.Embedded
import com.mail2dev.upperdot.data.local.entity.NoteEntity

data class NoteWithContact(
    @Embedded val note: NoteEntity,
    val contactName: String
)
