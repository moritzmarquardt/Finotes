package de.marquisproject.fionotes.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import de.marquisproject.fionotes.ArchiveRoute
import de.marquisproject.fionotes.BinRoute
import de.marquisproject.fionotes.HomeRoute
import de.marquisproject.fionotes.R
import de.marquisproject.fionotes.SettingsRoute


class NavItem(
    val icon: @Composable () -> Unit,
    val label: @Composable () -> Unit,
    val route: Any
)


@Composable
fun BottomNavBar(
    navController: NavController
) {
    val navItems = listOf(
        NavItem(
            icon = { Icon(Icons.Outlined.Home, "Localized description") },
            label = { Text("Notes") },
            route = HomeRoute
        ),
        NavItem(
            icon = { Icon(Icons.Outlined.Delete, "Localized description") },
            label = { Text("Bin") },
            route = BinRoute
        ),
        NavItem(
            icon = { Icon(painterResource(id = R.drawable.inventory_2_24dp), "Localized description") },
            label = { Text("Archived") },
            route = ArchiveRoute
        ),
        NavItem(
            icon = { Icon(Icons.Filled.Settings, "Localized description") },
            label = { Text("Settings") },
            route = SettingsRoute
        ),
    )
    val selectedItem = remember { mutableIntStateOf(0) }


    NavigationBar {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = item.icon,
                label = item.label,
                selected = selectedItem.intValue == index,
                onClick = {
                    selectedItem.intValue = index
                    navController.navigate(route = item.route)
                },
            )
        }
    }
}