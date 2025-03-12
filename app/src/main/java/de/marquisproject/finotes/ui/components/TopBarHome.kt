package de.marquisproject.finotes.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import de.marquisproject.finotes.ArchiveRoute
import de.marquisproject.finotes.BinRoute
import de.marquisproject.finotes.ExportImportRoute
import de.marquisproject.finotes.R


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
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .focusable()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            updateQuery("")
                        }
                    },
                value = searchQuery,
                onValueChange = updateQuery,
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
                placeholder = { Text("Search Finotes") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                )
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
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}