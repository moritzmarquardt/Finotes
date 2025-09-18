package de.marquisproject.finotes.ui.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
) {
    val iEviewModel: ImportExportViewModel = hiltViewModel()

    val navControllerIE = rememberNavController()
    val importExportMode by iEviewModel.importExportMode.collectAsState()
    val loadedData = iEviewModel.loadedData.collectAsState()
    val notesLoaded = loadedData.value.notes.isNotEmpty() || loadedData.value.archivedNotes.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (importExportMode) {
                        ImportExportMode.EXPORT -> "Export notes"
                        ImportExportMode.IMPORT -> "Import notes"
                        ImportExportMode.SYNC -> "Sync notes"
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
                            iEviewModel.setShowFileInfoAlert(true)
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
                        iEviewModel.setMode(ImportExportMode.EXPORT)
                        navControllerIE.navigate(ExportRoute)
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
                        iEviewModel.setMode(ImportExportMode.IMPORT)
                        navControllerIE.navigate(ImportRoute)
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
                    icon = { Icon(painterResource(id = R.drawable.baseline_cloud_sync_24), contentDescription = "Cloud Sync") },
                    label = { Text("Cloud Sync") },
                    selected = importExportMode == ImportExportMode.SYNC,
                    onClick = {
                        iEviewModel.setMode(ImportExportMode.SYNC)
                        navControllerIE.navigate(SyncRoute)
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
        NavHost(
            navController = navControllerIE,
            startDestination = ExportRoute,
            modifier = Modifier.padding(innerPadding),
            exitTransition = { ExitTransition.None},
            enterTransition = { EnterTransition.None }
        ) {
            composable<ExportRoute> {
                ExportScreen(
                    iEviewModel = iEviewModel,
                )
            }
            composable<ImportRoute> {
                ImportScreen(
                    iEviewModel = iEviewModel,
                )
            }
            composable<SyncRoute> {
                SyncScreen()
            }
        }
    }

}

@Serializable object ExportRoute
@Serializable object ImportRoute
@Serializable object SyncRoute