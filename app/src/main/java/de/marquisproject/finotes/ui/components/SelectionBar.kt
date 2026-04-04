package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.ui.theme.FinotesTheme


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

@Composable
fun SelectionGroup(
    numSelected: Int,
    onSelectionClear: () -> Unit,
    actionButtons: List<Pair<Painter, () -> Unit>>,
    modifier: Modifier = Modifier,
    onSelectAll: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    OutlinedIconButton(onClick = onSelectAll, shape = MaterialTheme.shapes.medium) {
                        Icon(Icons.Default.SelectAll, contentDescription = "Select all")
                    }
                    OutlinedIconButton(onClick = onSelectionClear, shape = MaterialTheme.shapes.medium) {
                        Icon(Icons.Default.Deselect, contentDescription = "Clear selection")
                    }
                    Box(
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "$numSelected",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actionButtons.forEach { (painter, onClick) ->
                        OutlinedIconButton(
                            onClick = onClick,
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Icon(painter, contentDescription = "Action")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectionBarPreview() {
    FinotesTheme {
        SelectionBar(
            numSelected = 3,
            onSelectionClear = { },
            actionButtons = listOf(
                rememberVectorPainter(Icons.Default.Delete) to { },
                rememberVectorPainter(Icons.Default.Share) to { }
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelectionGroupPreview() {
    FinotesTheme {
        SelectionGroup(
            numSelected = 3,
            onSelectionClear = { },
            onSelectAll = { },
            actionButtons = listOf(
                rememberVectorPainter(Icons.Default.PinDrop) to { },
                rememberVectorPainter(Icons.Default.Delete) to { },
                rememberVectorPainter(Icons.Default.Archive) to { },
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}