package de.marquisproject.finotes.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.marquisproject.finotes.ui.components.NoteCard
import de.marquisproject.finotes.ui.viewmodels.ImportExportMode
import de.marquisproject.finotes.ui.viewmodels.ImportExportViewModel

@Composable
fun ImportScreen(
    viewModel: ImportExportViewModel,
    modifier: Modifier = Modifier
) {
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importNotesFromFile(it) // Call ViewModel method
        }
    }

    val loadedData by viewModel.loadedData.collectAsStateWithLifecycle()
    val importData by viewModel.importData.collectAsStateWithLifecycle()
    val notesLoaded = loadedData.notes.isNotEmpty() || loadedData.archivedNotes.isNotEmpty()
    val openInfoAlert by viewModel.showFileInfoAlert.collectAsStateWithLifecycle()
    val showFinalImportAlert by viewModel.showFinalImportAlert.collectAsStateWithLifecycle()
    val onlyNonDuplicatesInImportData by viewModel.onlyNonDuplicatesInImportData.collectAsStateWithLifecycle()
    val showImportLoading by viewModel.showImportLoading.collectAsStateWithLifecycle()


    viewModel.setMode(ImportExportMode.IMPORT)

    Box(modifier = modifier.fillMaxSize()) {
        if (!notesLoaded) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Load notes from a JSON file to import them.")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        pickFileLauncher.launch("application/json")
                    }) {
                        Text("Load notes from JSON file")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = {
                        item {
                            ButtonFastSelection(
                                onClick = { viewModel.selectOnlyNonDuplicates() },
                                text = "Select non-duplicates",
                                selected = onlyNonDuplicatesInImportData
                            )
                        }
                        item {
                            ButtonFastSelection(
                                onClick = { viewModel.deselectAllNotes() },
                                text = "Unselect all",
                                icon = Icons.Default.Clear,
                                selected = importData.notes.isEmpty() && importData.archivedNotes.isEmpty()
                            )
                        }
                        item {
                            ButtonFastSelection(
                                onClick = { viewModel.selectAllNotes() },
                                text = "Select all",
                                selected = (importData.notes == loadedData.notes && importData.archivedNotes == loadedData.archivedNotes)
                            )
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier
                            .fillMaxSize(),
                        columns = StaggeredGridCells.Adaptive(180.dp),
                        content = {
                            item(
                                span = StaggeredGridItemSpan.FullLine
                            ) {
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                )
                            }
                            items(
                                items = loadedData.notes,
                                key = { note -> "note_${note.id}" }
                            ) { note ->
                                val isSelected = importData.notes.contains(note)
                                NoteCard(
                                    note = note,
                                    selected = isSelected,
                                    onClick = { viewModel.longClickSelect(note) },
                                    onLongClick = { viewModel.longClickSelect(note) },
                                )
                            }
                            item(
                                span = StaggeredGridItemSpan.FullLine
                            ) {
                                Text(
                                    text = "Archived notes",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                )
                            }
                            items(
                                items = loadedData.archivedNotes,
                                key = { note -> "archived_${note.id}" }
                            ) { note ->
                                val isSelected =
                                    importData.archivedNotes.contains(note)
                                NoteCard(
                                    note = note,
                                    selected = isSelected,
                                    onClick = { viewModel.longClickSelect(note) },
                                    onLongClick = { viewModel.longClickSelect(note) },
                                )
                            }
                            item(
                                span = StaggeredGridItemSpan.FullLine
                            ) {
                                // spacer at the bottom of height 200.dp
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                )
                            }
                        }
                    )


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Button(
                            onClick = {
                                viewModel.clearImportData()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            )
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { viewModel.setShowFinalImportAlert(true) },
                            enabled = importData.notes.isNotEmpty() || importData.archivedNotes.isNotEmpty(),
                        ) {
                            Text("Import selected Notes (${importData.notes.size + importData.archivedNotes.size})")
                        }
                    }
                }
            }
        }

        if (showImportLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        when {
            openInfoAlert -> {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowFileInfoAlert(false) },
                    title = {
                        Text("File opened successfully")
                    },
                    text = {
                        Column {
                            Text("The file contains ${loadedData.notes.size} notes and ${loadedData.archivedNotes.size} archived notes")
                            Text("Select the notes you want to import and click on the import button.")
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.setShowFileInfoAlert(false)
                        }) {
                            Text("Okay")
                        }
                    }
                )
            }
        }
        when {
            showFinalImportAlert -> {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowFinalImportAlert(false) },
                    title = {
                        Text("Import selected notes")
                    },
                    text = {
                        Column {
                            Text("This will add ${importData.notes.size} note(s) and ${importData.archivedNotes.size} archived note(s) to your database.")
                            Text(text = buildAnnotatedString {
                                append("This action is ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("irreversible")
                                }
                                append(".")
                            })
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.setShowFinalImportAlert(false)
                            viewModel.importImportData()
                        }) {
                            Text("Import")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { viewModel.setShowFinalImportAlert(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            )
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ButtonFastSelection(onClick: () -> Unit, text: String, icon: ImageVector? = null, selected: Boolean) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text)
        },
        leadingIcon = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
            selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiary,
        ),
    )
}