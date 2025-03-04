package de.marquisproject.fionotes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBarDefaults
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.marquisproject.fionotes.NoteRoute
import de.marquisproject.fionotes.ui.components.NoteCard
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
                    viewModel.insertNewEmptyNote()
                    navController.navigate(NoteRoute)
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
                    NoteCard(
                        setCurrentNoteId = { viewModel.setCurrentNoteId(it) },
                        setCurrentNote = { viewModel.setCurrentNote(it) },
                        note = note,
                        navigate = { navController.navigate(NoteRoute) },
                        searchQuery = uiState.searchQuery
                    )

                }
            }
        )
    }
}