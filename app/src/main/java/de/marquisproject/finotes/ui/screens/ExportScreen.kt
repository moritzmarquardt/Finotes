package de.marquisproject.finotes.ui.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.ui.viewmodels.ExportFileFormat
import de.marquisproject.finotes.ui.viewmodels.ImportExportState
import de.marquisproject.finotes.ui.viewmodels.ImportExportViewModel

@Composable
fun ExportScreen(
    iEState: ImportExportState,
    iEviewModel: ImportExportViewModel,
    createFileLauncher: ActivityResultLauncher<String>
) {
    Column (
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Export notes here to transfer them to a new phone or computer.")
        Text("Export options", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 30.dp, bottom = 10.dp))
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(top = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Text("Include archived notes", style = MaterialTheme.typography.titleMedium)
                val subtitle = if (iEState.exportSettings.includeArchived) "Yes" else "No"
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = iEState.exportSettings.includeArchived,
                onCheckedChange = {
                    iEviewModel.updateExportSettings (
                        iEState.exportSettings.copy(includeArchived = it)
                    )
                                  },
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
        Row (
            modifier = Modifier.padding(top = 25.dp),
        ) {
            Column {
                Text("Choose export format", style = MaterialTheme.typography.titleMedium)
                Text("${iEState.exportSettings.exportFileFormat}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(modifier = Modifier.selectableGroup()) {
            ExportFileFormat.entries.forEach { format ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (iEState.exportSettings.exportFileFormat == format),
                            onClick = {
                                iEviewModel.updateExportSettings (
                                    iEState.exportSettings.copy(exportFileFormat = format)
                                )
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (iEState.exportSettings.exportFileFormat == format),
                        onClick = null // null recommended for accessibility with screen readers
                    )
                    Text(
                        text = format.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
        Button(
            onClick = {
                iEviewModel.exportNotes()
                createFileLauncher.launch("notes_backup.json")
                      },
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp)
        ) {
            if (iEState.isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.width(28.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            } else {
                Text("Export ${iEState.notesToExport.size} notes")
            }
        }
    }
}
