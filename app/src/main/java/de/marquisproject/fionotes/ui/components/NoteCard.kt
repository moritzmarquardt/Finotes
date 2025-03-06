package de.marquisproject.fionotes.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.marquisproject.fionotes.R
import de.marquisproject.fionotes.data.notes.model.Note

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OutlinedNoteCard(
    note: Note,
    searchQuery: String,
    onClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit,
    selected: Boolean
)  {
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

    val colors = if (selected) {
        CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        CardDefaults.outlinedCardColors()
    }
    OutlinedCard(
        modifier = Modifier
            .padding(4.dp)
            .combinedClickable(
                onClick = {
                    onClick(note)
                },
                onLongClick = {
                    onLongClick(note)
                }
            ),
        colors = colors
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            if (note.isPinned){
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_push_pin_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                )
            }
            Column{
                if (note.title.isNotBlank()) {
                    Text(
                        modifier = Modifier.padding(end = 20.dp),
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
}

@Composable
fun NoteCard(
    note: Note,
    searchQuery: String,
    selected: Boolean,
    onClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit,
    onSwipe: ((Note) -> Unit)? = null,
    swipeIcon: Painter = painterResource(id = R.drawable.outline_star_outline_24),
) {
    if (onSwipe != null) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.StartToEnd) {
                    onSwipe(note)
                    true
                } else {
                    false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier,
            backgroundContent = {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Row(modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(swipeIcon, contentDescription = "delete")
                    }
                }
            },
            enableDismissFromEndToStart = false,
            content = {
                OutlinedNoteCard(
                    note = note,
                    searchQuery = searchQuery,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    selected = selected
                )
            }
        )
    } else {
        OutlinedNoteCard(
            note = note,
            searchQuery = searchQuery,
            onClick = onClick,
            onLongClick = onLongClick,
            selected = selected
        )
    }
}