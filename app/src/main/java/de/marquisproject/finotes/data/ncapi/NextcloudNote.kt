package de.marquisproject.finotes.data.ncapi

import com.google.gson.annotations.SerializedName
import de.marquisproject.finotes.data.notes.model.Note

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

/**
 * Extension function to convert a NextcloudNote (API model) to a Note (Domain model).
 * Bijective translation: Content's first line becomes the title, the rest becomes the body.
 */
fun NextcloudNote.toNote(): Note {
    val lines = content.lines()
    val title = lines.firstOrNull() ?: ""
    val body = if (lines.size > 1) lines.drop(1).joinToString("\n") else ""

    return Note(
        remoteId = id,
        title = title,
        body = body,
        lastModified = modifiedAt * 1000L, // Nextcloud uses seconds, we use millis
        isPinned = favorite,
        needsSync = false
    )
}