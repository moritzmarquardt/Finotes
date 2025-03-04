package de.marquisproject.fionotes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.marquisproject.fionotes.NoteRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinScreen(
    navController: NavController,
    viewModel: MainViewModel
) {

    val uiState by viewModel.uiState.collectAsState()


    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Bin")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back press")
                    }
                },

                )
        }
    ) { innerPadding ->
        if (uiState.binList.isEmpty()) {
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                Text(
                    text = "No binned notes",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        LazyVerticalStaggeredGrid(
            modifier = Modifier.padding(innerPadding),
            columns = StaggeredGridCells.Adaptive(200.dp),
            content = {
                items(uiState.binList) { note ->
                    OutlinedCard(
                        modifier = Modifier
                            .padding(3.dp)
                            .clickable(
                                onClick = {
                                    viewModel.setCurrentNoteId(note.id)
                                    viewModel.setCurrentNote(note)
                                    navController.navigate(NoteRoute)
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (note.title.isNotBlank()) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (note.body.isNotBlank()) {
                                Text(
                                    text = note.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 7,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}