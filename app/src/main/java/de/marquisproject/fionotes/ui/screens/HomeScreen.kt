package de.marquisproject.fionotes.ui.screens

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
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.ui.components.TopBarHome


@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    
    val uiState by viewModel.uiState.collectAsState()


    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBarHome(
                navController = navController,
                updateQuery = { viewModel.updateQuery(it) },
                searchQuery = uiState.searchQuery
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.upsertNote(
                        Note(
                            title = "New note",
                            content = "This is a new note",
                            lastEdited = "2021-10-10",
                            isPinned = false,
                            color = 0,
                        )
                    )
                    //navController.navigate(NoteRoute(id = "new"))
                },
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
            Text(text = "Home Screen with filter: ${uiState.filter} and search query: ${uiState.searchQuery}")
        }
        LazyColumn {
            for (note in uiState.notesList) {
                item {
                    Text(text = note.title)
                }
            }
        }
    }
}