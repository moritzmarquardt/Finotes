package de.marquisproject.finotes.data.ncapi

import android.content.Context
import com.google.gson.GsonBuilder
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.model.SingleSignOnAccount
import retrofit2.NextcloudRetrofitApiBuilder

/**
 * This is an object class that provides instances of Apis.
 */

/**class ApiProvider private constructor() {
    fun getNotesAPI(
        context: Context,
        ssoAccount: SingleSignOnAccount,
    ): NotesApi? {
        // The NextcloudAPI object from the SSO library handles authentication.
        val nextcloudAPI = NextcloudAPI(context, ssoAccount, GsonBuilder().create())
        return NextcloudRetrofitApiBuilder(nextcloudAPI, "/index.php/apps/notes/api/v1/")
            .create(NotesApi::class.java)
    }

    companion object {
        @get:Synchronized
        var instance: ApiProvider? = null
            get() {
                if (field == null) {
                    field = ApiProvider()
                }
                return field
            }
            private set
    }
}*/


/**
 * A singleton object that provides an instance of the NotesApi.
 * Using an 'object' declaration is the recommended way to create singletons in Kotlin.
 */
object ApiProvider {
    fun getNotesAPI(
        context: Context,
        ssoAccount: SingleSignOnAccount,
    ): NotesApi? {
        // The NextcloudAPI object from the SSO library handles authentication.
        val nextcloudAPI = NextcloudAPI(context, ssoAccount, GsonBuilder().create())
        return NextcloudRetrofitApiBuilder(nextcloudAPI, "/index.php/apps/notes/api/v1/")
            .create(NotesApi::class.java)
    }
}