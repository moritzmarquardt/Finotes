package de.marquisproject.finotes.data.notes.sources

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import de.marquisproject.finotes.data.notes.model.Category
import de.marquisproject.finotes.data.notes.model.CategoryUsage
import de.marquisproject.finotes.data.notes.model.Note

@Database(
    entities = [Note::class, Category::class, CategoryUsage::class],
    version = 3,
    exportSchema = true
)
abstract class NoteDatabase : RoomDatabase() {
    abstract val dao: NoteDAO
    abstract val categoryDao: CategoryDAO

    companion object {
        fun getMigration1to2(context: Context): Migration = NoteMigration1to2(context)
        fun getMigration2to3(): Migration = NoteMigration2to3()
    }
}
