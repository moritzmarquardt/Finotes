package de.marquisproject.finotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import de.marquisproject.finotes.NoteRoute
import de.marquisproject.finotes.R
import de.marquisproject.finotes.ui.components.NotesList
import de.marquisproject.finotes.ui.components.SelectionBar
import de.marquisproject.finotes.ui.viewmodels.MainViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BinScreen(
    navController: NavController,
    viewModel: MainViewModel,
) {
    //val notesList by viewModel.
    val inSelectionMode by viewModel.inSelectionMode.collectAsState()
    val selectedNotes by viewModel.selectedNotes.collectAsState()
    val binList by viewModel.binList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val openFinalDeleteAlert = remember { mutableStateOf(false) }

    viewModel.fetchBinNotes() // fetch them here when the screen is opened

    BackHandler {
        if (inSelectionMode) {
            viewModel.clearSelection()
        } else {
            navController.popBackStack()
        }
    }



    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (inSelectionMode) {
                SelectionBar(
                    numSelected = selectedNotes.size,
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        },
        floatingActionButton = {
            if (binList.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    onClick = {
                        viewModel.selectAllBinned()
                    },
                ) {
                    Text("Select all")
                }
            }
        }
    ) { innerPadding ->
        if (binList.isEmpty()) {
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
        NotesList(
            padding = innerPadding,
            notesList = binList,
            selectedNotes = selectedNotes,
            searchQuery = searchQuery,
            onShortClick = { note ->
                viewModel.shortClickSelect(note = note, shortClickAction = {navController.navigate(NoteRoute)})
            },
            onLongClick = { note ->
                viewModel.longClickSelect(note = note)
            }
        )
        if(openFinalDeleteAlert.value) {
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
                    Button(
                        onClick = { openFinalDeleteAlert.value = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}