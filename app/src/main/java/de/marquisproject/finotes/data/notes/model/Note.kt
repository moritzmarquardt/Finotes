package de.marquisproject.finotes.data.notes.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NoteStatus {
    ACTIVE,
    ARCHIVED,
    BINNED
}

@Entity(tableName = "notes_table", indices = [Index(value = ["noteStatus"])])
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
     * The entity has a index now for the noteStatus which means that the database will automatically
     * store and update an index of the noteStatus for every note which will make accesses for
     * the noteStatus faster.
     *
     * Important: If the type of a Field is changed, the App will crash because the db still has the old type.
     * Therefore the App has to be uninstalled and reinstalled to recreate the db. Or a migration has to be created.
     */
    @PrimaryKey(autoGenerate = true) val id: Long = -1L,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "body") val body: String = "",
    @ColumnInfo(name = "dateCreated") val dateCreated: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "isPinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "noteStatus") val noteStatus: NoteStatus = NoteStatus.ACTIVE,
    @ColumnInfo(name = "category") val category: String = "",
    @ColumnInfo(name = "lastModified") val lastModified: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "needsSync") val needsSync: Boolean = false,
    @ColumnInfo(name = "remoteId") val remoteId: Long = -1L
)