package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
    LazyVerticalStaggeredGrid(
        modifier = Modifier.padding(padding),
        columns = StaggeredGridCells.Adaptive(180.dp),
        content = {
            items(
                items = notesList,
                key = { note -> note.id }
            ) { note ->
                NoteCard(
                    note = note,
                    searchQuery = searchQuery,
                    selected = selectedNotes.contains(note),
                    onClick = {
                        onShortClick(note)
                    },
                    onLongClick = {
                        onLongClick(note)
                    },
                )
            }
        }
    )
}