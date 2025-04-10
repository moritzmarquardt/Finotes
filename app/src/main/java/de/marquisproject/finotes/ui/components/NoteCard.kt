package de.marquisproject.finotes.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import de.marquisproject.finotes.R
import de.marquisproject.finotes.data.notes.model.Note
import kotlin.math.abs

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
            Column {
                if (note.title.isNotBlank()) {
                    Text(
                        modifier = Modifier
                            .padding(end = 20.dp),
                        text = highlightText(note.title, searchQuery, highlightColor = MaterialTheme.colorScheme.secondaryContainer),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (note.body.isNotBlank()) {
                    Text(
                        text = highlightText(note.body, searchQuery, highlightColor = MaterialTheme.colorScheme.secondaryContainer),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 7,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
    onSwipe: ((Note) -> Unit)? = null,
    swipeIcon: Painter = painterResource(id = R.drawable.outline_star_outline_24),
) {
    if (onSwipe != null) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                value == SwipeToDismissBoxValue.StartToEnd
            },
        )

        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                onSwipe(note)
            }
        }

        val init = try {
            dismissState.requireOffset()
        } catch (e: Exception) {
            0f // or any default value you prefer
        }
        val alpha = if (dismissState.progress == 1.0f) {
            1f - init
        } else {
            1f - 2 * abs(dismissState.progress)
        }

        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.graphicsLayer { this.alpha = alpha },
            backgroundContent = {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
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
                    selected = selected,
                )
            }
        )
    } else {

        OutlinedNoteCard(
            note = note,
            searchQuery = searchQuery,
            onClick = onClick,
            onLongClick = onLongClick,
            selected = selected,
        )
    }
}


fun highlightText(text: String, query: String?, highlightColor: Color): AnnotatedString {
    if (query.isNullOrBlank()) return AnnotatedString(text)

    val lowerCaseText = text.lowercase()
    val lowerCaseQuery = query.lowercase()
    var startIndex = lowerCaseText.indexOf(lowerCaseQuery)
    if (startIndex == -1) return AnnotatedString(text)

    return buildAnnotatedString {
        var currentIndex = 0
        while (startIndex != -1) {
            append(text.substring(currentIndex, startIndex))
            withStyle(style = SpanStyle(background = highlightColor)) {
                append(text.substring(startIndex, startIndex + query.length))
            }
            currentIndex = startIndex + query.length
            startIndex = lowerCaseText.indexOf(lowerCaseQuery, currentIndex)
        }
        append(text.substring(currentIndex))
    }
}