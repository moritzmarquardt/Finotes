package de.marquisproject.finotes.testutils

import android.content.ContentValues
import de.marquisproject.finotes.data.notes.model.NoteStatus
import kotlinx.serialization.Serializable

/**
 * Legacy Note data class representing Database V1 schema structure.
 * Used as a helper data structure in database migration tests.
 */
@Serializable
data class LegacyNoteV1(
    /**
     * this differs from the actual legacy implementation which was val id: Long = -1L.
     * But it is necessary because JSON deserialization assigns the default -1L which triggers
     * the conflict replace, and then it does not make sense anymore.
     * For some reason this only happens in the tests because they are all inserted at the same time
      */
    val id: Long? = null,
    val title: String = "",
    val body: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val noteStatus: NoteStatus = NoteStatus.ACTIVE,
    val color: Int = 0,
) {
    fun toContentValues(): ContentValues = ContentValues().apply {
        put("id", id)
        put("title", title)
        put("body", body)
        put("dateCreated", dateCreated)
        put("isPinned", isPinned)
        put("noteStatus", noteStatus.name)
        put("color", color)
    }
}