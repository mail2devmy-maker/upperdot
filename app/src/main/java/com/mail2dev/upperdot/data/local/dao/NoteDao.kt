package com.mail2dev.upperdot.data.local.dao

import androidx.room.*
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

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
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%' 
        OR contactId IN (SELECT id FROM contacts WHERE fullName LIKE '%' || :query || '%')
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>
}
