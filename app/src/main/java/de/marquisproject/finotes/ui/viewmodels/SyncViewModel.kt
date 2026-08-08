package de.marquisproject.finotes.ui.viewmodels

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.AccountImporter.extractSingleSignOnAccountFromResponse
import com.nextcloud.android.sso.Constants
import com.nextcloud.android.sso.FilesAppTypeRegistry
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountPermissionNotGrantedException
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.data.ncapi.ApiProvider
import de.marquisproject.finotes.data.ncapi.NotesRepository
import de.marquisproject.finotes.data.ncapi.toNote
import de.marquisproject.finotes.data.notes.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SyncViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val repository = NotesRepository()
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    private val _ssoAccount = MutableStateFlow<SingleSignOnAccount?>(null)
    val ssoAccount: StateFlow<SingleSignOnAccount?> = _ssoAccount.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        try {
            val context = getApplication<Application>().applicationContext
            val account = SingleAccountHelper.getCurrentSingleSignOnAccount(context)
            _ssoAccount.value = account
            Log.d("SyncScreen", "Current account: $account")
        } catch (e: NoCurrentAccountSelectedException) {
            Log.d("SyncScreen", "No current account selected", e)
        }  catch (e: NextcloudFilesAppAccountNotFoundException) {
            Log.d("SyncScreen", "Nextcloud files app account not found", e)
        } catch (e: Exception) {
            Log.d("SyncScreen", "Error getting current account", e)
        }
    }

    fun setAccount(account: SingleSignOnAccount?) {
        _ssoAccount.value = account
    }

    fun onAuthenticationResult(result: ActivityResult) {
        val context = getApplication<Application>().applicationContext
        val ssoAccount: SingleSignOnAccount? = extractSingleSignOnAccountFromResponse(result.data, context)
        SingleAccountHelper.commitCurrentAccount(context, ssoAccount?.name)
        setAccount(ssoAccount)
    }

    fun onAccountPickerResult(result: ActivityResult, authenticationLauncher: ActivityResultLauncher<Intent>) {
        val context = getApplication<Application>().applicationContext
        if (result.resultCode == RESULT_OK) {
            val accountName: String? = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            val account = AccountImporter.getAccountForName(context, accountName)
                ?: throw NextcloudFilesAppAccountPermissionNotGrantedException(context)
            val authIntent = makeAuthIntent(account)
            try {
                authenticationLauncher.launch(authIntent)
            } catch (e: ActivityNotFoundException) {
                Log.e("SyncScreen", "Activity not found", e)
            }
        }
    }

    fun makeAccountPickerIntent(): Intent {
        val AUTH_TOKEN_SSO = "SSO"
        return AccountManager.newChooseAccountIntent(
            null,
            null,
            FilesAppTypeRegistry.getInstance().accountTypes,
            null,
            AUTH_TOKEN_SSO,
            null,
            null
        )
    }

    fun makeAuthIntent(account: Account): Intent {
        val componentName: String =
            FilesAppTypeRegistry.getInstance().findByAccountType(account.type).packageId
        val authIntent = Intent()
        authIntent.component = ComponentName(
            componentName,
            "com.owncloud.android.ui.activity.SsoGrantPermissionActivity"
        )
        authIntent.putExtra(Constants.NEXTCLOUD_FILES_ACCOUNT, account)
        return authIntent
    }

    fun fetchNotes() {
        val account = _ssoAccount.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _syncState.value = SyncState.Syncing("Fetching notes...")
            try {
                val notesAPI = ApiProvider.getNotesAPI(getApplication(), account)
                val response = notesAPI?.getNotes()?.execute()
                if (response?.isSuccessful == true) {
                    val nextcloudNotes = response.body() ?: emptyList()
                    _notes.value = nextcloudNotes.map { it.toNote() }
                    _syncState.value = SyncState.Idle
                } else {
                    _syncState.value = SyncState.Error("Failed to fetch notes: ${response?.message()}")
                }
            } catch (e: Exception) {
                Log.e("SyncViewModel", "Error fetching notes", e)
                _syncState.value = SyncState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val message: String) : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}
