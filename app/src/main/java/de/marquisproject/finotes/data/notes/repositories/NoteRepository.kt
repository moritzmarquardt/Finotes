package de.marquisproject.finotes.data.notes.repositories

import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.sources.NoteDAO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface NoteRepository {
    suspend fun insertNote(note: Note): Long

    /**
     * Updates multiple notes in the database and marks them as needing to be synced.
     * @param notes The list of notes to update.
     */
    suspend fun updateNotes(notes: List<Note>)
    suspend fun binNotes(notes: List<Note>)
    suspend fun archiveNotes(notes: List<Note>)
    suspend fun restoreNotes(notes: List<Note>)

    suspend fun permanentlyDeleteNotes(notes: List<Note>)

    /**
     * Fetches a note by its id from the database.
     * @param noteId The id of the note to fetch.
     * @return A flow of the note.
     */
    fun fetchNoteById(noteId: Long): Flow<Note>

    /**
     * Restore a note by its id.
     * @param id The id of the note to restore.
     */
    suspend fun restoreNoteById(id: Long)

    /**
     * Fetches all notes from the database with the given status.
     * @param noteStatus The status of the notes to fetch.
     */
    fun fetchAllNotesByStatus(noteStatus: NoteStatus): Flow<List<Note>>

    /**
     * Fetches notes from the database with the given query.
     * @param searchQuery The query to search for.
     * @param isPinned Whether to search for pinned notes.
     * @param categories The categories to search for.
     * @param noteStatus The status of the notes to search for.
     * @return A flow of a list of notes.
     */
    fun fetchNotesWithQuery(
        searchQuery: String?,
        isPinned: Boolean?,
        categories: List<Long>?,
        noteStatus: NoteStatus?,
    ): Flow<List<Note>>
}