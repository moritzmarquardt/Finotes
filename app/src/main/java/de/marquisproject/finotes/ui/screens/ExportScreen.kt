package de.marquisproject.finotes.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.ui.viewmodels.ImportExportViewModel
import de.marquisproject.finotes.data.backup.ExportFileFormat
import de.marquisproject.finotes.data.backup.ImportExportMode
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ExportScreen(
    viewModel: ImportExportViewModel,
    modifier: Modifier = Modifier
) {
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportNotesToFile(it)
        }
    }

    val exportSettings by viewModel.exportSettings.collectAsStateWithLifecycle()
    val exportData by viewModel.exportData.collectAsStateWithLifecycle()
    val exportPassphrase by viewModel.exportPassphrase.collectAsStateWithLifecycle()
    val operationResult by viewModel.operationResult.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(operationResult) {
        operationResult?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            viewModel.consumeOperationResult()
        }
    }

    viewModel.setMode(ImportExportMode.EXPORT)

    Column (
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
                val subtitle = if (exportSettings.includeArchived) "Yes" else "No"
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = exportSettings.includeArchived,
                onCheckedChange = {
                    viewModel.setExportSettings (
                        exportSettings.copy(includeArchived = it)
                    )
                              },
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(top = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Text("Include binned notes", style = MaterialTheme.typography.titleMedium)
                val subtitle = if (exportSettings.includeBinned) "Yes" else "No"
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = exportSettings.includeBinned,
                onCheckedChange = {
                    viewModel.setExportSettings (
                        exportSettings.copy(includeBinned = it)
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
                Text("${exportSettings.exportFileFormat}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(modifier = Modifier.selectableGroup()) {
            ExportFileFormat.entries.forEach { format ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (exportSettings.exportFileFormat == format),
                            onClick = {
                                viewModel.setExportSettings (
                                    exportSettings.copy(exportFileFormat = format)
                                )
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (exportSettings.exportFileFormat == format),
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
        Text(
            "Optional: encrypt backup",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 25.dp)
        )
        Text(
            "Enter a passphrase to encrypt the backup with AES-256-GCM. Without it the file is readable by anyone with access to it.",
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = exportPassphrase,
            onValueChange = { viewModel.setExportPassphrase(it) },
            label = { Text("Passphrase (leave empty for no encryption)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
        Button(
            onClick = {
                createFileLauncher.launch(viewModel.suggestBackupFileName())
                      },
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp)
        ) {
            val numExportNotes = exportData.notes.size + exportData.archivedNotes.size + exportData.binnedNotes.size
            Text("Export $numExportNotes notes")
        }
    }
}
