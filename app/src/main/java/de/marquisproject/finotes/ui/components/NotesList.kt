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
    notesList: List<Note>,
    selectedNotes: List<Note> = emptyList(),
    searchQuery: String = "",
    onShortClick: (Note) -> Unit = {},
    onLongClick: (Note) -> Unit = {},
){
    val pinnedNotes = notesList.filter { it.isPinned }
    val nonPinnedNotes = notesList.filterNot { it.isPinned }

    LazyVerticalStaggeredGrid(
        modifier = Modifier.padding(padding),
        columns = StaggeredGridCells.Adaptive(180.dp),
        content = {
            if (pinnedNotes.isNotEmpty()){
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Text(
                        text = "Pinned Notes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    )
                }
                items(
                    items = pinnedNotes,
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
            if (nonPinnedNotes.isNotEmpty()){
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    )
                }
                items(
                    items = nonPinnedNotes,
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