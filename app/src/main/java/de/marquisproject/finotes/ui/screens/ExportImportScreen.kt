package de.marquisproject.finotes.ui.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.marquisproject.finotes.R
import de.marquisproject.finotes.ui.viewmodels.ImportExportMode
import de.marquisproject.finotes.ui.viewmodels.ImportExportViewModel
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    navControllerMain: NavController,
    iEviewModel: ImportExportViewModel,
    createFileLauncher: ActivityResultLauncher<String>,
    pickFileLauncher: ActivityResultLauncher<String>
) {
    val navControllerIE = rememberNavController()
    val iEState by iEviewModel.importExportState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (iEState.mode) {
                        ImportExportMode.EXPORT -> "Export notes"
                        ImportExportMode.IMPORT -> "Import notes"
                    }
                    Text(text = title)
                },
                navigationIcon = {
                    IconButton(onClick = { navControllerMain.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back press"
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar( ) {
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.outline_file_upload_24), contentDescription = "Home") },
                    label = { Text("Export") },
                    selected = iEState.mode == ImportExportMode.EXPORT,
                    onClick = {
                        iEviewModel.setMode(ImportExportMode.EXPORT)
                        navControllerIE.navigate(ExportRoute)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.outline_file_download_24), contentDescription = "Home") },
                    label = { Text("Import") },
                    selected = iEState.mode == ImportExportMode.IMPORT,
                    onClick = {
                        iEviewModel.setMode(ImportExportMode.IMPORT)
                        navControllerIE.navigate(ImportRoute)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navControllerIE,
            startDestination = ExportRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<ExportRoute> {
                ExportScreen(
                    iEState = iEState,
                    iEviewModel = iEviewModel,
                    createFileLauncher = createFileLauncher
                )
            }
            composable<ImportRoute> {
                ImportScreen(
                    pickFileLauncher = pickFileLauncher
                )
            }
        }
    }

}

@Serializable object ExportRoute
@Serializable object ImportRoute