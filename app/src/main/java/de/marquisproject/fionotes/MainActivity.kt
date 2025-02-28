package de.marquisproject.fionotes

import de.marquisproject.fionotes.ui.screens.HomeScreen
import de.marquisproject.fionotes.ui.screens.SettingsScreen
import de.marquisproject.fionotes.ui.screens.NoteScreen
import de.marquisproject.fionotes.data.notes.sources.NoteDatabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Room
import de.marquisproject.fionotes.data.notes.repositories.NoteRepository
import de.marquisproject.fionotes.ui.screens.ArchiveScreen
import de.marquisproject.fionotes.ui.screens.MainViewModel
import de.marquisproject.fionotes.ui.screens.BinScreen
import de.marquisproject.fionotes.ui.theme.FionotesTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            "note.db"
        ).build()
    }
    val viewModel by viewModels<MainViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(NoteRepository(db)) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            FionotesTheme {
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
                            )
                        }
                        composable<BinRoute> {
                            BinScreen(
                                navController = navController,
                            )
                        }
                        composable<NoteRoute> {
                            val args = it.toRoute<NoteRoute>()
                            NoteScreen(
                                navController = navController,
                                id = args.id,
                            )
                        }
                        composable<SettingsRoute> {
                            SettingsScreen(
                                navController = navController,
                            )
                        }
                    }
                }
            }
        }
    }


@Serializable
object HomeRoute

@Serializable
object ArchiveRoute

@Serializable
object BinRoute

@Serializable
object SettingsRoute

@Serializable
data class NoteRoute (
    val id: String
)