package de.marquisproject.finotes.ui.viewmodels

import de.marquisproject.finotes.data.notes.model.Note

data class MainUiState(
    val searchQuery: String = "",
    val notesList: List<Note> = emptyList(),
    val archivedList: List<Note> = emptyList(),
    val binList: List<Note> = emptyList(),
    val currentNote: Note = Note(),
    val selectedNotes: List<Note> = emptyList(),
    val inSelectionMode: Boolean = false,
    val currentNoteIsNeverEdited: Boolean = false,
)