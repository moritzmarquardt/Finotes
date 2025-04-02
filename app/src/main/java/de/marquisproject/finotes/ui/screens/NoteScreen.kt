package de.marquisproject.finotes.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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

    val currentNote by viewModel.currentNote.collectAsState()
    val currentNoteIsNeverEdited by viewModel.currentNoteIsNeverEdited.collectAsState()
    val bodyFocusRequester = FocusRequester()
    val openFinalDeleteAlert = remember { mutableStateOf(false) }
    var bodyTextState by remember { mutableStateOf(TextFieldValue(currentNote.body)) }

    LaunchedEffect(key1 = currentNote.id) {
        if (currentNoteIsNeverEdited) {
            bodyFocusRequester.requestFocus()
        }
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
                }
            )
        },
        modifier = Modifier.imePadding()
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

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            BasicTextField(
                value = currentNote.title,
                onValueChange = { viewModel.updateCurrentNoteTitle(it) },
                textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
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
                        if (currentNote.title.isEmpty()) {
                            Text("Title", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                        }
                        innerTextField()
                    }
                }
            )
            BasicTextField(
                value = bodyTextState,
                onValueChange = { textFieldValue ->
                    val bodyText = textFieldValue.text
                    val cursorPosition = textFieldValue.selection.start

                    // Ensure cursorPosition is valid if textFieldValue is empty
                    if (cursorPosition == 0 || cursorPosition > bodyText.length) {
                        bodyTextState = textFieldValue
                        viewModel.updateCurrentNoteBody(bodyTextState.text)
                        return@BasicTextField
                    }

                    val lastInputIsNewLine = bodyText[cursorPosition - 1] == '\n'
                    val lastInputIsDelete = bodyText.length < bodyTextState.text.length

                    if (lastInputIsNewLine && !lastInputIsDelete) {
                        // empty new line that was just created
                        val textBeforeCursor = bodyText.substring(0, cursorPosition - 1) // Before newline
                        val textAfterCursor = bodyText.substring(cursorPosition) // After newline
                        // Last line before cursor and if \n not found, return textBeforeCursor because it has to be the first line anyways
                        val previousLine = textBeforeCursor.substringAfterLast("\n", textBeforeCursor)

                        when {
                            previousLine.startsWith("- ") && previousLine != "- " -> {
                                // Continue list with "- "
                                val updatedText = "$textBeforeCursor\n- $textAfterCursor"
                                bodyTextState = TextFieldValue(updatedText, TextRange(cursorPosition + 2))
                            }
                            previousLine == "- " -> {
                                // Remove empty "- " line
                                val updatedText = textBeforeCursor.dropLast(2) + textAfterCursor
                                bodyTextState = TextFieldValue(updatedText, TextRange(textBeforeCursor.length - 2))
                            }
                            else -> {
                                bodyTextState = textFieldValue
                            }
                        }
                    } else {
                        bodyTextState = textFieldValue
                    }

                    viewModel.updateCurrentNoteBody(bodyTextState.text)

                    /*if (lastInputIsNewLine && !lastInputIsDelete) {
                        val textBeforePosition = bodyText.substring(0, cursorPosition)
                        val textAfterPosition = bodyText.substring(cursorPosition)
                        val lineBefore = textBeforePosition.split("\n").dropLast(1).lastOrNull()
                        val lineBeforeIsWithList = lineBefore?.startsWith("- ") == true
                        val lineBeforeIsListButEmpty = lineBefore == "- "
                        if (lineBeforeIsWithList && !lineBeforeIsListButEmpty) {
                            val updatedText = "$textBeforePosition- $textAfterPosition"
                            bodyTextState = TextFieldValue(
                                text = updatedText,
                                selection = TextRange(textBeforePosition.length + 2) // Move cursor to end of the new line
                            )
                        } else if (lineBeforeIsListButEmpty) {
                            val textBeforePositionWithoutLastEmptyListLine = textBeforePosition.substring(0, textBeforePosition.length - 3)
                            val updatedText = "${textBeforePositionWithoutLastEmptyListLine}$textAfterPosition"
                            bodyTextState = TextFieldValue(
                                text = updatedText,
                                selection = TextRange(textBeforePositionWithoutLastEmptyListLine.length) // Move cursor to end of the new line
                            )
                        } else {
                            bodyTextState = textFieldValue
                        }
                    } else {
                        bodyTextState = textFieldValue
                    }

                    viewModel.updateCurrentNoteBody(bodyTextState.text)*/
                                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    //.focusable()
                    .fillMaxWidth()
                    .focusRequester(bodyFocusRequester),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (currentNote.body.isEmpty()) {
                            Text("Body", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
