package de.marquisproject.finotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.marquisproject.finotes.NoteRoute
import de.marquisproject.finotes.R
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.ui.components.NotesList
import de.marquisproject.finotes.ui.components.SelectionBar
import de.marquisproject.finotes.ui.components.TopBarHome
import de.marquisproject.finotes.ui.viewmodels.HomeViewModel
import de.marquisproject.finotes.utils.NoteSection
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
) {
    val viewModel: HomeViewModel = hiltViewModel()
    
    val inSelectionMode by viewModel.inSelectionMode.collectAsStateWithLifecycle()
    val selectedNotes by viewModel.selectedNotes.collectAsStateWithLifecycle()
    val pinnedNotesDisplay by viewModel.pinnedNotesDisplay.collectAsStateWithLifecycle()
    val normalNotesDisplay by viewModel.normalNotesDisplay.collectAsStateWithLifecycle()

    // Create a SnackbarHostState to control the Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Clear selection when pressing back if there is any
    BackHandler(inSelectionMode) {
        viewModel.clearSelection()
    }

    // when a search query is set, clear the query when pressing back
    BackHandler(viewModel.searchQuery.text.isNotEmpty()) {
        viewModel.setQuery("")
    }

    // Handle focus management when IME (keyboard) visibility changes
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }


    // Observe Snackbar events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.snackbarEventFlow.collectLatest { event ->
            when (event) {
                is HomeViewModel.SnackbarEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        withDismissAction = true, // Allows dismissal by swipe or timeout
                        duration = SnackbarDuration.Short
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.performUndo()
                    }
                }
            }
        }
    }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (inSelectionMode) {
                var pinIcon = painterResource(id = R.drawable.outline_push_pin_24)
                var pinAction = { viewModel.pinSelectedNotes() }
                if (selectedNotes.all { it.isPinned }){
                    pinIcon = painterResource(id = R.drawable.baseline_push_pin_24)
                    pinAction = { viewModel.unpinSelectedNotes() }
                }
                SelectionBar(
                    numSelected = selectedNotes.size,
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
                    searchQuery = viewModel.searchQuery,
                )
            }
        },
        floatingActionButton = {
            AddNoteFAB(
                onClick = {
                    navController.navigate(NoteRoute(noteId = null, noteStatus = NoteStatus.ACTIVE)) // Navigate to new note with id -1
                }
            ) },
        snackbarHost = { SnackbarHost(snackbarHostState) } // Provide the SnackbarHost
    ) { innerPadding ->
        NotesList(
            padding = innerPadding,
            noteSections = listOf(
                NoteSection(
                    title = "Pinned Notes",
                    notesList = pinnedNotesDisplay,
                    onSelectSection = { viewModel.toggleSelectAllPinnedNotes(add = true) },
                    isSectionSelected = viewModel.allPinnedNotesSelected(exclusive = false)
                ),
                NoteSection(
                    title = "Notes",
                    notesList = normalNotesDisplay,
                    onSelectSection = { viewModel.toggleSelectAllNonPinnedNotes(add = true) },
                    isSectionSelected = viewModel.allNonPinnedNotesSelected(exclusive = false)
                )
            ),
            inSelectionMode = inSelectionMode,
            selectedNotes = selectedNotes.toList(),
            searchQuery = viewModel.searchQuery,
            onShortClick = { note ->
                viewModel.shortClickSelect(note = note, shortClickAction = {navController.navigate(NoteRoute(noteId = requireNotNull(note.id), noteStatus = note.noteStatus))} )
            },
            onLongClick = { note ->
                viewModel.longClickSelect(note = note)
            }
        )
        if (pinnedNotesDisplay.isEmpty() && normalNotesDisplay.isEmpty()) {
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

@Composable
fun AddNoteFAB(
    onClick : () -> Unit,
) {
    FloatingActionButton(
        onClick = { onClick() },
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
    ) {
        Icon(Icons.Filled.Add, "Localized description")
    }
}