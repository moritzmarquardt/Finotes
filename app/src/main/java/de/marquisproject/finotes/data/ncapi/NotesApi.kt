package de.marquisproject.finotes.data.ncapi

import com.nextcloud.android.sso.api.EmptyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotesApi {
    /**
     * I wanted to use suspend functions that return Response object,
     * but nextcloud sso library does not support suspense calls to retrofit
     * which would be the most modern way. SO here I use the slightly older
     * way of making functions non-suspend that cannot return Response objects
     * but just Calls or Observables. Since Calls are simpler I use them.
     * TODO("Extend SSO library to support suspend calls")
     */
    @GET("notes")
    fun getNotes(): Call<List<NextcloudNote>>

    @GET("notes?exclude=etag,readonly,content,title,category,favorite,modified")
    fun getNotesIDs(): Call<List<NextcloudNote>>

    @POST("notes")
    fun createNote(@Body note: NextcloudNote): Call<NextcloudNote>

    @GET("notes/{remoteId}")
    fun getNote(@Path("remoteId") remoteId: Long): Call<NextcloudNote>

    @PUT("notes/{remoteId}")
    fun editNote(@Path("remoteId") remoteId: Long, @Body note: NextcloudNote): Call<NextcloudNote>

    @DELETE("notes/{remoteId}")
    fun deleteNote(@Path("remoteId") noteId: Long): Call<EmptyResponse>
}