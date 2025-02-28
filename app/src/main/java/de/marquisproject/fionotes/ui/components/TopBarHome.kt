package de.marquisproject.fionotes.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import de.marquisproject.fionotes.ArchiveRoute
import de.marquisproject.fionotes.BinRoute
import de.marquisproject.fionotes.R
import de.marquisproject.fionotes.SettingsRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarHome(
    navController: NavController,
    updateQuery: (String) -> Unit,
    searchQuery: String,
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    CenterAlignedTopAppBar(
        title = {
            SearchBarDefaults.InputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .focusRequester(focusRequester)
                    .focusable(),
                query = searchQuery,
                onQueryChange = updateQuery,
                onSearch = {
                    focusManager.clearFocus()
                },
                expanded = false,
                onExpandedChange = { },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
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
                placeholder = { Text("Search Fionotes") },
            )

        },
        actions = {
            IconButton(onClick = { expandedMenu = !expandedMenu }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false }
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
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}