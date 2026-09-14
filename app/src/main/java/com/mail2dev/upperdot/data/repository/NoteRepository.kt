package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.NoteDao
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.model.NoteWithContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allNotesWithContact: Flow<List<NoteWithContact>> = noteDao.getAllNotesWithContact()
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
        // Delete local image/file attachments and voice recording
        deleteNoteFiles(note)

        // Remove from database
        noteDao.deleteNote(note)
    }

    suspend fun deleteAll() {
        val notes = noteDao.getAllNotes().firstOrNull()
        notes?.forEach { deleteNoteFiles(it) }

        noteDao.deleteAll()
    }

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    fun searchNotesWithContact(query: String): Flow<List<NoteWithContact>> = noteDao.searchNotesWithContact(query)

    /**
     * Deletes all local physical files associated with the given NoteEntity.
     */
    private fun deleteNoteFiles(note: NoteEntity) {
        try {
            // 1. Delete all image/file attachments in attachmentPaths list
            note.attachmentPaths.forEach { path ->
                if (path.isNotBlank()) {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }

            // 2. Delete voice recording file if present
            note.voiceRecordingPath?.let { path ->
                if (path.isNotBlank()) {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}