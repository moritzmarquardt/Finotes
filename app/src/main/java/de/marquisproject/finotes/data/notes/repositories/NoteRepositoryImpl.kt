package de.marquisproject.finotes.data.notes.repositories

import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.sources.NoteDAO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NoteRepositoryImpl(
    private val noteDao: NoteDAO
) : NoteRepository {
    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note.prepareForUpdate().copy(id = null))
    }

    /**
     * Updates multiple notes in the database and marks them as needing to be synced.
     * @param notes The list of notes to update.
     */
    override suspend fun updateNotes(notes: List<Note>) {
        noteDao.updateNotes(notes.map { it.prepareForUpdate() })
    }

    override suspend fun binNotes(notes: List<Note>) {
        updateNotes(notes.map { it.prepareForUpdate().copy(noteStatus = NoteStatus.BINNED) })
    }

    override suspend fun archiveNotes(notes: List<Note>) {
        updateNotes(notes.map { it.prepareForUpdate().copy(noteStatus = NoteStatus.ARCHIVED) })
    }

    override suspend fun restoreNotes(notes: List<Note>) {
        updateNotes(notes.map { it.prepareForUpdate().copy(noteStatus = NoteStatus.ACTIVE) })
    }

    override suspend fun permanentlyDeleteNotes(notes: List<Note>) {
        //TODO Delete note from server
        noteDao.deleteNotes(notes)
    }

    /**
     * Fetches a note by its id from the database.
     * @param noteId The id of the note to fetch.
     * @return A flow of the note.
     */
    override fun fetchNoteById(noteId: Long): Flow<Note> = noteDao.getNoteById(noteId)

    /**
     * Restore a note by its id.
     * @param id The id of the note to restore.
     */
    override suspend fun restoreNoteById(id: Long) {
        val note = noteDao.getNoteById(id).first()
        restoreNotes(listOf(note))
    }

    /**
     * Fetches all notes from the database with the given status.
     * @param noteStatus The status of the notes to fetch.
     */
    override fun fetchAllNotesByStatus(noteStatus: NoteStatus): Flow<List<Note>> =
        noteDao.getAllNotesByStatus(noteStatus)

    /**
     * Fetches notes from the database with the given query.
     * @param searchQuery The query to search for.
     * @param isPinned Whether to search for pinned notes.
     * @param categories The categories to search for.
     * @param noteStatus The status of the notes to search for.
     * @return A flow of a list of notes.
     */
    override fun fetchNotesWithQuery(
        searchQuery: String?,
        isPinned: Boolean?,
        categories: List<Long>?,
        noteStatus: NoteStatus?,
    ): Flow<List<Note>> = noteDao.getNotesWithQuery(
        searchQuery = searchQuery,
        isPinned = isPinned,
        categories = categories,
        noteStatus = noteStatus,
    )
}