package de.marquisproject.fionotes.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.marquisproject.fionotes.ui.components.TopBar

@Composable
fun HomeScreen(
    filter: String = "all",
) {
    var searchQuery by remember { mutableStateOf("") }
    Scaffold (
        topBar = {
            TopBar(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it }
            )
        }
    ) { innerPadding ->
        Card(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text(text = "Home Screen with filter: $filter and search query: $searchQuery")
        }
    }

}