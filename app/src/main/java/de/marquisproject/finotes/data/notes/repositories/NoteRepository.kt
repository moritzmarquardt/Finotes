package de.marquisproject.finotes.data.notes.repositories

import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.sources.NoteDatabase
import kotlinx.coroutines.flow.first

class NoteRepository (
    private val noteDb: NoteDatabase
) {
    suspend fun insertNote(note: Note) : Long {
        return noteDb.dao.insertNote(note.prepareForUpdate().copy(id = 0))
    }

    /**
     * Updates the note in the database and marks it as needing to be synced to the server.
     * @param note The note to update.
     */
    suspend fun updateNote(note: Note) {
        noteDb.dao.updateNote(note.prepareForUpdate())
    }

    /**
     * Change Note status to binned in the database and marks it as needing to be synced to the server.
     * @param note The note to bin.
     */
    suspend fun binNote(note: Note) {
        noteDb.dao.updateNote(note.prepareForUpdate().copy(noteStatus = NoteStatus.BINNED))
    }

    /**
     * Change Note status to archived in the database and marks it as needing to be synced to the server.
     * @param note The note to archive.
     */
    suspend fun archiveNote(note: Note) {
        noteDb.dao.updateNote(note.prepareForUpdate().copy(noteStatus = NoteStatus.ARCHIVED))
    }

    /**
     * Change Note status to active in the database and marks it as needing to be synced to the server.
     * @param note The note to restore.
     */
    suspend fun restoreNote (note: Note) {
        noteDb.dao.updateNote(note.prepareForUpdate().copy(noteStatus = NoteStatus.ACTIVE))
    }

    suspend fun restoreNoteById(id: Long) {
        val note = noteDb.dao.getNoteById(id).first()
        restoreNote(note)
    }

    /**
     * Permanently deletes the note from the database.
     * @param note The note to delete.
     */
    suspend fun permanentlyDeleteNote(note: Note) {
        // TODO() delete from server
        noteDb.dao.deleteNote(note)
    }

    /**
     * Fetches all notes from the database.
     * @return A flow of a list of notes.
     */
    fun fetchAllNotes() = noteDb.dao.getAllNotes()

    /**
     * Fetches a note by its id from the database.
     * @param noteId The id of the note to fetch.
     * @return A flow of the note.
     */
    fun fetchNoteById(noteId: Long) = noteDb.dao.getNoteById(noteId)

    /**
     * Fetches all notes from the database with the given status.
     * @param noteStatus The status of the notes to fetch.
     */
    fun fetchAllNotesByStatus(noteStatus: NoteStatus) = noteDb.dao.getAllNotesByStatus(noteStatus)



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
    ) = noteDb.dao.getNotesWithQuery(
        searchQuery = searchQuery,
        isPinned = isPinned,
        categories = categories,
        noteStatus = noteStatus,
    )

}