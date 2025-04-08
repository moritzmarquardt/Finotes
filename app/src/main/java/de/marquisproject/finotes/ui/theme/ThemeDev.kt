package de.marquisproject.finotes.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import de.marquisproject.finotes.ArchiveRoute
import de.marquisproject.finotes.BinRoute
import de.marquisproject.finotes.ExportImportRoute
import de.marquisproject.finotes.R
import de.marquisproject.finotes.SettingsRoute
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.ui.components.NoteCard
import de.marquisproject.finotes.ui.components.TopBarHome
import de.marquisproject.finotes.ui.screens.AddNoteFAB
import de.marquisproject.finotes.ui.screens.ButtonFastSelection

/**
 * Preview for the Finotes app using the Fiona theme.
 */

@Preview
@Composable
fun HomeScreenPreview() {
    FinotesTheme {
        val dummyNoteList = listOf(
            Note(
                id = 1,
                title = "Note 1",
                body = "This is the content of note 1",
                isPinned = true,
                ),
            Note(
                id = 2,
                title = "Note 2",
                body = "This is the content of note 2",
                isPinned = true
            ),
        )
        Scaffold(
            topBar = { TopBarHome(navController = rememberNavController(), updateQuery = {}, searchQuery = "note") },
            floatingActionButton = { AddNoteFAB(onClick = {}) },
        ) { innerPadding ->
            LazyVerticalStaggeredGrid(
                modifier = Modifier.padding(innerPadding),
                columns = StaggeredGridCells.Adaptive(180.dp),
                content = {
                    items(
                        items = dummyNoteList,
                        key = { note -> note.id }
                    ) { note ->
                        var searchQuery = ""
                        if (note.id.toInt() !=1) { searchQuery = "note"}
                        NoteCard(
                            note = note,
                            searchQuery = searchQuery,
                            selected = note.id.toInt() == 1,
                            onClick = {},
                            onLongClick = {},
                        )
                    }
                }
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 100,
    heightDp = 100
)
@Composable
fun PreviewFAB() {
    FinotesTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .align(Alignment.Center)
            ) {
                AddNoteFAB(
                    onClick = {}
                )
            }
        }
    }
}


@Preview(
    showBackground = true,
    widthDp = 200,
    heightDp = 200
)
@Composable
fun PreviewDropDownmenu() {
    FinotesTheme {
        val navController = rememberNavController()
        var expandedMenu = true
        DropdownMenu(
            modifier = Modifier
                .height(200.dp)
                .width(200.dp),
            expanded = expandedMenu,
            onDismissRequest = { expandedMenu = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            DropdownMenuItem(
                text = { Text("Archive") },
                onClick = {
                    expandedMenu = false
                    navController.navigate(ArchiveRoute)
                },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.inventory_2_24dp), contentDescription = "Archive")
                }
            )
            DropdownMenuItem(
                text = { Text("Bin") },
                onClick = {
                    expandedMenu = false
                    navController.navigate(BinRoute)
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = "Bin")
                }
            )
            DropdownMenuItem(
                text = { Text("Export/Import notes") },
                onClick = {
                    expandedMenu = false
                    navController.navigate(ExportImportRoute)
                },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.baseline_import_export_24), contentDescription = "Bin")
                }
            )
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {
                    expandedMenu = false
                    navController.navigate(SettingsRoute)
                },
                leadingIcon = {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 100
)
@Composable
fun PreviewFastSelectionCarusel() {
    FinotesTheme {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                item {
                    ButtonFastSelection(onClick = {}, text = "Select non-duplicates", selected = false )
                }
                item {
                    ButtonFastSelection(onClick = {}, text = "Unselect all", icon = Icons.Default.Clear, selected = true)
                }
                item {
                    ButtonFastSelection(onClick = {}, text = "Select all", selected = true)
                }
            }
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 100
)
@Composable
fun PreviewCancelAndImportButton () {
    FinotesTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                enabled = true,
                onClick = {  },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {  },
                enabled = true,
            ) {
                Text("Import selected Notes (3)")
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 100
)
@Composable
fun PreviewTopBarHome () {
    FinotesTheme {
        TopBarHome(
            navController = rememberNavController(),
            updateQuery = {},
            searchQuery = "",
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 100
)
@Composable
fun PreviewBottomNavImportExport () {
    FinotesTheme {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            NavigationBarItem(
                icon = { Icon(painterResource(id = R.drawable.outline_file_upload_24), contentDescription = "Home") },
                label = { Text("Export") },
                selected = true,
                onClick = {},
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                )
            )
            NavigationBarItem(
                icon = { Icon(painterResource(id = R.drawable.outline_file_download_24), contentDescription = "Home") },
                label = { Text("Import") },
                selected = false,
                onClick = { },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                )
            )
        }
    }
}


