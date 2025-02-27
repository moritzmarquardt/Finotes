package de.marquisproject.fionotes.data.notes.sources

import android.content.Context
import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.data.notes.sources.NoteDAO

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], exportSchema = true, version = 1)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDAO(): NoteDAO

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(
            context: Context
        ): NoteDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            } else {
                synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        NoteDatabase::class.java,
                        "notes.db"
                    ).build()
                    INSTANCE = instance
                    return instance
                }
            }

        }
    }
}