package de.marquisproject.finotes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBar(
    numSelected: Int,
    onSelectionClear: () -> Unit,
    actionButtons: List<Pair<Painter, () -> Unit>>,
) {
    TopAppBar(
        title = {
            Text(text = "$numSelected")
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
        }
    )
}