package de.marquisproject.fionotes.data.notes.repositories

import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.data.notes.sources.NoteDAO
import kotlinx.coroutines.flow.Flow

class NoteRepository (private val noteDao: NoteDAO) {
    suspend fun insertNote(noteModel: Note)  {
        noteDao.insertNote(noteModel)
    }

    suspend fun updateNote(noteModel: Note) {
        noteDao.updateNote(noteModel)
    }

    suspend fun deleteNotes(noteModel: Note) {
        noteDao.deleteNotes(noteModel)
    }

    fun getNotes(): Flow<List<Note>> = noteDao.getNotes()

}