// now the same for dark mode

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 100,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewNoteCardDark () {
    val note = Note(
        id = 1,
        title = "Test Note",
        body = "This is a test note with a lot to display",
        isPinned = true,
        noteStatus = NoteStatus.ACTIVE,
    )
    FinotesTheme {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            content = {
                Box(
                    modifier = Modifier.width(200.dp)
                ) {
                    NoteCard(
                        note = note,
                        searchQuery = "Test",
                        selected = false,
                        onClick = {},
                        onLongClick = {},
                        onSwipe = null,
                    )
                }
                Box(
                    modifier = Modifier.width(200.dp)
                ) {
                    NoteCard(
                        note = note,
                        searchQuery = "",
                        selected = true,
                        onClick = {},
                        onLongClick = {},
                        onSwipe = null,
                    )
                }
            }
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 100,
    heightDp = 100,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewFABDark() {
    FinotesTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .align(Alignment.Center)
            ) {
                AddNoteFAB(
                    onClick = {}
                )
            }
        }
    }
}


@Preview(
    widthDp = 200,
    heightDp = 200,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewDropDownmenuDark() {
    FinotesTheme {
        val navController = rememberNavController()
        var expandedMenu = true
        DropdownMenu(
            modifier = Modifier
                .height(200.dp)
                .width(200.dp),
            expanded = expandedMenu,
            onDismissRequest = { expandedMenu = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            DropdownMenuItem(
                text = { Text("Archive") },
                onClick = {
                    expandedMenu = false
                    navController.navigate(ArchiveRoute)
                },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.inventory_2_24dp), contentDescription = "Archive")
                }
            )
            DropdownMenuItem(
                text = { Text("Bin") },
                onClick = {
                    expandedMenu = false
                    navController.navigate(BinRoute)
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = "Bin")
                }
            )
            DropdownMenuItem(
                text = { Text("Export/Import notes") },
                onClick = {
                    expandedMenu = false
                    navController.navigate(ExportImportRoute)
                },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.baseline_import_export_24), contentDescription = "Bin")
                }
            )
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {
                    expandedMenu = false
                    navController.navigate(SettingsRoute)
                },
                leadingIcon = {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            )
        }
    }
}

@Preview(
    widthDp = 400,
    heightDp = 100,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewFastSelectionCaruselDark() {
    FinotesTheme {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                item {
                    ButtonFastSelection(onClick = {}, text = "Select non-duplicates", selected = false )
                }
                item {
                    ButtonFastSelection(onClick = {}, text = "Unselect all", icon = Icons.Default.Clear, selected = true)
                }
                item {
                    ButtonFastSelection(onClick = {}, text = "Select all", selected = true)
                }
            }
        )
    }
}

@Preview(
    widthDp = 400,
    heightDp = 100,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewCancelAndImportButtonDark () {
    FinotesTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                enabled = true,
                onClick = {  },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {  },
                enabled = true,
            ) {
                Text("Import selected Notes (3)")
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 100,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewTopBarHomeDark () {
    FinotesTheme {
        TopBarHome(
            navController = rememberNavController(),
            updateQuery = {},
            searchQuery = "",
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 100,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewBottomNavImportExportDark () {
    FinotesTheme {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            NavigationBarItem(
                icon = { Icon(painterResource(id = R.drawable.outline_file_upload_24), contentDescription = "Home") },
                label = { Text("Export") },
                selected = true,
                onClick = {},
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                )
            )
            NavigationBarItem(
                icon = { Icon(painterResource(id = R.drawable.outline_file_download_24), contentDescription = "Home") },
                label = { Text("Import") },
                selected = false,
                onClick = { },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                )
            )
        }
    }
}