package de.marquisproject.finotes.data.notes.sources

import de.marquisproject.finotes.data.notes.model.Note
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Note::class],
    version = 1
)
abstract class ArchiveDatabase : RoomDatabase() {
    abstract val dao : ArchiveDAO
}