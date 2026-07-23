package de.marquisproject.finotes.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.marquisproject.finotes.R
import de.marquisproject.finotes.ui.viewmodels.ImportExportMode
import de.marquisproject.finotes.ui.viewmodels.ImportExportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    navControllerMain: NavController,
) {
    val viewModel: ImportExportViewModel = hiltViewModel()

    val importExportMode by viewModel.importExportMode.collectAsStateWithLifecycle()
    val loadedData by viewModel.loadedData.collectAsStateWithLifecycle()
    val notesLoaded = loadedData.notes.isNotEmpty() || loadedData.archivedNotes.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (importExportMode) {
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
                actions = {
                    if (importExportMode == ImportExportMode.IMPORT && notesLoaded) {
                        IconButton(onClick = {
                            viewModel.setShowFileInfoAlert(true)
                        }) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "File info"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.outline_file_upload_24), contentDescription = "Home") },
                    label = { Text("Export") },
                    selected = importExportMode == ImportExportMode.EXPORT,
                    onClick = {
                        viewModel.setMode(ImportExportMode.EXPORT)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.secondary,
                    )
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.outline_file_download_24), contentDescription = "Home") },
                    label = { Text("Import") },
                    selected = importExportMode == ImportExportMode.IMPORT,
                    onClick = {
                        viewModel.setMode(ImportExportMode.IMPORT)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.secondary,
                    )
                )
            }
        }
    ) { innerPadding ->
        when (importExportMode) {
            ImportExportMode.EXPORT -> {
                ExportScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ImportExportMode.IMPORT -> {
                ImportScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

}