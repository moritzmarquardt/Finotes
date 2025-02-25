package de.marquisproject.fionotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.marquisproject.fionotes.ui.theme.FionotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var searchQuery by remember { mutableStateOf("") }
            val navController = rememberNavController()
            FionotesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomBar(
                            navController = navController
                        )
                    },
                    topBar = {
                        TopBar(
                            searchQuery = searchQuery,
                            onQueryChange = { searchQuery = it }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { navController.navigate("note") },
                            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                        ) {
                            Icon(Icons.Filled.Add, "Localized description")
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(
                            searchQuery = searchQuery,
                        ) }
                        composable("bin") { BinScreen() }
                        composable("archive") { ArchiveScreen() }
                        composable("settings") { SettingsScreen() }
                        composable("note") { NoteView()}
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    searchQuery: String,
) {
    Text(text = "Home Screen with search query: $searchQuery")
}

@Composable
fun BinScreen() {
    Text(text = "Bin Screen")
}

@Composable
fun ArchiveScreen() {
    Text(text = "Archive Screen")
}

@Composable
fun SettingsScreen() {
    Text(text = "Settings Screen")
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun NoteView() {
    val noteId = "123"
    Text(text = "Note View with id: $noteId")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester()}
    var isFocused by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                value = searchQuery,
                onValueChange = onQueryChange,
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                placeholder = { Text("Search Fionotes") },
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
            )

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun BottomBar(
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
                onClick = { navController.navigate("home") }
            )
            NavigationBarItem(
                icon = {
                    Icon(Icons.Outlined.Delete, "Localized description")
                },
                label = { Text("Bin") },
                selected = currentDestination == "bin",
                onClick = { navController.navigate("bin") }
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.inventory_2_24dp),
                        "Localized description")
                },
                label = { Text("Archived") },
                selected = currentDestination == "archive",
                onClick = { navController.navigate("archive") }
            )
            NavigationBarItem(
                icon = {
                    Icon(Icons.Filled.Settings, "Localized description")
                },
                label = { Text("Settings") },
                selected = currentDestination == "settings",
                onClick = { navController.navigate("settings") }
            )
        }
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FionotesTheme {
        Greeting("Android")
    }
}