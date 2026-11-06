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
     * This data class is the exact implementation which was used for production and is now legacy.
     * See [toContentValues] below for the id speciality with test sqlite databases.
      */
    val id: Long = -1L,
    val title: String = "",
    val body: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val noteStatus: NoteStatus = NoteStatus.ACTIVE,
    val color: Int = 0,
) {
    fun toContentValues(): ContentValues = ContentValues().apply {
        /**
         * Since the test is run with a sqlite database and not a room database, negative ids are handled different.
         * In a room database, negative ids are handled as nulls and auto ids are generated. In sqlite negative ids
         * such as -1L are just inserted and if many notes have -1L as their id since it is the default value and default
         * values are used when deserializing the notes from the JSON, then these will overwrite each other.
         * Therefore, this if check has to be added to mimic the actual room behavior.
         */
        if (id != -1L) put("id", id)
        put("title", title)
        put("body", body)
        put("dateCreated", dateCreated)
        put("isPinned", isPinned)
        put("noteStatus", noteStatus.name)
        put("color", color)
    }
}