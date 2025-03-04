package de.marquisproject.fionotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.marquisproject.fionotes.data.notes.model.Note

@Composable
fun NoteCard(
    setCurrentNoteId: (Long) -> Unit,
    setCurrentNote: (Note) -> Unit,
    note: Note,
    navigate: () -> Unit,
    searchQuery: String
) {
    fun highlightText(text: String, query: String): AnnotatedString {
        if (query.isBlank()) return AnnotatedString(text)

        val lowerCaseText = text.lowercase()
        val lowerCaseQuery = query.lowercase()
        var startIndex = lowerCaseText.indexOf(lowerCaseQuery)
        if (startIndex == -1) return AnnotatedString(text)

        return buildAnnotatedString {
            var currentIndex = 0
            while (startIndex != -1) {
                append(text.substring(currentIndex, startIndex))
                withStyle(style = SpanStyle(background = Color.Yellow)) {
                    append(text.substring(startIndex, startIndex + query.length))
                }
                currentIndex = startIndex + query.length
                startIndex = lowerCaseText.indexOf(lowerCaseQuery, currentIndex)
            }
            append(text.substring(currentIndex))
        }
    }



    OutlinedCard(
        modifier = Modifier
            .padding(4.dp)
            .clickable(
                onClick = {
                    setCurrentNoteId(note.id)
                    setCurrentNote(note)
                    navigate()
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (note.title.isNotBlank()) {
                Text(
                    text = highlightText(note.title, searchQuery),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (note.body.isNotBlank()) {
                Text(
                    text = highlightText(note.body, searchQuery),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 7,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}