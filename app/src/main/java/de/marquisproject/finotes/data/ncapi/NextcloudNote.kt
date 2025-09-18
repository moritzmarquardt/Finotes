package de.marquisproject.finotes.data.ncapi

import com.google.gson.annotations.SerializedName

// Data class to represent a single note from the API response.
data class NextcloudNote(
    val id: Long,
    val etag: String,
    val content: String,
    @SerializedName("modified_at")
    val modifiedAt: Long,
    val category: String?,
    val favorite: Boolean
)