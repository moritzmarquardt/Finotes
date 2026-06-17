package de.marquisproject.finotes.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.R
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.utils.MarkdownUtils

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun OutlinedNoteCard(
    note: Note,
    searchQuery: String?,
    onClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit,
    selected: Boolean,
) {
    val colors = if (selected) {
        CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    }

    val pinnedTint = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primary
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
        colors = colors,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column {
                if (note.title.isNotBlank()) {
                    Text(
                        modifier = Modifier
                            .padding(end = 20.dp),
                        text = highlightText(AnnotatedString(note.title), searchQuery, highlightColor = MaterialTheme.colorScheme.secondaryContainer),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (note.body.isNotBlank()) {
                    val previewBody = note.body.lineSequence().take(8).joinToString("\n")
                    Text(
                        text = highlightText(MarkdownUtils.renderMarkdown(previewBody), searchQuery, highlightColor = MaterialTheme.colorScheme.secondaryContainer),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 7,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (note.isPinned) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_push_pin_24),
                    contentDescription = null,
                    tint = pinnedTint,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                )
            }
        }
    }

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NoteCard(
    note: Note,
    searchQuery: String? = null,
    selected: Boolean,
    onClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit,
) {
    OutlinedNoteCard(
        note = note,
        searchQuery = searchQuery,
        onClick = onClick,
        onLongClick = onLongClick,
        selected = selected,
    )
}


fun highlightText(text: AnnotatedString, query: String?, highlightColor: Color): AnnotatedString {
    if (query.isNullOrBlank()) return text

    val builder = AnnotatedString.Builder(text)
    val lowerCaseText = text.text.lowercase()
    val lowerCaseQuery = query.lowercase()
    var startIndex = lowerCaseText.indexOf(lowerCaseQuery)

    while (startIndex != -1) {
        builder.addStyle(
            style = SpanStyle(background = highlightColor),
            start = startIndex,
            end = startIndex + query.length
        )
        startIndex = lowerCaseText.indexOf(lowerCaseQuery, startIndex + query.length)
    }

    return builder.toAnnotatedString()
}
