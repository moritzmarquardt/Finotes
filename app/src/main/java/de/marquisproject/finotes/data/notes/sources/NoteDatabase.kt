package de.marquisproject.finotes.data.notes.sources

import android.content.Context
import android.util.Log
import de.marquisproject.finotes.data.notes.model.Note
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Note::class],
    version = 2
)
abstract class NoteDatabase : RoomDatabase() {
    abstract val dao : NoteDAO
}


class ExternalDbMigrations(private val context: Context) {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // create new table if not exists
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `notes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
            """.trimIndent())

            // attach old DB files (adjust names to your actual filenames)
            val activePath = context.getDatabasePath("note.db").absolutePath
            val binnedPath = context.getDatabasePath("bin.db").absolutePath
            val archivedPath = context.getDatabasePath("archive.db").absolutePath
            // Log the paths
            Log.d("ExternalDbMigrations", "Active Path: $activePath")
            Log.d("ExternalDbMigrations", "Binned Path: $binnedPath")
            Log.d("ExternalDbMigrations", "Archived Path: $archivedPath")

            try {
                db.execSQL("ATTACH DATABASE '$activePath' AS old_active")
                db.execSQL("ATTACH DATABASE '$binnedPath' AS old_binned")
                db.execSQL("ATTACH DATABASE '$archivedPath' AS old_archived")
            } catch (e: Exception) {
                Log.e("ExternalDbMigrations", "Error attaching old databases", e)
            }

            //LOGIC FOR MIGRATION TODO()

            db.execSQL("DETACH DATABASE old_active")
            db.execSQL("DETACH DATABASE old_binned")
            db.execSQL("DETACH DATABASE old_archived")
        }
    }
}