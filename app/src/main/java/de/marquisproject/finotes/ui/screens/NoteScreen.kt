package de.marquisproject.finotes.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.marquisproject.finotes.R
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
    viewModel: MainViewModel,
) {

    val uiState by viewModel.uiState.collectAsState()
    val bodyFocusRequester = FocusRequester()
    val openFinalDeleteAlert = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    //Text(text = "Note View with id: ${uiState.currentNoteId}")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Localized description")
                    }
                },
                actions = {
                    if (uiState.currentNote.noteStatus == NoteStatus.ACTIVE) {
                        IconButton(onClick = {
                            viewModel.updateCurrentNoteIsPinned(!uiState.currentNote.isPinned)
                        }) {
                            if (uiState.currentNote.isPinned) {
                                Icon(
                                    painterResource(id = R.drawable.baseline_push_pin_24),
                                    contentDescription = "Localized description"
                                )
                            } else {
                                Icon(
                                    painterResource(id = R.drawable.outline_push_pin_24),
                                    contentDescription = "Localized description"
                                )
                            }
                        }
                    }
                    if (uiState.currentNote.noteStatus == NoteStatus.ACTIVE) {
                        IconButton(onClick = {
                            viewModel.archiveNote(uiState.currentNote)
                            navController.popBackStack()
                        }) {
                            Icon(painterResource(id = R.drawable.outline_archive_24), contentDescription = "Localized description")
                        }
                    }
                    if (uiState.currentNote.noteStatus == NoteStatus.ARCHIVED) {
                        IconButton(onClick = {
                            viewModel.unarchiveNote(uiState.currentNote)
                            navController.popBackStack()
                        }) {
                            Icon(painterResource(id = R.drawable.outline_unarchive_24), contentDescription = "Localized description")
                        }
                    }
                    if (uiState.currentNote.noteStatus == NoteStatus.BINNED) {
                        IconButton(onClick = {
                            viewModel.restoreNote(uiState.currentNote)
                            navController.popBackStack()
                        }) {
                            Icon(painterResource(id = R.drawable.baseline_restore_from_trash_24), contentDescription = "Localized description")
                        }
                    }
                    if (uiState.currentNote.noteStatus == NoteStatus.ACTIVE || uiState.currentNote.noteStatus == NoteStatus.ARCHIVED) {
                        IconButton(onClick = {
                            viewModel.binNote(uiState.currentNote)
                            navController.popBackStack()
                        }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                    if (uiState.currentNote.noteStatus == NoteStatus.BINNED) {
                        IconButton(onClick = {
                            //viewModel.deleteNoteFromBin(uiState.currentNote)
                            //navController.popBackStack()
                            openFinalDeleteAlert.value = true
                        }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(56.dp),
                actions = {
                    val timestamp = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(uiState.currentNote.dateCreated))
                    Text(text = "Created on: $timestamp", style = MaterialTheme.typography.labelMedium)
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            TextField(
                label = null,
                value = uiState.currentNote.title,
                onValueChange = { viewModel.updateCurrentNoteTitle(it) },
                placeholder = { Text("Title", style = MaterialTheme.typography.titleLarge) },
                textStyle = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .focusable()
                    .fillMaxWidth(),
                keyboardActions = KeyboardActions(
                    onDone = { bodyFocusRequester.requestFocus() },
                    onNext = { bodyFocusRequester.requestFocus() },
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
            TextField(
                label = null,
                value = uiState.currentNote.body,
                onValueChange = { viewModel.updateCurrentNoteBody(it) },
                placeholder = { Text("Body", style = MaterialTheme.typography.bodyLarge) },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .focusable()
                    .fillMaxWidth()
                    .fillMaxSize()
                    .focusRequester(bodyFocusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
            when {
                openFinalDeleteAlert.value -> {
                    AlertDialog(
                        onDismissRequest = { openFinalDeleteAlert.value = false },
                        title = {
                            Text("Delete note permanently")
                        },
                        text = {
                            Text("This deletion is irreversible")
                        },
                        confirmButton = {
                            Button(onClick = {
                                openFinalDeleteAlert.value = false
                                navController.popBackStack()
                                viewModel.deleteNoteFromBin(uiState.currentNote)
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
}
