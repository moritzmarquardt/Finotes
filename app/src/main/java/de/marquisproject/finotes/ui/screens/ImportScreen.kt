package de.marquisproject.finotes.ui.screens

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.marquisproject.finotes.ui.viewmodels.ImportExportState
import de.marquisproject.finotes.ui.viewmodels.ImportExportViewModel

@Composable
fun ImportScreen(
    iEstate: ImportExportState,
    iEviewModel: ImportExportViewModel,
    pickFileLauncher: ActivityResultLauncher<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            Log.e("Import", "Button Clicked")
            pickFileLauncher.launch("application/json")
        }) {
            Text("Import")
        }
    }
}