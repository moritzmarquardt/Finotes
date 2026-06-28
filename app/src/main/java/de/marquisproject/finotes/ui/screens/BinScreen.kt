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
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.marquisproject.finotes.NoteRoute
import de.marquisproject.finotes.R
import de.marquisproject.finotes.ui.components.NotesList
import de.marquisproject.finotes.ui.components.SelectionBar
import de.marquisproject.finotes.ui.viewmodels.BinViewModel
import de.marquisproject.finotes.utils.NoteSection
import kotlin.collections.listOf


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BinScreen(
    navController: NavController,
) {
    val viewModel: BinViewModel = hiltViewModel()
    //val notesList by viewModel.
    val inSelectionMode by viewModel.inSelectionMode.collectAsStateWithLifecycle()
    val selectedNotes by viewModel.selectedNotes.collectAsStateWithLifecycle()
    val notesList by viewModel.notesList.collectAsStateWithLifecycle()
    val openFinalDeleteAlert = remember { mutableStateOf(false) }

    var isSearching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    BackHandler {
        if (inSelectionMode) {
            viewModel.clearSelection()
        } else if (isSearching) {
            isSearching = false
            viewModel.searchQuery.clearText()
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
                        if (isSearching) {
                            de.marquisproject.finotes.ui.components.SearchField(
                                state = viewModel.searchQuery,
                                modifier = Modifier.focusRequester(focusRequester),
                                placeholder = "Search Bin"
                            )
                        } else {
                            Text(text = "Bin")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isSearching) {
                                isSearching = false
                                viewModel.searchQuery.clearText()
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back press"
                            )
                        }
                    },
                    actions = {
                        if (!isSearching) {
                            IconButton(onClick = { isSearching = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
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
            if (notesList.isNotEmpty()) {
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
        if (notesList.isEmpty()) {
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
            noteSections = listOf(
                NoteSection(
                    title = "Binned Notes",
                    notesList = notesList,
                )
            ),
            selectedNotes = selectedNotes,
            searchQuery = viewModel.searchQuery,
            onShortClick = { note ->
                viewModel.shortClickSelect(note = note, shortClickAction = {navController.navigate(NoteRoute(noteId = requireNotNull(note.id), noteStatus = note.noteStatus))} )
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