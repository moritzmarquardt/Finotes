package de.marquisproject.finotes.testutils

import de.marquisproject.finotes.data.notes.model.NoteStatus
import kotlinx.serialization.Serializable

@Serializable
data class LegacyNoteV1(
    /**
     * @property id: unique identifier for the note
     * @property title: title of the note
     * @property body: content of the note
     * @property dateCreated: timestamp of the creation date as Long
     * @property isPinned: boolean indicating if the note is pinned
     * @property noteStatus: status of the note as NoteStatus so either ACTIVE, ARCHIVED or BINNED
     * @property color: color of the note as Int
     *
     * Important: If the type of Field is changed, the App will crash because the db still has the old type.
     * Therefore, the App has to be uninstalled and reinstalled to recreate the db.
     */
    val id: Long = -1L,
    val title: String = "",
    val body: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val noteStatus: NoteStatus = NoteStatus.ACTIVE,
    val color: Int = 0,
)