package de.marquisproject.fionotes.data.notes.sources

import de.marquisproject.fionotes.data.notes.model.Note

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDAO {
    @Upsert
    suspend fun upsertNote(note: Note)
    /**
     * Upsert is a combination of insert and update
     * If the note with the same id already exists, it will be updated else it will be inserted
     * suspend is used because it is a coroutine function and we use it here because no data is returned
     * @param note: Note object to be upserted
     */

    @Insert
    suspend fun insertNote(note: Note) : Long
    /**
     * Insert a new note into the database.
     * This is not suspend, because we want to return the id of the new inserted note
     * @param note: Note object to be inserted
     */

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes_table ORDER BY lastEdited DESC")
    fun getAllNotes(): Flow<List<Note>>
    /**
     * With flow we get an observable which will notify us when there is a change in the database.
     * @return: Flow of List of Notes ordered by lastEdited in descending order
     */

    @Query("SELECT * FROM notes_table WHERE id = :noteId")
    fun getNoteById(noteId: Long): Flow<Note>

    @Query("SELECT * FROM notes_table WHERE isPinned = 1")
    fun getPinnedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY lastEdited DESC")
    fun searchNotes(searchQuery: String): Flow<List<Note>>
}