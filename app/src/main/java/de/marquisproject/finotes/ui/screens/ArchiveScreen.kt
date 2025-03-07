package de.marquisproject.finotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.R
import de.marquisproject.finotes.ui.components.NoteCard
import de.marquisproject.finotes.ui.components.SelectionBar
import de.marquisproject.finotes.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    navController: NavController,
    viewModel: MainViewModel
) {

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                SelectionBar(
                    numSelected = uiState.selectedNotes.size,
                    onSelectionClear = { viewModel.clearSelection() },
                    actionButtons = listOf(
                        painterResource(id = R.drawable.outline_unarchive_24) to { viewModel.unarchiveSelectedNotes() },
                        painterResource(id = R.drawable.outline_delete_24) to { viewModel.binSelectedNotes() }
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(text = "Archive")
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back press"
                            )
                        }
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        }
    ) { innerPadding ->
        if (uiState.archivedList.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No notes in the archive",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
            }
        }
        LazyVerticalStaggeredGrid(
            modifier = Modifier.padding(innerPadding),
            columns = StaggeredGridCells.Adaptive(200.dp),
            content = {
                items(
                    items = uiState.archivedList,
                    key = { note -> note.id } //necessary for the swiping to work
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
                            viewModel.binNote(note)
                            scope.launch {
                                snackbarHostState
                                    .showSnackbar(
                                        message = "Note moved to bin",
                                        duration = SnackbarDuration.Short
                                    )
                            }
                                  },
                        swipeIcon = painterResource(id = R.drawable.outline_delete_24)*/
                    )
                }
            }
        )
    }
}