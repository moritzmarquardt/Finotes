package de.marquisproject.finotes.ui.screens

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.AccountImporter.extractSingleSignOnAccountFromResponse
import com.nextcloud.android.sso.Constants
import com.nextcloud.android.sso.FilesAppTypeRegistry
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountPermissionNotGrantedException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import de.marquisproject.finotes.data.ncapi.ApiProvider
import de.marquisproject.finotes.data.ncapi.NextcloudNote
import de.marquisproject.finotes.data.ncapi.NotesApi
import de.marquisproject.finotes.data.ncapi.NotesRepository
import de.marquisproject.finotes.ui.viewmodels.SyncViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response


@Composable
fun SyncScreen() {
    val syncViewModel: SyncViewModel = hiltViewModel()
    val context = LocalContext.current
    val ssoAccount = syncViewModel.ssoAccount
    val syncState = syncViewModel.syncState.collectAsState()

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
            Text("Current account: ${ssoAccount.name}")
        }
        Text("Sync state: $syncState")

        Button(
            onClick = {
                syncViewModel.viewModelScope.launch(Dispatchers.IO) {
                    val notesAPI: NotesApi? = ApiProvider.getNotesAPI(
                        context,
                        ssoAccount!!,
                    )
                    val call = notesAPI?.getNotes()
                    val response = call?.execute()
                    Log.e("SyncScreen", "response: $response")
                    if (response != null && response.isSuccessful) {
                        val notesList = response.body() // This is your list of notes
                        if (notesList != null) {
                            Log.d("SyncScreen", "Successfully fetched ${notesList.size} notes.")
                            // You can now process the list of notes
                            notesList.forEach { note ->
                                Log.d("SyncScreen", "Note: $note")
                            }
                        } else {
                            Log.e("SyncScreen", "Response body is null")
                        }
                    } else {
                        // Handle API error
                        val errorBody = response?.errorBody()?.string()
                        Log.e("SyncScreen", "API Error: ${response?.code()} - ${response?.message()} - $errorBody")
                    }
                }
            }
        ) {
            Text("Sync notes")
        }
    }


}
