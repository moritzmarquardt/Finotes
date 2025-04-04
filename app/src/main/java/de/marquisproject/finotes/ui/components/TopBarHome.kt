package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
                        tint = MaterialTheme.colorScheme.tertiary
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
                    //focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    //unfocusedContainerColor = Color.Transparent,
                    //focusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
                    //unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    //cursorColor = MaterialTheme.colorScheme.onSurface,
                    //unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface,
                    //focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface,
                    //disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface,
                    //focusedTextColor = MaterialTheme.colorScheme.onPrimary
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
            //containerColor = MaterialTheme.colorScheme.background
        )
    )
}