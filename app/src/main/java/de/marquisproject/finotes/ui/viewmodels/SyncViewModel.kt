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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
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
import de.marquisproject.finotes.data.ncapi.NotesRepository
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
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
    var ssoAccount: SingleSignOnAccount? = null

    init {
        try {
            val context = getApplication<Application>().applicationContext
            ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context)
            Log.e("SyncScreen", "Current account: $ssoAccount")
            Log.e("SyncScreen", "Current account name: ${ssoAccount?.name}")
            Log.e("SyncScreen", "Current account type: ${ssoAccount?.type}")
            Log.e("SyncScreen", "auth token: ${ssoAccount?.token}")

            // val nextcloudAPI = NextcloudAPI(context, ssoAccount, GsonBuilder().create())
            // TODO: Use nextcloudAPI
        } catch (e: NoCurrentAccountSelectedException) {
            Log.e("SyncScreen", "No current account selected", e)
        }  catch (e: NextcloudFilesAppAccountNotFoundException) {
            Log.e("SyncScreen", "Nextcloud files app account not found", e)
        } catch (e: Exception) {
            Log.e("SyncScreen", "Error getting current account", e)
        }
    }

    fun setAccount(account: SingleSignOnAccount?) {
        ssoAccount = account
    }

    fun onAuthenticationResult(result: ActivityResult) {
        /**
         * This launcher takes the result from the auth intent from the pick account launcher
         * and commits the authorized account.
         */
        val context = getApplication<Application>().applicationContext
        val ssoAccount: SingleSignOnAccount? = extractSingleSignOnAccountFromResponse(result.data, context)
        SingleAccountHelper.commitCurrentAccount(context, ssoAccount?.name)
        setAccount(ssoAccount)
    }

    fun onAccountPickerResult(result: ActivityResult, authenticationLauncher: ActivityResultLauncher<Intent>) {
        /**
         * This activity launcher takes the selected account from the account picker
         *         and launches a activity that targets the nextcloud files app and asks to
         *         authenticate this account with a token. The result of this goes to the
         *         authentication launcher.
         */
        val context = getApplication<Application>().applicationContext
        if (result.resultCode == RESULT_OK) {
            val accountName: String? = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            val account = AccountImporter.getAccountForName(context, accountName)
            if (account == null) {
                throw NextcloudFilesAppAccountPermissionNotGrantedException(context)
            }
            val authIntent = makeAuthIntent(account)
            try {
                authenticationLauncher.launch(authIntent)
            } catch (e: ActivityNotFoundException) {
                Log.e("SyncScreen", "Activity not found", e)
            }
        }
    }

    fun makeAccountPickerIntent(): Intent {
        /**
         * The modern variation of newChooseAccountIntent changes the following arguments:
         *                 - The modern version removes the alwaysPromptForAccount parameter
         *                 - It uses the more general List<Account> interface instead of ArrayList<Account>
         */
        val AUTH_TOKEN_SSO = "SSO"
        val intent: Intent = AccountManager.newChooseAccountIntent(
            null,
            null,
            FilesAppTypeRegistry.getInstance().accountTypes,
            null,
            AUTH_TOKEN_SSO,
            null,
            null
        )
        return intent
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


}

// Sealed class to represent different states of the sync process.
sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val message: String) : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

