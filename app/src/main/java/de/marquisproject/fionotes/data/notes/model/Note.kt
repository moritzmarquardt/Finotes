package de.marquisproject.fionotes.data.notes.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class Note(
    /**
     * @param id: unique identifier for the note
     * @param title: title of the note
     * @param body: content of the note
     * @param lastEdited: timestamp of the last edit as Long
     * @param isPinned: boolean indicating if the note is pinned
     * @param color: color of the note as Int
     *
     * Important: If the type of a Field is changed, the App will crash because the db still has the old type.
     * Therefore the App has to be uninstalled and reinstalled to recreate the db.
     */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "content") val body: String = "",
    @ColumnInfo(name = "lastEdited") val lastEdited: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "isPinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "isArchived") val isArchived: Boolean = false,
    @ColumnInfo(name = "isBinned") val isBinned: Boolean = false,
    @ColumnInfo(name = "color") val color: Int = 0,
)