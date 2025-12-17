package de.marquisproject.finotes

import de.marquisproject.finotes.ui.screens.HomeScreen
import de.marquisproject.finotes.ui.screens.NoteScreen
import de.marquisproject.finotes.ui.screens.ExportImportScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.ui.screens.ArchiveScreen
import de.marquisproject.finotes.ui.screens.BinScreen
import de.marquisproject.finotes.ui.screens.SettingsScreen
import de.marquisproject.finotes.ui.theme.FinotesTheme
import de.marquisproject.finotes.ui.viewmodels.SettingsViewModel
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            FinotesTheme(
                themeVariant = settingsViewModel.themeVariant.collectAsState().value
            ) {
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    enterTransition = {
                        EnterTransition.None
                                      },
                    exitTransition = {
                        ExitTransition.None
                                     },
                ) {
                    composable<HomeRoute> {
                        HomeScreen(
                            navController = navController,
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
                    composable<SettingsRoute> {
                        SettingsScreen(
                            navController = navController,
                            viewModel = settingsViewModel,
                        )
                    }
                    composable<NoteRoute> {
                        NoteScreen(
                            navController = navController,
                        )
                    }
                    composable<ExportImportRoute> {
                        ExportImportScreen(
                            navControllerMain = navController,
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
@Serializable
data class NoteRoute(
    val noteId: Long? = null, // Default value for new note
    val noteStatus: NoteStatus,
)
@Serializable object ExportImportRoute
@Serializable object SettingsRoute


// Data class for navigation item
data class NavItem(
    val route: Any,
    val label: String,
    val iconPainterResource: Int,
)