package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.NoteDao
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val noteCount: Flow<Int> = noteDao.getNoteCount()

    fun getNotesForContact(contactId: String): Flow<List<NoteEntity>> = noteDao.getNotesForContact(contactId)

    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)
}
