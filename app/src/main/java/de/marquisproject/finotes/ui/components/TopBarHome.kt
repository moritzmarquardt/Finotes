package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import de.marquisproject.finotes.ArchiveRoute
import de.marquisproject.finotes.BinRoute
import de.marquisproject.finotes.ExportImportRoute
import de.marquisproject.finotes.NavItem
import de.marquisproject.finotes.R
import de.marquisproject.finotes.SettingsRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarHome(
    navController: NavController,
    updateQuery: (String) -> Unit,
    searchQuery: String,
) {
    var expandedMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusable()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            updateQuery("")
                        }
                                    },
                shape = RoundedCornerShape(50),
                value = searchQuery,
                onValueChange = updateQuery,
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            updateQuery("")
                        }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                placeholder = { Text("Search Finotes") },
                singleLine = true,
                maxLines = 1,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
        },
        actions = {
            IconButton(onClick = { expandedMenu = !expandedMenu }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                val navItems = listOf(
                    NavItem(ArchiveRoute, "Archive", R.drawable.inventory_2_24dp),
                    NavItem(BinRoute, "Bin", R.drawable.baseline_delete_24),
                    NavItem(ExportImportRoute, "Export/Import", R.drawable.baseline_import_export_24),
                    NavItem(SettingsRoute, "Settings", R.drawable.baseline_settings_24)
                )
                navItems.forEach { navItem ->
                    DropdownMenuItem(
                        modifier = Modifier.padding(end = 20.dp),
                        text = { Text(navItem.label) },
                        onClick = {
                            expandedMenu = false
                            navController.navigate(navItem.route)
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(id = navItem.iconPainterResource),
                                contentDescription = navItem.label
                            )
                        }
                    )

                }
            }
        }
    )
}