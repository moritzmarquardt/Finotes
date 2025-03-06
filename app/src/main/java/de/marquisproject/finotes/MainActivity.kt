package de.marquisproject.finotes

import android.net.Uri
import de.marquisproject.finotes.ui.screens.HomeScreen
import de.marquisproject.finotes.ui.screens.NoteScreen
import de.marquisproject.finotes.ui.screens.ExportImportScreen
import de.marquisproject.finotes.data.notes.sources.NoteDatabase
import de.marquisproject.finotes.data.notes.sources.ArchiveDatabase
import de.marquisproject.finotes.data.notes.sources.BinDatabase

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import de.marquisproject.finotes.ui.screens.ArchiveScreen
import de.marquisproject.finotes.ui.viewmodels.MainViewModel
import de.marquisproject.finotes.ui.screens.BinScreen
import de.marquisproject.finotes.ui.theme.FinotesTheme
import de.marquisproject.finotes.ui.viewmodels.ImportExportViewModel
import kotlinx.serialization.Serializable
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val noteDb by lazy {
        Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            "note.db"
        ).build()
    }
    private val archiveDb by lazy {
        Room.databaseBuilder(
            applicationContext,
            ArchiveDatabase::class.java,
            "archive.db"
        ).build()
    }
    private val binDb by lazy {
        Room.databaseBuilder(
            applicationContext,
            BinDatabase::class.java,
            "bin.db"
        ).build()
    }
    private val viewModel by viewModels<MainViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(NoteRepository(
                        noteDb = noteDb,
                        archiveDb = archiveDb,
                        binDb = binDb
                    )) as T
                }
            }
        }
    )

    private val importExportViewModel by viewModels<ImportExportViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ImportExportViewModel(NoteRepository(
                        noteDb = noteDb,
                        archiveDb = archiveDb,
                        binDb = binDb
                    )) as T
                }
            }
        }
    )

    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            val jsonString = importExportViewModel.importExportState.value.exportJson

            try {
                contentResolver.openOutputStream(it)?.bufferedWriter().use { writer ->
                    writer?.write(jsonString)
                }
                Toast.makeText(this, "Backup saved successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save backup", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { it ->
            Log.d("ImportExportViewModel", "start readFromFile")
            try {
                contentResolver.openInputStream(it)?.bufferedReader().use { reader ->
                    val jsonString = reader?.readText()
                    jsonString?.let {
                        Log.d("ImportExportViewModel", "Json String read from file: $jsonString")
                        importExportViewModel.restoreBackup(it)
                        Toast.makeText(this, "Backup restored!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            FinotesTheme {
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                navController = navController,
                                viewModel = viewModel,
                            )
                        }
                        composable<ArchiveRoute> {
                            ArchiveScreen(
                                navController = navController,
                                viewModel = viewModel,
                            )
                        }
                        composable<BinRoute> {
                            BinScreen(
                                navController = navController,
                                viewModel = viewModel,
                            )
                        }
                        composable<NoteRoute> {
                            NoteScreen(
                                navController = navController,
                                viewModel = viewModel,
                            )
                        }
                        composable<ExportImportRoute> {
                            ExportImportScreen(
                                navControllerMain = navController,
                                iEviewModel = importExportViewModel,
                                createFileLauncher = createFileLauncher,
                                pickFileLauncher = pickFileLauncher,
                            )
                        }
                    }
                }
            }
        }
    }

// main routes
@Serializable object HomeRoute
@Serializable object ArchiveRoute
@Serializable object BinRoute
@Serializable object NoteRoute
@Serializable object ExportImportRoute