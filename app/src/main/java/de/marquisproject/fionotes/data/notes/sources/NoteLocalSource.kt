package de.marquisproject.fionotes.data.notes.sources

import de.marquisproject.fionotes.data.notes.model.Note
import kotlinx.coroutines.flow.Flow

class NoteLocalSource(private val noteDAO: NoteDAO) {
    fun insertNote(noteModel: Note) {
        noteDAO.insertNote(noteModel)
    }

    fun updateNote(noteModel: Note) {
        noteDAO.updateNote(noteModel)
    }

    fun deleteNote(noteModel: Note) {
        noteDAO.deleteNotes(noteModel)
    }

    fun getAllNotes(): Flow<List<Note>> {
        return noteDAO.getNotes()
    }
}