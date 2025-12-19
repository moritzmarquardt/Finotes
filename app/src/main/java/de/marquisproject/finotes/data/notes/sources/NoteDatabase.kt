package de.marquisproject.finotes.data.notes.sources

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Database
import androidx.room.OnConflictStrategy
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus

import java.io.File

@Database(
    entities = [Note::class],
    version = 2
)
abstract class NoteDatabase : RoomDatabase() {
    abstract val dao: NoteDAO
}


/**
 * this will only be included once and then immediately deprecated
 */
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

            // It's safer to copy data manually instead of attaching databases,
            // to avoid issues with WAL (Write-Ahead Logging).
            copyDataFromOldDb(context.getDatabasePath("note.db").absolutePath, NoteStatus.ACTIVE, db)
            copyDataFromOldDb(context.getDatabasePath("bin.db").absolutePath, NoteStatus.BINNED, db)
            copyDataFromOldDb(context.getDatabasePath("archive.db").absolutePath, NoteStatus.ARCHIVED, db)
        }
    }

    private fun copyDataFromOldDb(dbPath: String, status: NoteStatus, newDb: SupportSQLiteDatabase) {
        val dbFile = File(dbPath)
        if (!dbFile.exists()) {
            Log.w("ExternalDbMigrations", "Old database file does not exist, skipping: $dbPath")
            return
        }

        val oldDb = try {
            SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
        } catch (e: Exception) {
            Log.e("ExternalDbMigrations", "Error opening old database: $dbPath", e)
            return
        }

        val cursor = oldDb.rawQuery("SELECT title, body, dateCreated FROM notes_table", null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                val titleIndex = c.getColumnIndex("title")
                val bodyIndex = c.getColumnIndex("body")
                val dateCreatedIndex = c.getColumnIndex("dateCreated")

                if (titleIndex == -1 || bodyIndex == -1 || dateCreatedIndex == -1) {
                    Log.e("ExternalDbMigrations", "Column not found in old db: $dbPath")
                    return@use
                }

                newDb.beginTransaction()
                try {
                    do {
                        val contentValues = ContentValues().apply {
                            put("title", c.getString(titleIndex))
                            put("content", c.getString(bodyIndex)) // 'body' in old schema, 'content' in new
                            put("status", status.name)
                            put("createdAt", c.getLong(dateCreatedIndex))
                            put("updatedAt", c.getLong(dateCreatedIndex)) // Set updatedAt to createdAt
                        }
                        newDb.insert("notes", OnConflictStrategy.REPLACE, contentValues)
                    } while (c.moveToNext())
                    newDb.setTransactionSuccessful()
                } finally {
                    newDb.endTransaction()
                }
            }
        }
        oldDb.close()
        Log.d("ExternalDbMigrations", "Successfully migrated data from $dbPath")
    }
}