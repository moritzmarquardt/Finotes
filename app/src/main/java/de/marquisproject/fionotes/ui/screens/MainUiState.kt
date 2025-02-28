package de.marquisproject.fionotes.ui.screens

import de.marquisproject.fionotes.data.notes.model.Note

data class MainUiState(
    val searchQuery: String = "",
    val filter: String = "all",
    val notesList: List<Note> = emptyList(),
)