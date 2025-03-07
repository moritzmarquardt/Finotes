package de.marquisproject.finotes.data.notes.sources

import de.marquisproject.finotes.data.notes.model.Note

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow


@Dao
interface ArchiveDAO {
    @Insert
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes_table ORDER BY dateCreated DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertListOfNotes(notes: List<Note>)
}