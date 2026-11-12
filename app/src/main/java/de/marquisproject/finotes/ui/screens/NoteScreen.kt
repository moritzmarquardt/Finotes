package de.marquisproject.finotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.ui.components.MarkdownTextField
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.marquisproject.finotes.R
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.ui.viewmodels.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
) {
    val viewModel: NoteViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    val currentNote by viewModel.currentNote.collectAsStateWithLifecycle()
    val areQuickActionsAllowed by viewModel.areQuickActionsAllowed.collectAsStateWithLifecycle()
    val isEditingAllowed by viewModel.isEditingAllowed.collectAsStateWithLifecycle()
    val shouldFocusBody by viewModel.shouldFocusBody.collectAsStateWithLifecycle()
    val bodyFocusRequester = remember { FocusRequester() }
    val openFinalDeleteAlert = remember { mutableStateOf(false) }

    BackHandler {
        scope.launch {
            viewModel.saveIfNeeded()
            navController.popBackStack()
        }
    }


    LaunchedEffect(shouldFocusBody) {
        if (shouldFocusBody) {
            bodyFocusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.saveIfNeeded()
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Localized description")
                    }
                },
                actions = {
                    if (currentNote.noteStatus == NoteStatus.ACTIVE) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                scope.launch {
                                    viewModel.updateCurrentNoteIsPinned(!currentNote.isPinned)
                                }
                            }
                        ) {
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
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                scope.launch {
                                    viewModel.archiveNote(currentNote)
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(painterResource(id = R.drawable.outline_archive_24), contentDescription = "Localized description")
                        }
                    }
                    if (currentNote.noteStatus == NoteStatus.ARCHIVED) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                scope.launch {
                                    viewModel.unarchiveNote(currentNote)
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(painterResource(id = R.drawable.outline_unarchive_24), contentDescription = "Localized description")
                        }
                    }
                    if (currentNote.noteStatus == NoteStatus.BINNED) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                scope.launch {
                                    viewModel.restoreNote(currentNote)
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(painterResource(id = R.drawable.baseline_restore_from_trash_24), contentDescription = "Localized description")
                        }
                    }
                    if (currentNote.noteStatus == NoteStatus.ACTIVE || currentNote.noteStatus == NoteStatus.ARCHIVED) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                scope.launch {
                                    viewModel.binNote(currentNote)
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                    if (currentNote.noteStatus == NoteStatus.BINNED) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                openFinalDeleteAlert.value = true
                            }
                        ) {
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
                            scope.launch {
                                viewModel.deleteNoteFromBin(currentNote)
                                navController.popBackStack()
                            }
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
                    readOnly = !isEditingAllowed,
                    state = viewModel.titleState,
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    ),
                    onKeyboardAction = {
                        bodyFocusRequester.requestFocus()
                    },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    placeholder = { Text("Title", color = Color.Gray, style = MaterialTheme.typography.titleLarge) },
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
                    readOnly = !isEditingAllowed,
                    state = viewModel.bodyState,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    focusRequester = bodyFocusRequester,
                    placeholder = {
                        Text("Body", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
