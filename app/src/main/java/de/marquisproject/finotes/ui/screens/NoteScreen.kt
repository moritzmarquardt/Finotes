package de.marquisproject.finotes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.marquisproject.finotes.ui.components.MarkdownTextField
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import de.marquisproject.finotes.R
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.ui.viewmodels.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
) {
    val viewModel: NoteViewModel = hiltViewModel()

    val currentNote by viewModel.currentNote.collectAsState()
    val currentBodyTextFieldValue by viewModel.currentBodyTextFieldValue.collectAsState()
    val noteIsLoaded by viewModel.noteIsLoaded.collectAsState()
    val bodyFocusRequester = remember { FocusRequester() }
    val openFinalDeleteAlert = remember { mutableStateOf(false) }


    LaunchedEffect(noteIsLoaded, currentNote.id) {
        if (noteIsLoaded && currentNote.id == null) {
            bodyFocusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveCurrentNote()  // make sure to save the note before navigating
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Localized description")
                    }
                },
                actions = {
                    if (currentNote.noteStatus == NoteStatus.ACTIVE) {
                        IconButton(onClick = {
                            viewModel.updateCurrentNoteIsPinned(!currentNote.isPinned)
                        }) {
                            if (currentNote.isPinned) {
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
                    if (currentNote.noteStatus == NoteStatus.ACTIVE) {
                        IconButton(onClick = {
                            viewModel.archiveNote(currentNote)
                            navController.popBackStack()
                        }) {
                            Icon(painterResource(id = R.drawable.outline_archive_24), contentDescription = "Localized description")
                        }
                    }
                    if (currentNote.noteStatus == NoteStatus.ARCHIVED) {
                        IconButton(onClick = {
                            viewModel.unarchiveNote(currentNote)
                            navController.popBackStack()
                        }) {
                            Icon(painterResource(id = R.drawable.outline_unarchive_24), contentDescription = "Localized description")
                        }
                    }
                    if (currentNote.noteStatus == NoteStatus.BINNED) {
                        IconButton(onClick = {
                            viewModel.restoreNote(currentNote)
                            navController.popBackStack()
                        }) {
                            Icon(painterResource(id = R.drawable.baseline_restore_from_trash_24), contentDescription = "Localized description")
                        }
                    }
                    if (currentNote.noteStatus == NoteStatus.ACTIVE || currentNote.noteStatus == NoteStatus.ARCHIVED) {
                        IconButton(onClick = {
                            viewModel.binNote(currentNote)
                            navController.popBackStack()
                        }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                    if (currentNote.noteStatus == NoteStatus.BINNED) {
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
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
                            viewModel.deleteNoteFromBin(currentNote)
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.imePadding()
            ) {
                TextField(
                    readOnly = currentNote.noteStatus != NoteStatus.ACTIVE && noteIsLoaded,
                    value = currentNote.title,
                    onValueChange = { viewModel.updateCurrentNoteTitle(it) },
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { bodyFocusRequester.requestFocus() },
                    ),
                    singleLine = true,
                    placeholder = { Text("Title", color = Color.Gray, style = MaterialTheme.typography.titleLarge)},
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )
                MarkdownTextField(
                    value = currentBodyTextFieldValue,
                    onValueChange = { viewModel.updateCurrentNoteBody(it) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    focusRequester = bodyFocusRequester,
                    placeholder = {
                        Text("Body", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
