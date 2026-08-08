package de.marquisproject.finotes.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import de.marquisproject.finotes.R
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.ui.components.NoteCard
import de.marquisproject.finotes.ui.components.SelectionBar
import de.marquisproject.finotes.ui.components.TopBarHome
import de.marquisproject.finotes.ui.screens.AddNoteFAB
import de.marquisproject.finotes.ui.screens.ButtonFastSelection

/**
 * Global variable to change the theme of all previews.
 * Options: ThemeVariant.AU, ThemeVariant.MARQUI, ThemeVariant.MINION, ThemeVariant.MATERIAL
 */
val PREVIEW_THEME = ThemeVariant.AU

/**
 * Multi-preview annotation for Light and Dark modes.
 */
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews

/**
 * A container that applies the current PREVIEW_THEME and provides a Surface.
 */
@Composable
fun ThemePreviewContainer(content: @Composable () -> Unit) {
    FinotesTheme(themeVariant = PREVIEW_THEME) {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

/**
 * Sample notes for previews.
 */
val SAMPLE_NOTES = listOf(
    Note(id = 1, title = "Note 1", body = "This is the content of note 1", isPinned = true),
    Note(id = 2, title = "Note 2", body = "This is the content of note 2", isPinned = true),
    Note(id = 3, title = "Note 3", body = "This is the content of note 3", isPinned = false)
)

@ThemePreviews
@Composable
fun HomeScreenPreview() {
    ThemePreviewContainer {
        Scaffold(
            topBar = { TopBarHome(navController = rememberNavController(), searchQuery = rememberTextFieldState("note")) },
            floatingActionButton = { AddNoteFAB(onClick = {}) },
        ) { innerPadding ->
            LazyVerticalStaggeredGrid(
                modifier = Modifier.padding(innerPadding),
                columns = StaggeredGridCells.Adaptive(180.dp),
                content = {
                    items(
                        items = SAMPLE_NOTES,
                        key = { note -> requireNotNull(note.id) }
                    ) { note ->
                        val searchQuery = rememberTextFieldState("note")
                        NoteCard(
                            note = note,
                            searchQuery = searchQuery,
                            selected = false,
                            onClick = {},
                            onLongClick = {},
                        )
                    }
                }
            )
        }
    }
}

@ThemePreviews
@Composable
fun HomeScreenSelectionPreview() {
    ThemePreviewContainer {
        Scaffold(
            topBar = {
                SelectionBar(
                    numSelected = 1,
                    onSelectionClear = { },
                    actionButtons = listOf(
                        painterResource(id = R.drawable.outline_push_pin_24) to {},
                        painterResource(id = R.drawable.outline_archive_24) to {},
                        painterResource(id = R.drawable.outline_delete_24) to {}
                    )
                )
            },
            floatingActionButton = { AddNoteFAB(onClick = {}) },
        ) { innerPadding ->
            LazyVerticalStaggeredGrid(
                modifier = Modifier.padding(innerPadding),
                columns = StaggeredGridCells.Adaptive(180.dp),
                content = {
                    items(
                        items = SAMPLE_NOTES,
                        key = { note -> requireNotNull(note.id) }
                    ) { note ->
                        NoteCard(
                            note = note,
                            searchQuery = rememberTextFieldState(),
                            selected = note.id == 1L,
                            onClick = {},
                            onLongClick = {},
                        )
                    }
                }
            )
        }
    }
}

@ThemePreviews
@Composable
fun PreviewDropDownMenu() {
    ThemePreviewContainer {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            DropdownMenuItem(
                text = { Text("Archive") },
                onClick = {},
                leadingIcon = { Icon(painterResource(id = R.drawable.inventory_2_24dp), contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Bin") },
                onClick = { },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Export/Import") },
                onClick = {},
                leadingIcon = { Icon(painterResource(id = R.drawable.baseline_import_export_24), contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {},
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
            )
        }
    }
}

@ThemePreviews
@Composable
fun PreviewFastSelectionCarousel() {
    ThemePreviewContainer {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                item { ButtonFastSelection(onClick = {}, text = "Select non-duplicates", selected = false ) }
                item { ButtonFastSelection(onClick = {}, text = "Unselect all", icon = Icons.Default.Clear, selected = true) }
                item { ButtonFastSelection(onClick = {}, text = "Select all", selected = true) }
            }
        )
    }
}

@ThemePreviews
@Composable
fun PreviewCancelAndImportButton() {
    ThemePreviewContainer {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                ) { Text("Cancel") }
                Button(onClick = { }) { Text("Import selected Notes (3)") }
            }
        }
    }
}

@ThemePreviews
@Composable
fun PreviewTopBarHome() {
    ThemePreviewContainer {
        TopBarHome(
            navController = rememberNavController(),
            searchQuery = rememberTextFieldState(),
        )
    }
}

@ThemePreviews
@Composable
fun PreviewBottomNavImportExport() {
    ThemePreviewContainer {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(painterResource(id = R.drawable.outline_file_upload_24), contentDescription = null) },
                label = { Text("Export") },
                selected = true,
                onClick = {},
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                )
            )
            NavigationBarItem(
                icon = { Icon(painterResource(id = R.drawable.outline_file_download_24), contentDescription = null) },
                label = { Text("Import") },
                selected = false,
                onClick = { },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
fun PreviewTopBarNote() {
    ThemePreviewContainer {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = { }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(painterResource(id = R.drawable.outline_push_pin_24), contentDescription = "Pin")
                }
                IconButton(onClick = { }) {
                    Icon(painterResource(id = R.drawable.outline_archive_24), contentDescription = "Archive")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

@ThemePreviews
@Composable
fun PreviewSelectionBar() {
    ThemePreviewContainer {
        SelectionBar(
            numSelected = 3,
            onSelectionClear = { },
            actionButtons = listOf(
                painterResource(id = R.drawable.outline_push_pin_24) to {},
                painterResource(id = R.drawable.outline_archive_24) to {},
                painterResource(id = R.drawable.outline_delete_24) to {},
            )
        )
    }
}
