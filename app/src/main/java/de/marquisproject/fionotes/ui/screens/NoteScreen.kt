package de.marquisproject.fionotes.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun NoteScreen(
    id: String
) {
    Text(text = "Note View with id: $id")
}