package de.marquisproject.fionotes.data.notes.repositories

import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.data.notes.sources.ArchiveDatabase
import de.marquisproject.fionotes.data.notes.sources.BinDatabase
import de.marquisproject.fionotes.data.notes.sources.NoteDatabase

class NoteRepository (
    private val db: NoteDatabase,
    private val archiveDb: ArchiveDatabase,
    private val binDb: BinDatabase
) {
    suspend fun upsertNote(note: Note) {
        db.dao.upsertNote(note)
    }

    suspend fun insertNote(note: Note) : Long {
        return db.dao.insertNote(note)
    }

    suspend fun deleteNote(note: Note) {
        db.dao.deleteNote(note)
        archiveDb.dao.deleteNote(note)
        binDb.dao.insertNote(note)
    }

    fun getAllNotes() = db.dao.getAllNotes()

    fun getAllArchivedNotes() = archiveDb.dao.getAllNotes()

    fun getAllDeletedNotes() = binDb.dao.getAllNotes()

    fun getNoteById(noteId: Long) = db.dao.getNoteById(noteId)

    fun getPinnedNotes() = db.dao.getPinnedNotes()

    fun searchNotes(searchQuery: String) = db.dao.searchNotes(searchQuery)

    suspend fun archiveNote(note: Note) {
        archiveDb.dao.insertNote(note)
        db.dao.deleteNote(note)
    }

    suspend fun unarchiveNote(note: Note) {
        db.dao.insertNote(note)
        archiveDb.dao.deleteNote(note)
    }

    suspend fun deleteNoteFromBin(note: Note) {
        binDb.dao.deleteNote(note)
    }

}