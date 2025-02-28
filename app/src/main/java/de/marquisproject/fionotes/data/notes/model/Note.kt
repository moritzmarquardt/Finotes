package de.marquisproject.fionotes.data.notes.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "content") val content: String = "",
    @ColumnInfo(name = "lastEdited") val lastEdited: String?,
    @ColumnInfo(name = "isPinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "color") val color: Int = 0,
)