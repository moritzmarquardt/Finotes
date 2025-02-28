package de.marquisproject.fionotes.data.notes.repositories

import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.data.notes.sources.NoteDatabase
import kotlinx.coroutines.flow.Flow

class NoteRepository (private val db: NoteDatabase) {
    suspend fun upsertNote(note: Note) {
        db.dao.upsertNote(note)
    }

    suspend fun deleteNote(note: Note) {
        db.dao.upsertNote(note)
    }

    fun getAllNotes() = db.dao.getAllNotes()

    fun getNoteById(noteId: Long) = db.dao.getNoteById(noteId)

    fun getPinnedNotes() = db.dao.getPinnedNotes()

    fun searchNotes(searchQuery: String) = db.dao.searchNotes(searchQuery)

}