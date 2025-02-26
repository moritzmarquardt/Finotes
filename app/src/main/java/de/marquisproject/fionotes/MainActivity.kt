package de.marquisproject.fionotes

// my own packages
import de.marquisproject.fionotes.ui.components.BottomNavBar
import de.marquisproject.fionotes.ui.screens.HomeScreen
import de.marquisproject.fionotes.ui.screens.SettingsScreen
import de.marquisproject.fionotes.ui.screens.NoteScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.marquisproject.fionotes.ui.theme.FionotesTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            FionotesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavBar(
                            navController = navController
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { navController.navigate(NoteRoute(
                                id = "new"
                            )) },
                            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                        ) {
                            Icon(Icons.Filled.Add, "Localized description")
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute(filter = "all"),
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<HomeRoute> {
                            val args = it.toRoute<HomeRoute>()
                            HomeScreen(
                                filter = args.filter,
                            )
                        }
                        composable<NoteRoute> {
                            val args = it.toRoute<NoteRoute>()
                            NoteScreen(
                                id = args.id,
                            )
                        }
                        composable<SettingsRoute> {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class HomeRoute(
    val filter : String = "all",
)

@Serializable
object SettingsRoute

@Serializable
data class NoteRoute (
    val id: String
)