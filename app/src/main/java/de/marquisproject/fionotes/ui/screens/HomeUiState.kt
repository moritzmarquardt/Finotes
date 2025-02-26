package de.marquisproject.fionotes.ui.screens

import de.marquisproject.fionotes.data.notes.model.Note

data class HomeUiState(
    val searchQuery: String = "",
    val notes: List<Note> = emptyList(),
    val selectedNotesIds: List<String> = emptyList()
)