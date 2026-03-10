package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.utils.NoteSection

@Composable
fun NotesList(
    padding: PaddingValues = PaddingValues(0.dp),
    noteSections: List<NoteSection> = emptyList(),
    inSelectionMode: Boolean = false,
    selectedNotes: List<Note> = emptyList(),
    searchQuery: String = "",
    onShortClick: (Note) -> Unit = {},
    onLongClick: (Note) -> Unit = {},
){
    LazyVerticalStaggeredGrid(
        modifier = Modifier.padding(padding),
        columns = StaggeredGridCells.Adaptive(180.dp),
        content = {
            noteSections.forEach { section ->
                if (section.notesList.isNotEmpty()){
                    if (section.title.isNotEmpty()) {
                        item(
                            span = StaggeredGridItemSpan.FullLine
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal =  if (inSelectionMode) 0.dp else 12.dp)
                                    .height(48.dp)
                            ) {
                                if (inSelectionMode) {
                                    Checkbox(
                                        enabled = true,
                                        checked = section.isSectionSelected,
                                        onCheckedChange = { section.onSelectSection() },
                                        modifier = Modifier.padding(0.dp)
                                    )
                                }
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                    items(
                        items = section.notesList,
                        key = { note -> requireNotNull(note.id) }
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
            }
        }
    )
}