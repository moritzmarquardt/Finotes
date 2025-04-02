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
        return noteDb.dao.insertNote(note.copy(id = 0))
    }

    suspend fun binNote(note: Note) {
        if (note.noteStatus == NoteStatus.ACTIVE) {
            noteDb.dao.deleteNote(note)
        } else if (note.noteStatus == NoteStatus.ARCHIVED) {
            archiveDb.dao.deleteNote(note)
        }
        binDb.dao.insertNote(note.copy(id = 0, noteStatus = NoteStatus.BINNED))
    }

    suspend fun restoreNote (note: Note) {
        binDb.dao.deleteNote(note)
        noteDb.dao.insertNote(note.copy(id = 0, noteStatus = NoteStatus.ACTIVE))
    }

    suspend fun deleteNoteFromBin(note: Note) {
        binDb.dao.deleteNote(note)
    }

    suspend fun archiveNote(note: Note) {
        archiveDb.dao.insertNote(note.copy(id = 0, noteStatus = NoteStatus.ARCHIVED))
        noteDb.dao.deleteNote(note)
    }

    suspend fun unarchiveNote(note: Note) {
        noteDb.dao.insertNote(note.copy(id = 0, noteStatus = NoteStatus.ACTIVE))
        archiveDb.dao.deleteNote(note)
    }

    fun fetchAllNotes() = noteDb.dao.getAllNotes()

    fun fetchNotesWithQuery(searchQuery: String) = noteDb.dao.getNotesWithQuery(searchQuery)

    fun fetchAllArchivedNotes() = archiveDb.dao.getAllNotes()

    fun fetchAllDeletedNotes() = binDb.dao.getAllNotes()


    fun insertNotes(notes: List<Note>) {
        // make all note ids 0 to insert them as new notes and avoid conflicts
        val zeroIdNotes = notes.map { it.copy(id = 0) }
        noteDb.dao.insertListOfNotes(zeroIdNotes)
    }

    fun insertNotesToArchive(archivedNotes: List<Note>) {
        val zeroIdNotes = archivedNotes.map { it.copy(id = 0) }
        archiveDb.dao.insertListOfNotes(zeroIdNotes)
    }

}