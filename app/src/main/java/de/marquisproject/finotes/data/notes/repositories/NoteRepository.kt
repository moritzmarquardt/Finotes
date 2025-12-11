package de.marquisproject.finotes.data.notes.repositories

import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.sources.NoteDatabase

class NoteRepository (
    private val noteDb: NoteDatabase
) {
    suspend fun insertNote(note: Note) : Long {
        return noteDb.dao.insertNote(note.copy(id = 0))
    }

    suspend fun updateNote(note: Note) {
        noteDb.dao.updateNote(note.prepareForUpdate())
    }

    suspend fun binNote(note: Note) {
        noteDb.dao.updateNote(note.prepareForUpdate().copy(noteStatus = NoteStatus.BINNED))
    }

    suspend fun archiveNote(note: Note) {
        noteDb.dao.updateNote(note.prepareForUpdate().copy(noteStatus = NoteStatus.ARCHIVED))
    }

    suspend fun restoreNote (note: Note) {
        noteDb.dao.updateNote(note.prepareForUpdate().copy(noteStatus = NoteStatus.ACTIVE))
    }

    suspend fun permanentlyDeleteNote(note: Note) {
        noteDb.dao.deleteNote(note)
    }

    fun fetchAllNotes() = noteDb.dao.getAllNotes()

    fun fetchNoteById(noteId: Long) = noteDb.dao.getNoteById(noteId)

    fun fetchNotesWithQuery(
        searchQuery: String?,
        isPinned: Boolean?,
        categories: List<String>?,
        noteStatus: NoteStatus?,
    ) = noteDb.dao.getNotesWithQuery(
        searchQuery = searchQuery,
        isPinned = isPinned,
        categories = categories,
        noteStatus = noteStatus,
    )

}