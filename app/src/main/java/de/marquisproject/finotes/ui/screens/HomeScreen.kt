package de.marquisproject.finotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.marquisproject.finotes.NoteRoute
import de.marquisproject.finotes.R
import de.marquisproject.finotes.ui.components.NoteCard
import de.marquisproject.finotes.ui.components.SelectionBar
import de.marquisproject.finotes.ui.components.TopBarHome
import de.marquisproject.finotes.ui.viewmodels.MainViewModel


@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(uiState.inSelectionMode) {
        viewModel.clearSelection()
    }


    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (uiState.inSelectionMode) {
                var pinIcon = painterResource(id = R.drawable.outline_push_pin_24)
                var pinAction = { viewModel.pinSelectedNotes() }
                if (uiState.selectedNotes.all { it.isPinned }){
                    pinIcon = painterResource(id = R.drawable.baseline_push_pin_24)
                    pinAction = { viewModel.unpinSelectedNotes() }
                }
                SelectionBar(
                    numSelected = uiState.selectedNotes.size,
                    onSelectionClear = { viewModel.clearSelection() },
                    actionButtons = listOf(
                        pinIcon to pinAction,
                        painterResource(id = R.drawable.outline_archive_24) to { viewModel.archiveSelectedNotes() },
                        painterResource(id = R.drawable.outline_delete_24) to { viewModel.binSelectedNotes() }
                    )
                )
            } else {
                TopBarHome(
                    navController = navController,
                    updateQuery = { viewModel.updateQuery(it) },
                    searchQuery = uiState.searchQuery,
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.insertAndShowNewEmptyNote()
                    navController.navigate(NoteRoute)
                },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Icon(Icons.Filled.Add, "Localized description")
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            modifier = Modifier.padding(innerPadding),
            columns = StaggeredGridCells.Adaptive(200.dp),
            content = {
                items(
                    items = uiState.notesList,
                    key = { note -> note.id }
                ) { note ->
                    NoteCard(
                        note = note,
                        searchQuery = uiState.searchQuery,
                        selected = uiState.selectedNotes.contains(note),
                        onClick = {
                            viewModel.shortClickSelect(note = note, navController = navController)
                        },
                        onLongClick = {
                            viewModel.longClickSelect(note = note)
                        },
                        /*onSwipe = {
                            viewModel.archiveNote(note)
                            scope.launch {
                                snackbarHostState
                                    .showSnackbar(
                                        message = "Note archived",
                                        duration = SnackbarDuration.Short
                                    )
                            }
                                  },
                        swipeIcon = painterResource(id = R.drawable.outline_archive_24),*/
                    )
                }
            }
        )
        if (uiState.notesList.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No notes",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
            }
        }
    }
}