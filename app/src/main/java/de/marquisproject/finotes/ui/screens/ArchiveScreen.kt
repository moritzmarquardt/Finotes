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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import de.marquisproject.finotes.NoteRoute
import de.marquisproject.finotes.R
import de.marquisproject.finotes.ui.components.NotesList
import de.marquisproject.finotes.ui.components.SelectionBar
import de.marquisproject.finotes.ui.viewmodels.ArchiveViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ArchiveScreen(
    navController: NavController,
) {

    val viewModel: ArchiveViewModel = hiltViewModel()

    val inSelectionMode by viewModel.inSelectionMode.collectAsState()
    val selectedNotes by viewModel.selectedNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val notesList by viewModel.notesList.collectAsState()

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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
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
                    text = "No notes in the archive",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
            }
        }
        NotesList(
            padding = innerPadding,
            notesList = notesList,
            selectedNotes = selectedNotes,
            searchQuery = searchQuery,
            onShortClick = { note ->
                viewModel.shortClickSelect(note = note, shortClickAction = {navController.navigate(NoteRoute(noteId = note.id, noteStatus = note.noteStatus))} )
            },
            onLongClick = { note ->
                viewModel.longClickSelect(note = note)
            }
        )
    }
}