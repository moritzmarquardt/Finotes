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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.marquisproject.finotes.R
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.ui.components.MarkdownTextField
import de.marquisproject.finotes.ui.viewmodels.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
) {
    val viewModel: NoteViewModel = hiltViewModel()

    val isPinned by viewModel.isPinned.collectAsStateWithLifecycle()
    val noteStatus by viewModel.noteStatus.collectAsStateWithLifecycle()
    val areQuickActionsAllowed by viewModel.areQuickActionsAllowed.collectAsStateWithLifecycle()
    val isEditingAllowed by viewModel.isEditingAllowed.collectAsStateWithLifecycle()
    val shouldFocusBody by viewModel.shouldFocusBody.collectAsStateWithLifecycle()
    val bodyFocusRequester = remember { FocusRequester() }
    val openFinalDeleteAlert = remember { mutableStateOf(false) }

    BackHandler {
        viewModel.saveIfNeeded {
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
                        viewModel.saveIfNeeded {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteStatus == NoteStatus.ACTIVE) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                viewModel.togglePinned()
                            }
                        ) {
                            if (isPinned) {
                                Icon(
                                    painterResource(id = R.drawable.baseline_push_pin_24),
                                    contentDescription = "Unpin"
                                )
                            } else {
                                Icon(
                                    painterResource(id = R.drawable.outline_push_pin_24),
                                    contentDescription = "Pin"
                                )
                            }
                        }
                    }
                    if (noteStatus == NoteStatus.ACTIVE) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                viewModel.archiveNote {
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(painterResource(id = R.drawable.outline_archive_24), contentDescription = "Archive")
                        }
                    }
                    if (noteStatus == NoteStatus.ARCHIVED) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                viewModel.unarchiveNote {
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(painterResource(id = R.drawable.outline_unarchive_24), contentDescription = "Unarchive")
                        }
                    }
                    if (noteStatus == NoteStatus.BINNED) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                viewModel.restoreNote {
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(painterResource(id = R.drawable.baseline_restore_from_trash_24), contentDescription = "Restore")
                        }
                    }
                    if (noteStatus == NoteStatus.ACTIVE || noteStatus == NoteStatus.ARCHIVED) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                viewModel.binNote {
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Bin"
                            )
                        }
                    }
                    if (noteStatus == NoteStatus.BINNED) {
                        IconButton(
                            enabled = areQuickActionsAllowed,
                            onClick = {
                                openFinalDeleteAlert.value = true
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete permanently"
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
                            viewModel.deleteNoteFromBin {
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
