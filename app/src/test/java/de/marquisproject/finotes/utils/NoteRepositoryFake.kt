package de.marquisproject.finotes.utils

import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class NoteRepositoryFake(
    initialNotes: List<Note> = emptyList()
) : NoteRepository {
    private var nextId = (initialNotes.maxOfOrNull { it.id ?: 0L } ?: 0L) + 1L
    private val notesFlow = MutableStateFlow(initialNotes)

    override suspend fun insertNote(note: Note): Long {
        val id = nextId++
        val preparedNote = note.prepareForUpdate().copy(id = id)
        notesFlow.update { current -> current + preparedNote }
        return id
    }

    override suspend fun updateNotes(notes: List<Note>) {
        val updatedMap = notes.associateBy { it.id }
        notesFlow.update { current ->
            current.map { existing ->
                updatedMap[existing.id]?.prepareForUpdate() ?: existing
            }
        }
    }

    override suspend fun binNotes(notes: List<Note>) {
        val targetIds = notes.mapNotNull { it.id }.toSet()
        notesFlow.update { current ->
            current.map { existing ->
                if (existing.id in targetIds) {
                    existing.prepareForUpdate().copy(noteStatus = NoteStatus.BINNED)
                } else {
                    existing
                }
            }
        }
    }

    override suspend fun archiveNotes(notes: List<Note>) {
        val targetIds = notes.mapNotNull { it.id }.toSet()
        notesFlow.update { current ->
            current.map { existing ->
                if (existing.id in targetIds) {
                    existing.prepareForUpdate().copy(noteStatus = NoteStatus.ARCHIVED)
                } else {
                    existing
                }
            }
        }
    }

    override suspend fun restoreNotes(notes: List<Note>) {
        val targetIds = notes.mapNotNull { it.id }.toSet()
        notesFlow.update { current ->
            current.map { existing ->
                if (existing.id in targetIds) {
                    existing.prepareForUpdate().copy(noteStatus = NoteStatus.ACTIVE)
                } else {
                    existing
                }
            }
        }
    }

    override suspend fun permanentlyDeleteNotes(notes: List<Note>) {
        val targetIds = notes.mapNotNull { it.id }.toSet()
        notesFlow.update { current ->
            current.filterNot { it.id in targetIds }
        }
    }

    override fun fetchNoteById(noteId: Long): Flow<Note> {
        return notesFlow.map { list ->
            list.find { it.id == noteId } ?: Note()
        }
    }

    override suspend fun restoreNoteById(id: Long) {
        val note = notesFlow.value.find { it.id == id }
        if (note != null) {
            restoreNotes(listOf(note))
        }
    }

    override fun fetchAllNotesByStatus(noteStatus: NoteStatus): Flow<List<Note>> {
        return notesFlow.map { list ->
            list.filter { it.noteStatus == noteStatus }
        }
    }

    override fun fetchNotesWithQuery(
        searchQuery: String?,
        isPinned: Boolean?,
        categories: List<Long>?,
        noteStatus: NoteStatus?
    ): Flow<List<Note>> {
        return notesFlow.map { list ->
            list.filter { note ->
                val matchesQuery = searchQuery.isNullOrBlank() ||
                        note.title.contains(searchQuery, ignoreCase = true) ||
                        note.body.contains(searchQuery, ignoreCase = true)
                val matchesPinned = isPinned == null || note.isPinned == isPinned
                val matchesCategory = categories.isNullOrEmpty() || note.category in categories
                val matchesStatus = noteStatus == null || note.noteStatus == noteStatus
                matchesQuery && matchesPinned && matchesCategory && matchesStatus
            }
        }
    }

    fun getAllNotes(): List<Note> = notesFlow.value
}
