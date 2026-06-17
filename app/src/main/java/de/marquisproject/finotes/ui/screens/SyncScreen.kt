package de.marquisproject.finotes.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import de.marquisproject.finotes.ui.viewmodels.SyncState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted
import de.marquisproject.finotes.ui.components.NotesList
import de.marquisproject.finotes.ui.viewmodels.SyncViewModel
import de.marquisproject.finotes.utils.NoteSection


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    onNavigateBack: () -> Unit,
) {
    val syncViewModel: SyncViewModel = hiltViewModel()
    val context = LocalContext.current
    val ssoAccount by syncViewModel.ssoAccount.collectAsState()
    val syncState by syncViewModel.syncState.collectAsState()
    val notes by syncViewModel.notes.collectAsState()

    val authenticationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        syncViewModel.onAuthenticationResult(result)
    }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        syncViewModel.onAccountPickerResult(result, authenticationLauncher)
    }





    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Sync")
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(
                onClick = {
                    //CheckAndroidAccountPermissions
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        Log.e("SyncScreen", "check permissions")
                        // Do something for lollipop and above versions
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.GET_ACCOUNTS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.e("SyncScreen", "Permission not granted yet!")
                            throw AndroidGetAccountsPermissionNotGranted(context)
                        } else {
                            Log.e("SyncScreen", "Permission granted!")
                        }
                    } else {
                        Log.e("SyncScreen", "Permission already granted!")
                    }
                    //This is a modern Intent if you use the right keywords
                    val accountPickerIntent = syncViewModel.makeAccountPickerIntent()
                    accountPickerLauncher.launch(input = accountPickerIntent)
                }
            ) {
                Text("Pick Account")
            }

            if (ssoAccount != null) {
                Text("Current account: ${ssoAccount?.name}")
                Button(onClick = { syncViewModel.fetchNotes() }) {
                    Text("Fetch Notes")
                }
            }

            when (val state = syncState) {
                is SyncState.Syncing -> {
                    CircularProgressIndicator()
                    Text(state.message)
                }

                is SyncState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }

                else -> {}
            }

            NotesList(
                noteSections = listOf(
                    NoteSection(
                        title = "Nextcloud Notes",
                        notesList = notes.map { it.copy(id = it.remoteId) },
                    )
                )
            )
        }
    }
}
