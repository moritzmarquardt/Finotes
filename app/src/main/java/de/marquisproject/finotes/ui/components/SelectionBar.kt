package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars // Import statusBars
import androidx.compose.foundation.layout.windowInsetsPadding // Import windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBar(
    numSelected: Int,
    onSelectionClear: () -> Unit,
    actionButtons: List<Pair<Painter, () -> Unit>>,
) {
    TopAppBar(
        title = {
            Text(
                text = "$numSelected",
            )
        },
        navigationIcon = {
            IconButton(onClick = onSelectionClear) {
                Icon(Icons.Default.Clear, contentDescription = "Clear selection")
            }
        },
        actions = {
            actionButtons.forEach {
                IconButton(onClick = it.second) {
                    Icon(it.first, contentDescription = "Action")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class) // This annotation is for Material 3 components like FilterChip
@Composable
fun SelectionBarTabs(
    numSelected: Int,
    onSelectionClear: () -> Unit,
    actionButtons: List<Pair<Painter, () -> Unit>>,
    chips: List<Triple<String, () -> Unit, Boolean>>,
) {
    // Use Surface to provide a background color and elevation, making it visually similar to a TopAppBar.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background, // Set background color as per your original TopAppBar
        shadowElevation = 3.dp // Apply a subtle shadow for elevation, making it stand out
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars) // Apply padding for status bar insets
        ) {
            // First Row: Contains the clear selection icon, the number of selected items, and action buttons.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // Standard height for a compact app bar row
                    .padding(horizontal = 4.dp), // Add horizontal padding for content alignment
                verticalAlignment = Alignment.CenterVertically, // Vertically center items in this row
                horizontalArrangement = Arrangement.SpaceBetween // Distribute space between leading and trailing elements
            ) {
                // Group the clear icon and selection count text together
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSelectionClear) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear selection",
                            tint = MaterialTheme.colorScheme.onBackground // Apply theme's onBackground color
                        )
                    }
                    Text(
                        text = "$numSelected",
                        style = MaterialTheme.typography.headlineSmall, // Use a headline style for the count
                        color = MaterialTheme.colorScheme.onBackground, // Apply theme's onBackground color
                        modifier = Modifier.padding(start = 8.dp) // Padding between icon and text
                    )
                }

                // Row to hold your action buttons
                Row {
                    actionButtons.forEach {
                        IconButton(onClick = it.second) {
                            Icon(
                                it.first,
                                contentDescription = "Action",
                                tint = MaterialTheme.colorScheme.onBackground // Apply theme's onBackground color
                            )
                        }
                    }
                }
            }

            // Second Row: Horizontally scrollable chips, spanning the full width.
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp), // Add padding below the chips row for visual spacing
                contentPadding = PaddingValues(horizontal = 16.dp), // Padding for the content within the LazyRow
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between individual chips
            ) {
                items(chips) { chipData ->
                    FilterChip(
                        selected = chipData.third, // You can make this dynamic if you need to indicate selection state
                        onClick = chipData.second, // Action when the chip is clicked
                        label = { Text(chipData.first) } // Text displayed on the chip
                    )
                }
            }
        }
    }
}
