package de.marquisproject.finotes.data.notes.repositories

import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.sources.ArchiveDatabase
import de.marquisproject.finotes.data.notes.sources.BinDatabase
import de.marquisproject.finotes.data.notes.sources.NoteDatabase

class NoteRepository (
    private val noteDb: NoteDatabase,
    private val archiveDb: ArchiveDatabase,
    private val binDb: BinDatabase
) {
    suspend fun updateNote(note: Note) {
        noteDb.dao.updateNote(note)
    }

    suspend fun insertNote(note: Note) : Long {
        return noteDb.dao.insertNote(note)
    }

    suspend fun binNote(note: Note) {
        noteDb.dao.deleteNote(note)
        archiveDb.dao.deleteNote(note)
        binDb.dao.insertNote(note.copy(noteStatus = NoteStatus.BINNED))
    }

    suspend fun restoreNote (note: Note) {
        binDb.dao.deleteNote(note)
        noteDb.dao.insertNote(note.copy(noteStatus = NoteStatus.ACTIVE))
    }

    suspend fun deleteNoteFromBin(note: Note) {
        binDb.dao.deleteNote(note)
    }

    suspend fun archiveNote(note: Note) {
        archiveDb.dao.insertNote(note.copy(noteStatus = NoteStatus.ARCHIVED))
        noteDb.dao.deleteNote(note)
    }

    suspend fun unarchiveNote(note: Note) {
        noteDb.dao.insertNote(note.copy(noteStatus = NoteStatus.ACTIVE))
        archiveDb.dao.deleteNote(note)
    }

    fun getAllNotes() = noteDb.dao.getAllNotes()

    fun searchNotes(searchQuery: String) = noteDb.dao.getNotesWithQuery(searchQuery)

    fun getAllArchivedNotes() = archiveDb.dao.getAllNotes()

    fun getAllDeletedNotes() = binDb.dao.getAllNotes()

}