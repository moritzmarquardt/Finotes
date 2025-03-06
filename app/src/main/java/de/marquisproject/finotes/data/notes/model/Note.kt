package de.marquisproject.finotes.data.notes.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteStatus {
    ACTIVE,
    ARCHIVED,
    BINNED
}

@Entity(tableName = "notes_table")
data class Note(
    /**
     * @property id: unique identifier for the note
     * @property title: title of the note
     * @property body: content of the note
     * @property dateCreated: timestamp of the creation date as Long
     * @property isPinned: boolean indicating if the note is pinned
     * @property noteStatus: status of the note as NoteStatus so either ACTIVE, ARCHIVED or BINNED
     * @property color: color of the note as Int
     *
     * Important: If the type of a Field is changed, the App will crash because the db still has the old type.
     * Therefore the App has to be uninstalled and reinstalled to recreate the db.
     */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "body") val body: String = "",
    @ColumnInfo(name = "dateCreated") val dateCreated: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "isPinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "noteStatus") val noteStatus: NoteStatus = NoteStatus.ACTIVE,
    @ColumnInfo(name = "color") val color: Int = 0,
)