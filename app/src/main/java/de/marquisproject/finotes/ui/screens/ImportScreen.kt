package de.marquisproject.finotes.ui.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import de.marquisproject.finotes.ui.components.NoteCard
import de.marquisproject.finotes.ui.viewmodels.ImportExportMode
import de.marquisproject.finotes.ui.viewmodels.ImportExportViewModel
import kotlin.math.exp

@Composable
fun ImportScreen(
    pickFileLauncher: ActivityResultLauncher<String>,
    iEviewModel: ImportExportViewModel,
) {
    val loadedData = iEviewModel.loadedData.collectAsState()
    val importData = iEviewModel.importData.collectAsState()
    val notesLoaded = loadedData.value.notes.isNotEmpty() || loadedData.value.archivedNotes.isNotEmpty()
    val openInfoAlert = iEviewModel.showFileInfoAlert.collectAsState()
    val showFinalImportAlert = iEviewModel.showFinalImportAlert.collectAsState()
    val onlyNonDuplicatesInImportData = iEviewModel.onlyNonDuplicatesInImportData.collectAsState()

    iEviewModel.setMode(ImportExportMode.IMPORT)

    if (!notesLoaded){
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Load notes from a JSON file to import them.")
            Row(
                modifier = Modifier.fillMaxWidth()
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
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
    ) {

        if (notesLoaded) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = {
                    item {
                        ButtonFastSelection(onClick = {iEviewModel.selectOnlyNonDuplicates()}, text = "Select non-duplicates", selected = onlyNonDuplicatesInImportData.value)
                    }
                    item {
                        ButtonFastSelection(onClick = {iEviewModel.deselectAllNotes()}, text = "Unselect all", icon = Icons.Default.Clear, selected = importData.value.notes.isEmpty() && importData.value.archivedNotes.isEmpty())
                    }
                    item {
                        ButtonFastSelection(onClick = {iEviewModel.selectAllNotes()}, text = "Select all", selected = (importData.value.notes == loadedData.value.notes && importData.value.archivedNotes == loadedData.value.archivedNotes))
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            if (notesLoaded) {
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
                            items = loadedData.value.notes,
                            key = { note -> note.id }
                        ) { note ->
                            val isSelected = importData.value.notes.contains(note)
                            NoteCard(
                                note = note,
                                selected = isSelected,
                                onClick = { iEviewModel.longClickSelect(note) },
                                onLongClick = { iEviewModel.longClickSelect(note) },
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
                            items = loadedData.value.archivedNotes,
                            key = { note -> note.id }
                        ) { note ->
                            val isSelected = importData.value.archivedNotes.contains(note)
                            NoteCard(
                                note = note,
                                selected = isSelected,
                                onClick = { iEviewModel.longClickSelect(note) },
                                onLongClick = { iEviewModel.longClickSelect(note) },
                            )
                        }
                        item(
                            span = StaggeredGridItemSpan.FullLine
                        ) {
                            // spacer at the bottom of heigth 200.dp
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            )
                        }
                    }
                )
            }


            if (notesLoaded){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                            )
                        )
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = {
                            iEviewModel.clearImportData()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        )
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { iEviewModel.setShowFinalImportAlert(true) },
                        enabled = importData.value.notes.isNotEmpty() || importData.value.archivedNotes.isNotEmpty(),
                    ) {
                        Text("Import selected Notes (${importData.value.notes.size + importData.value.archivedNotes.size})")
                    }
                }
            }

        }
    }
    when {
        openInfoAlert.value -> {
            AlertDialog(
                onDismissRequest = { iEviewModel.setShowFileInfoAlert(false) },
                title = {
                    Text("File opened successfully")
                },
                text = {
                    Column {
                        Text("The file contains ${loadedData.value.notes.size} notes and ${loadedData.value.archivedNotes.size} archived notes")
                        Text("Select the notes you want to import and click on the import button.")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        iEviewModel.setShowFileInfoAlert(false)
                    }) {
                        Text("Okay")
                    }
                }
            )
        }
    }
    when {
        showFinalImportAlert.value -> {
            AlertDialog(
                onDismissRequest = { iEviewModel.setShowFinalImportAlert(false) },
                title = {
                    Text("Import selected notes")
                },
                text = {
                    Column {
                        Text("This will add ${importData.value.notes.size} note(s) and ${importData.value.archivedNotes.size} archived note(s) to your database.")
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
                        iEviewModel.setShowFinalImportAlert(false)
                        iEviewModel.importImportData()
                    }) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { iEviewModel.setShowFinalImportAlert(false) },
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