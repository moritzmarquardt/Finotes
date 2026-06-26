package de.marquisproject.finotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.marquisproject.finotes.data.notes.model.Category
import de.marquisproject.finotes.ui.theme.ThemeVariant
import de.marquisproject.finotes.ui.theme.ThemeVariantMap
import de.marquisproject.finotes.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
) {
    val themeVariant by viewModel.themeVariant.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEventFlow.collectLatest { event ->
            when (event) {
                is SettingsViewModel.SnackbarEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(ThemeVariant.entries) { themeV ->
                ThemeVariantItem(
                    themeV = themeV,
                    isSelected = themeVariant == themeV,
                    onSelect = { viewModel.saveThemeVariant(themeV) }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                }
            }

            items(categories) { category ->
                CategorySettingsItem(
                    category = category,
                    onEdit = { categoryToEdit = it },
                    onDelete = { viewModel.deleteCategory(it) }
                )
            }
        }
    }

    if (showAddCategoryDialog) {
        CategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, color ->
                viewModel.addCategory(name, color)
                showAddCategoryDialog = false
            }
        )
    }

    categoryToEdit?.let { category ->
        CategoryDialog(
            category = category,
            onDismiss = { categoryToEdit = null },
            onConfirm = { name, color ->
                viewModel.updateCategory(category.copy(name = name, color = color))
                categoryToEdit = null
            }
        )
    }
}

@Composable
fun ThemeVariantItem(
    themeV: ThemeVariant,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = null)
            val name = ThemeVariantMap[themeV]?.name
                ?: (themeV.name.lowercase().replaceFirstChar { it.uppercase() } + "'s theme")
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Palette Preview
        Row(modifier = Modifier.width(100.dp), horizontalArrangement = Arrangement.End) {
            val primary = ThemeVariantMap[themeV]?.primaryLight ?: Color.Gray
            val secondary = ThemeVariantMap[themeV]?.secondaryLight ?: Color.Gray
            Box(modifier = Modifier.size(24.dp).background(primary))
            Box(modifier = Modifier.size(24.dp).background(secondary))
        }
    }
}

@Composable
fun CategorySettingsItem(
    category: Category,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(category.color), CircleShape)
        )
        Text(
            text = category.name,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(onClick = { onEdit(category) }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = { onDelete(category) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
fun CategoryDialog(
    category: Category? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedColor by remember { mutableStateOf(category?.color ?: Color.Blue.toArgb()) }
    var showCustomPicker by remember { mutableStateOf(false) }

    val presetColors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.Yellow,
        Color.Magenta, Color.Cyan, Color.Gray, Color(0xFFFFA500) // Orange
    )

    // Sync HSV state with selectedColor
    val initialHsv = remember(selectedColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(selectedColor, hsv)
        hsv
    }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    // When selectedColor changes from outside (e.g. preset click), update HSV states
    LaunchedEffect(selectedColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(selectedColor, hsv)
        
        // Only update if the resulting color is different to avoid the "grayscale reset"
        val currentColor = Color.hsv(hue, saturation, value).toArgb()
        if (selectedColor != currentColor) {
            hue = hsv[0]
            saturation = hsv[1]
            value = hsv[2]
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Add Category" else "Edit Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pick a color", style = MaterialTheme.typography.labelLarge)
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(presetColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, CircleShape)
                                .clickable { 
                                    selectedColor = color.toArgb()
                                    showCustomPicker = false
                                }
                                .padding(2.dp)
                        ) {
                            if (selectedColor == color.toArgb()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White.copy(alpha = 0.5f), CircleShape)
                                )
                            }
                        }
                    }
                    item {
                        val isCustom = presetColors.none { it.toArgb() == selectedColor }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(if (isCustom) Color(selectedColor) else Color.Transparent, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { showCustomPicker = !showCustomPicker }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Palette, 
                                contentDescription = "Custom Color",
                                modifier = Modifier.size(20.dp),
                                tint = if (isCustom) {
                                    if (Color(selectedColor).luminance() > 0.5f) Color.Black else Color.White
                                } else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (showCustomPicker) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Hue Slider
                    Text("Hue: ${hue.toInt()}°", style = MaterialTheme.typography.labelSmall)
                    val hueGradient = remember {
                        Brush.horizontalGradient(
                            listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                        )
                    }
                    ColorSlider(
                        value = hue,
                        onValueChange = { 
                            hue = it
                            selectedColor = Color.hsv(hue, saturation, value).toArgb()
                        },
                        valueRange = 0f..360f,
                        gradient = hueGradient
                    )

                    // Saturation Slider
                    Text("Saturation: ${(saturation * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    val saturationGradient = remember(hue, value) {
                        Brush.horizontalGradient(
                            listOf(Color.hsv(hue, 0f, value), Color.hsv(hue, 1f, value))
                        )
                    }
                    ColorSlider(
                        value = saturation,
                        onValueChange = { 
                            saturation = it
                            selectedColor = Color.hsv(hue, saturation, value).toArgb()
                        },
                        gradient = saturationGradient
                    )

                    // Value Slider
                    Text("Value: ${(value * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    val valueGradient = remember(hue, saturation) {
                        Brush.horizontalGradient(
                            listOf(Color.Black, Color.hsv(hue, saturation, 1f))
                        )
                    }
                    ColorSlider(
                        value = value,
                        onValueChange = { 
                            value = it
                            selectedColor = Color.hsv(hue, saturation, value).toArgb()
                        },
                        gradient = valueGradient
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ColorSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    gradient: Brush
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(gradient, androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}
