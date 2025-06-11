package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.data.notes.model.Note

@Composable
fun NotesList(
    padding: PaddingValues = PaddingValues(0.dp),
    title: String = "",
    notesList: List<Note>,
    title2: String = "",
    notesList2: List<Note> = emptyList(),
    selectedNotes: List<Note> = emptyList(),
    searchQuery: String = "",
    onShortClick: (Note) -> Unit = {},
    onLongClick: (Note) -> Unit = {},
){
    LazyVerticalStaggeredGrid(
        modifier = Modifier.padding(padding),
        columns = StaggeredGridCells.Adaptive(180.dp),
        content = {
            if (notesList.isNotEmpty()){
                if (title.isNotEmpty()) {
                    item(
                        span = StaggeredGridItemSpan.FullLine
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        )
                    }
                }
                items(
                    items = notesList,
                    key = { note -> note.id }
                ) { note ->
                    NoteCard(
                        note = note,
                        searchQuery = searchQuery,
                        selected = selectedNotes.contains(note),
                        onClick = { onShortClick(note) },
                        onLongClick = { onLongClick(note) },
                    )
                }
            }
            if (notesList2.isNotEmpty()){
                if (title2.isNotEmpty()) {
                    item(
                        span = StaggeredGridItemSpan.FullLine
                    ) {
                        Text(
                            text = title2,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 12.dp)
                        )
                    }
                }
                items(
                    items = notesList2,
                    key = { note -> "archived_${note.id}" }
                ) { note ->
                    // Insert separator before the first unpinned note
                    NoteCard(
                        note = note,
                        searchQuery = searchQuery,
                        selected = selectedNotes.contains(note),
                        onClick = { onShortClick(note) },
                        onLongClick = { onLongClick(note) },
                    )
                }
            }
        }
    )
}