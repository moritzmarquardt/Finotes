package de.marquisproject.finotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.marquisproject.finotes.ui.theme.ThemeVariant
import de.marquisproject.finotes.ui.theme.ThemeVariantMap
import de.marquisproject.finotes.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
) {
    val themeVariant = viewModel.themeVariant.collectAsState()

    Scaffold(
        topBar = {TopAppBar(
            title = {
                Text(
                    text = "Settings",
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )}
    ) { innerPadding ->
        Column(
            modifier = Modifier.selectableGroup()
                .padding(innerPadding)
        ) {
            Text(
                text = "Select your preferred theme",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
            ThemeVariant.entries.forEach { themeV ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .selectable(
                            selected = themeVariant.value == themeV,
                            onClick = {
                                viewModel.saveThemeVariant(themeV)
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        val themename = themeV.name.lowercase().replaceFirstChar { it.uppercase() } + "'s theme"
                        RadioButton(
                            selected = themeVariant.value == themeV,
                            onClick = null
                        )
                        Text(
                            text = themename,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // show three main colours as a palette of boxes
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .width(150.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val primaryColorLight = ThemeVariantMap[themeV]?.primaryLight
                        val primaryColorDark = ThemeVariantMap[themeV]?.primaryDark
                        val secondaryColorLight = ThemeVariantMap[themeV]?.secondaryLight
                        val secondaryColorDark = ThemeVariantMap[themeV]?.secondaryDark
                        val tertiaryColorLight = ThemeVariantMap[themeV]?.tertiaryLight
                        val tertiaryColorDark = ThemeVariantMap[themeV]?.tertiaryDark

                        Column {
                            Row {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .background(color = primaryColorLight ?: MaterialTheme.colorScheme.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .background(color = secondaryColorLight ?: MaterialTheme.colorScheme.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .background(color = tertiaryColorLight ?: MaterialTheme.colorScheme.primary)
                                )
                            }
                            Row {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .background(color = primaryColorDark ?: MaterialTheme.colorScheme.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .background(color = secondaryColorDark ?: MaterialTheme.colorScheme.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .background(color = tertiaryColorDark ?: MaterialTheme.colorScheme.primary)
                                )

                            }
                        }
                    }

                }

            }
        }

    }

}