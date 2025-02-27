package de.marquisproject.fionotes.data.notes.repositories

import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.data.notes.sources.NoteLocalSource
import kotlinx.coroutines.flow.Flow

class NoteRepository (private val noteLocalSource: NoteLocalSource) {
    val allNotes: Flow<List<Note>> = noteLocalSource.getAllNotes()

    fun insert(note: Note) {
        noteLocalSource.insertNote(note)
    }

    fun update(note: Note) {
        noteLocalSource.updateNote(note)
    }

    fun delete(note: Note) {
        noteLocalSource.deleteNote(note)
    }
}