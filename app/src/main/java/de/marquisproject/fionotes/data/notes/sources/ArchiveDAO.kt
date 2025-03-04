package de.marquisproject.fionotes.data.notes.sources

import de.marquisproject.fionotes.data.notes.model.Note

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow


@Dao
interface ArchiveDAO {
    @Insert
    suspend fun insertNote(note: Note) : Long

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes_table ORDER BY dateCreated DESC")
    fun getAllNotes(): Flow<List<Note>>
}