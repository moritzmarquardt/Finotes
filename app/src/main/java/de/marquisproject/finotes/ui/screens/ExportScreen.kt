package de.marquisproject.finotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.ui.viewmodels.ExportFileFormat
import de.marquisproject.finotes.ui.viewmodels.ExportSettings
import de.marquisproject.finotes.ui.viewmodels.ImportExportState
import de.marquisproject.finotes.ui.viewmodels.ImportExportViewModel

@Composable
fun ExportScreen(
    iEState: ImportExportState,
    iEviewModel: ImportExportViewModel,
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
        Column(
            modifier = Modifier.padding(top = 15.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Top
            ) {
                Text("Select Export Format", style = MaterialTheme.typography.titleMedium)
                Text("${iEState.exportSettings.exportFileFormat}", style = MaterialTheme.typography.bodyMedium)
            }
            Column {
                ExportFileFormat.entries.forEach { format ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = iEState.exportSettings.exportFileFormat == format,
                            onClick = {
                                iEviewModel.updateExportSettings (
                                    iEState.exportSettings.copy(exportFileFormat = format)
                                )
                            }
                        )
                        Button(
                            onClick = {
                                iEviewModel.updateExportSettings (
                                    iEState.exportSettings.copy(exportFileFormat = format)
                                )
                            },
                            colors = ButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            )
                        ) {
                            Text(format.name)
                        }
                    }
                }
            }
        }
        Button(
            onClick = { iEviewModel.exportNotes() },
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp)
        ) {
            Text("Export ${iEState.notesToExport.size} notes")
        }
    }
}
