package de.marquisproject.fionotes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.marquisproject.fionotes.NoteRoute
import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.ui.components.TopBarHome


@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    
    val uiState by viewModel.uiState.collectAsState()


    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBarHome(
                navController = navController,
                updateQuery = { viewModel.updateQuery(it) },
                searchQuery = uiState.searchQuery
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.insertNewEmptyNote()
                    navController.navigate(NoteRoute)
                },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Icon(Icons.Filled.Add, "Localized description")
            }
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            modifier = Modifier.padding(innerPadding),
            columns = StaggeredGridCells.Adaptive(200.dp),
            content = {
                items(uiState.notesList) { note ->
                    OutlinedCard(
                        modifier = Modifier
                            .padding(3.dp)
                            .clickable(
                                onClick = {
                                    viewModel.setCurrentNoteId(note.id)
                                    viewModel.setCurrentNote(note)
                                    navController.navigate(NoteRoute)
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (note.title.isNotBlank()) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (note.body.isNotBlank()) {
                                Text(
                                    text = note.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 7,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}