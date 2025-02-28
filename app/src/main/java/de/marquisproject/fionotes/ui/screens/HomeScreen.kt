package de.marquisproject.fionotes.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import de.marquisproject.fionotes.NoteRoute
import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.ui.components.TopBarHome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
                            body = "This is a new note",
                            lastEdited = System.currentTimeMillis(),
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
        LazyVerticalStaggeredGrid(
            modifier = Modifier.padding(innerPadding),
            columns = StaggeredGridCells.Adaptive(200.dp),
            content = {
                items(uiState.notesList) { note ->
                    Card(
                        modifier = Modifier
                            .padding(3.dp)
                            .clickable(
                                onClick = {
                                    navController.navigate(NoteRoute(id = note.id))
                                }
                            )
                    ) {
                        Text(text = note.title, style = MaterialTheme.typography.titleLarge)
                        Text(text = note.body, style = MaterialTheme.typography.bodySmall)
                        Text(text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.lastEdited)))
                    }
                }
            }
        )
    }
}