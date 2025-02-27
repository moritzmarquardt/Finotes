package de.marquisproject.fionotes.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.marquisproject.fionotes.NoteRoute
import de.marquisproject.fionotes.ui.components.BottomNavBar
import de.marquisproject.fionotes.ui.components.TopBar

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    navController: NavController,
) {
    val homeUiState by homeViewModel.uiState.collectAsState()

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                searchQuery = homeUiState.searchQuery,
                onQueryChange = { homeViewModel.updateQuery(it) }
            )
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(
                    NoteRoute(
                        id = "new"
                    )
                ) },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Icon(Icons.Filled.Add, "Localized description")
            }
        }
    ) { innerPadding ->
        Card(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text(text = "Home Screen with filter: ${homeUiState.filter} and search query: ${homeUiState.searchQuery}")
        }
    }
}