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

@Entity(tableName = Note.TABLE_NAME, indices = [Index(value = [Note.COLUMN_NOTE_STATUS])])
data class Note(
    /**
     * @property id: unique identifier for the note
     * @property title: title of the note
     * @property body: content of the note
     * @property category: category of the note
     * @property isPinned: boolean indicating if the note is pinned
     * @property dateCreated: timestamp of the creation date as Long
     * @property lastModified: timestamp of the last modification date as Long
     * @property noteStatus: status of the note as NoteStatus so either ACTIVE, ARCHIVED or BINNED
     * @property needsSync: boolean indicating if the note needs to be synced to the server
     * @property remoteId: unique identifier for the note on the server
     *
     * The entity has a index now for the noteStatus which means that the database will automatically
     * store and update an index of the noteStatus for every note which will make accesses for
     * the noteStatus faster.
     *
     * Important: If the type of a Field is changed, the App will crash because the db still has the old type.
     * Therefore the App has to be uninstalled and reinstalled to recreate the db. Or a migration has to be created.
     */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = COLUMN_TITLE) val title: String = "",
    @ColumnInfo(name = COLUMN_BODY) val body: String = "",
    @ColumnInfo(name = COLUMN_CATEGORY) val category: String = "",
    @ColumnInfo(name = COLUMN_IS_PINNED) val isPinned: Boolean = false,
    @ColumnInfo(name = COLUMN_DATE_CREATED) val dateCreated: Long = System.currentTimeMillis(),
    @ColumnInfo(name = COLUMN_LAST_MODIFIED) val lastModified: Long = System.currentTimeMillis(),
    @ColumnInfo(name = COLUMN_NOTE_STATUS) val noteStatus: NoteStatus = NoteStatus.ACTIVE,
    @ColumnInfo(name = COLUMN_NEEDS_SYNC) val needsSync: Boolean = false,
    @ColumnInfo(name = COLUMN_REMOTE_ID) val remoteId: Long? = null
) {
    companion object {
        const val TABLE_NAME = "notes_table"
        const val COLUMN_TITLE = "title"
        const val COLUMN_BODY = "body"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IS_PINNED = "isPinned"
        const val COLUMN_DATE_CREATED = "dateCreated"
        const val COLUMN_LAST_MODIFIED = "lastModified"
        const val COLUMN_NOTE_STATUS = "noteStatus"
        const val COLUMN_NEEDS_SYNC = "needsSync"
        const val COLUMN_REMOTE_ID = "remoteId"
    }

    /**
     * Returns a copy of the Note with the needsSync field set to true and the lastModified field set to the current time.
     */
    fun prepareForUpdate(): Note {
        return this.copy(
            lastModified = System.currentTimeMillis(),
            needsSync = true
        )
    }
}