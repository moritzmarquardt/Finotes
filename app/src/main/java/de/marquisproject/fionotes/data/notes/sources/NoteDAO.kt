package de.marquisproject.fionotes.data.notes.sources

import de.marquisproject.fionotes.data.notes.model.Note

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDAO {
    @Upsert
    suspend fun upsertNote(note: Note)
    // upsert is update if note with id already exists, insert otherwise
    // suspend, so it works with coroutines
    // can use it here because no data is returned

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes_table")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE isPinned = 1")
    fun getPinnedNotes(): Flow<List<Note>>
    // with flow we get an observable which will notify us when there is a change in the database

    @Query("SELECT * FROM notes_table ORDER BY lastEdited DESC")
    fun getNotesOrderedByDate(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE title LIKE :searchQuery OR content LIKE :searchQuery")
    fun searchNotes(searchQuery: String): Flow<List<Note>>
}