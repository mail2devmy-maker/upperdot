package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.NoteDao
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val noteCount: Flow<Int> = noteDao.getNoteCount()

    fun getNotesForContact(contactId: Long): Flow<List<NoteEntity>> = noteDao.getNotesForContact(contactId)

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity) {
        noteDao.insertNote(note)
    }

    suspend fun insertNotes(notes: List<NoteEntity>) {
        noteDao.insertNotes(notes)
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteAll() {
        noteDao.deleteAll()
    }

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)
}
