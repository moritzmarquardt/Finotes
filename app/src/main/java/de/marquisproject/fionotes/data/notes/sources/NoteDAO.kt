package de.marquisproject.fionotes.data.notes.sources

import androidx.lifecycle.LiveData
import de.marquisproject.fionotes.data.notes.model.Note

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDAO {
    @Query("SELECT * FROM notes_table")
    fun getNotes(): Flow<List<Note>>
    //flow is better than Live view because it updates if the underlying data changes

    @Query("SELECT * FROM notes_table")
    fun getPlainNotes(): List<Note>

    @Query("SELECT * FROM notes_table WHERE id = :id")
    fun getNoteDetail(id: Int): Flow<Note>

    @Query("SELECT * FROM notes_table WHERE id = :id")
    fun getPlainNoteDetail(id: Int): Note

    @Update
    fun updateNote(vararg notes: Note): Int

    @Delete
    fun deleteNotes(vararg notes: Note): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: Note): Long
}