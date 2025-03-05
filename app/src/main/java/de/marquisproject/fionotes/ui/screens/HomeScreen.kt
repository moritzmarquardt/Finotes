package de.marquisproject.fionotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.marquisproject.fionotes.NoteRoute
import de.marquisproject.fionotes.R
import de.marquisproject.fionotes.ui.components.NoteCard
import de.marquisproject.fionotes.ui.components.SelectionBar
import de.marquisproject.fionotes.ui.components.TopBarHome


@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        if (uiState.inSelectionMode) {
            viewModel.clearSelection()
        } else {
            navController.popBackStack()
        }
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
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            modifier = Modifier.padding(innerPadding),
            columns = StaggeredGridCells.Adaptive(200.dp),
            content = {
                items(uiState.notesList) { note ->
                    NoteCard(
                        note = note,
                        searchQuery = uiState.searchQuery,
                        selected = uiState.selectedNotes.contains(note),
                        onClick = {
                            viewModel.shortClickSelect(note = note, navController = navController)
                        },
                        onLongClick = {
                            viewModel.longClickSelect(note = note)
                        }
                    )

                }
            }
        )
    }
}