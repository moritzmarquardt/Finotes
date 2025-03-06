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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.R
import de.marquisproject.finotes.ui.components.NoteCard
import de.marquisproject.finotes.ui.components.SelectionBar
import de.marquisproject.finotes.ui.viewmodels.MainViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinScreen(
    navController: NavController,
    viewModel: MainViewModel
) {

    val uiState by viewModel.uiState.collectAsState()
    val openFinalDeleteAlert = remember { mutableStateOf(false) }

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
                        painterResource(id = R.drawable.baseline_restore_from_trash_24) to { viewModel.restoreSelectedNotes() },
                        painterResource(id = R.drawable.outline_delete_24) to { openFinalDeleteAlert.value = true }
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(text = "Bin")
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
        floatingActionButton = {
            if (uiState.binList.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.selectAllBinned()
                    },
                ) {
                    Text("Select all")
                }
            }
        }
    ) { innerPadding ->
        if (uiState.binList.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No notes in the bin",
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
                    items = uiState.binList,
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
                        }
                    )
                }
            }
        )
        when {
            openFinalDeleteAlert.value -> {
                AlertDialog(
                    onDismissRequest = { openFinalDeleteAlert.value = false },
                    title = {
                        Text("Delete selected notes permanently")
                    },
                    text = {
                        Text("This deletion is irreversible")
                    },
                    confirmButton = {
                        Button(onClick = {
                            openFinalDeleteAlert.value = false
                            viewModel.permanentlyDeleteSelection()
                        }) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { openFinalDeleteAlert.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}