package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.data.notes.model.Category

/**
 * A horizontal category picker that filters categories based on an external search query.
 */
@Composable
fun CategoryPicker(
    categories: List<Category>,
    selectedCategories: Set<Long>,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Only show the picker if there are categories matching the search (or no search)
    if (filteredCategories.isNotEmpty() || searchQuery.isBlank()) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(filteredCategories, key = { it.id }) { category ->
                val isSelected = selectedCategories.contains(category.id)
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(category.color).copy(alpha = 0.7f),
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(category.color)
                    )
                )
            }
        }
    }
}

/**
 * A reusable category selection dropdown menu.
 */
@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategoryId: Long?, // null if mixed, 0 if none, else category id
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = if (selectedCategoryId == null || selectedCategoryId == 0L) {
                    Icons.AutoMirrored.Outlined.Label
                } else {
                    Icons.AutoMirrored.Filled.Label
                },
                contentDescription = "Select Category",
                tint = tint
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("No Category") },
                onClick = {
                    onCategorySelected(0L)
                    expanded = false
                },
                leadingIcon = {
                    if (selectedCategoryId == 0L) Icon(Icons.Default.Check, null)
                }
            )
            if (categories.isNotEmpty()) HorizontalDivider()
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(category.color), CircleShape)
                        )
                    },
                    trailingIcon = {
                        if (selectedCategoryId == category.id) {
                            Icon(Icons.Default.Check, null)
                        }
                    },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
