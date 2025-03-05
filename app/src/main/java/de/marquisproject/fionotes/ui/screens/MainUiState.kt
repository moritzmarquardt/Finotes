package de.marquisproject.fionotes.ui.screens

import de.marquisproject.fionotes.data.notes.model.Note

data class MainUiState(
    val searchQuery: String = "",
    val notesList: List<Note> = emptyList(),
    val archivedList: List<Note> = emptyList(),
    val binList: List<Note> = emptyList(),
    val currentNote: Note = Note(),
    val selectedNotes: List<Note> = emptyList(),
    val inSelectionMode: Boolean = false
)