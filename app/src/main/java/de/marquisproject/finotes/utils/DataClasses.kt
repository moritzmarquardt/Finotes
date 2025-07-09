package de.marquisproject.finotes.utils

import de.marquisproject.finotes.data.notes.model.Note

data class NoteSection(
    val title: String,
    val notesList: List<Note>
)