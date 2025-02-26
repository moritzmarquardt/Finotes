package de.marquisproject.fionotes.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import de.marquisproject.fionotes.ui.components.TopBar

@Composable
fun HomeScreen(
    filter: String = "all",
    homeViewModel: HomeViewModel = viewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsState()

    Scaffold (
        topBar = {
            TopBar(
                searchQuery = homeUiState.searchQuery,
                onQueryChange = { homeViewModel.updateQuery(it) }
            )
        }
    ) { innerPadding ->
        Card(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text(text = "Home Screen with filter: $filter and search query: ${homeUiState.searchQuery}")
        }
    }
}