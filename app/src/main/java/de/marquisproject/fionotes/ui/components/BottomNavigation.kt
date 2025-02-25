package de.marquisproject.fionotes.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.marquisproject.fionotes.HomeRoute
import de.marquisproject.fionotes.R
import de.marquisproject.fionotes.SettingsRoute


@Composable
fun BottomNavigation(
    navController: NavController
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    BottomAppBar(
        actions = {
            NavigationBarItem(
                icon = {
                    Icon(Icons.Outlined.Home, "Localized description")
                },
                label = { Text("Notes") },
                selected = currentDestination == "home",
                onClick = { navController.navigate(HomeRoute(
                    filter="all"
                )) }
            )
            NavigationBarItem(
                icon = {
                    Icon(Icons.Outlined.Delete, "Localized description")
                },
                label = { Text("Bin") },
                selected = currentDestination == "bin",
                onClick = { navController.navigate(HomeRoute(
                    filter="bin"
                )) }
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.inventory_2_24dp),
                        "Localized description")
                },
                label = { Text("Archived") },
                selected = currentDestination == "archive",
                onClick = { navController.navigate(HomeRoute(
                    filter="archive"
                )) }
            )
            NavigationBarItem(
                icon = {
                    Icon(Icons.Filled.Settings, "Localized description")
                },
                label = { Text("Settings") },
                selected = currentDestination == "settings",
                onClick = { navController.navigate(SettingsRoute) }
            )
        }
    )
}