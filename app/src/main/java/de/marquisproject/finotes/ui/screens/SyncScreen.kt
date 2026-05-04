package de.marquisproject.finotes.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.data.notes.model.Note
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import de.marquisproject.finotes.ui.viewmodels.SyncState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted
import de.marquisproject.finotes.ui.viewmodels.SyncViewModel


@Composable
fun SyncScreen() {
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







    Column {
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
                }
                else {
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

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(notes) { note ->
                NoteItem(note)
            }
        }
    }
}

@Composable
fun NoteItem(note: Note) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = note.title, style = MaterialTheme.typography.titleMedium)
            Text(text = note.body, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
    }
}
