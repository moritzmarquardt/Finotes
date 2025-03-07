package de.marquisproject.finotes.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
    viewModel: MainViewModel,
) {

    val uiState by viewModel.uiState.collectAsState()
    val bodyFocusRequester = FocusRequester()
    val openFinalDeleteAlert = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = uiState.currentNote.id) {
        bodyFocusRequester.requestFocus()
    }

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
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    innerPadding
                )
        ) {
            BasicTextField(
                value = uiState.currentNote.title,
                onValueChange = { viewModel.updateCurrentNoteTitle(it) },
                textStyle = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .focusable()
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardActions = KeyboardActions(
                    onDone = { bodyFocusRequester.requestFocus() },
                    onNext = { bodyFocusRequester.requestFocus() },
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (uiState.currentNote.title.isBlank()) {
                            Text("Title", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                        }
                        innerTextField()
                    }
                }
            )
            BasicTextField(
                value = uiState.currentNote.body,
                onValueChange = { viewModel.updateCurrentNoteBody(it) },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .focusable()
                    .fillMaxWidth()
                    .weight(1f)
                    .focusRequester(bodyFocusRequester),
                keyboardActions = KeyboardActions(
                    onDone = { bodyFocusRequester.requestFocus() },
                    onNext = { bodyFocusRequester.requestFocus() },
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (uiState.currentNote.body.isBlank()) {
                            Text("Body", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        }
                        innerTextField()
                    }
                }
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
