package com.mail2dev.upperdot.data.local.dao

import androidx.room.*
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.model.NoteWithContact
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("""
        SELECT notes.*, contacts.fullName as contactName 
        FROM notes 
        INNER JOIN contacts ON notes.contactId = contacts.id 
        ORDER BY notes.createdAt DESC
    """)
    fun getAllNotesWithContact(): Flow<List<NoteWithContact>>

    @Query("SELECT * FROM notes WHERE contactId = :contactId ORDER BY createdAt DESC")
    fun getNotesForContact(contactId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM notes")
    fun getNoteCount(): Flow<Int>

    @Query("""
        SELECT notes.*, contacts.fullName as contactName 
        FROM notes 
        INNER JOIN contacts ON notes.contactId = contacts.id 
        WHERE notes.title LIKE '%' || :query || '%' 
        OR notes.content LIKE '%' || :query || '%' 
        OR contacts.fullName LIKE '%' || :query || '%'
        ORDER BY notes.createdAt DESC
    """)
    fun searchNotesWithContact(query: String): Flow<List<NoteWithContact>>

    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%' 
        OR contactId IN (SELECT id FROM contacts WHERE fullName LIKE '%' || :query || '%')
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>
}
