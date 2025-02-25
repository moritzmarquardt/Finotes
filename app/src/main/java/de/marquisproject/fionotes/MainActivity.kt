package de.marquisproject.fionotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import de.marquisproject.fionotes.ui.theme.FionotesTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FionotesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomAppBar(
                            actions = {
                                NavigationBarItem(
                                    icon = {
                                        Icon(Icons.Outlined.Home, "Localized description")
                                    },
                                    label = { Text("Notes") },
                                    selected = false,
                                    onClick = { }
                                )
                                NavigationBarItem(
                                    icon = {
                                        Icon(Icons.Outlined.Delete, "Localized description")
                                    },
                                    label = { Text("Bin") },
                                    selected = false,
                                    onClick = { }
                                )
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.inventory_2_24dp),
                                            "Localized description")
                                    },
                                    label = { Text("Archived") },
                                    selected = false,
                                    onClick = { }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            },
                            floatingActionButton = {
                                FloatingActionButton(
                                    onClick = { /* do something */ },
                                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                                ) {
                                    Icon(Icons.Filled.Add, "Localized description")
                                }
                            }
                        )
                    },
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text("Fionotes")
                            }
                        )
                    },
                ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun FAB(onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = { onClick() },
        containerColor = Color(252, 238, 131,255),
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
    ) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FionotesTheme {
        Greeting("Android")
    }
}