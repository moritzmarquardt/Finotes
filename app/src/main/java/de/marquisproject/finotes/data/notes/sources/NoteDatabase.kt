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
    version = 2,
    exportSchema = true
)
abstract class NoteDatabase : RoomDatabase() {
    abstract val dao: NoteDAO

    companion object {
        fun MIGRATION_1_2(context: Context) = object : Migration(1, 2) {
            // Kotlin
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d("NoteMigration", "Creating temporary table `notes_table_new` with Room schema")
                db.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS `notes_table_new` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                            `title` TEXT NOT NULL,
                            `body` TEXT NOT NULL,
                            `category` INTEGER NOT NULL,
                            `isPinned` INTEGER NOT NULL,
                            `dateCreated` INTEGER NOT NULL,
                            `lastModified` INTEGER NOT NULL,
                            `noteStatus` TEXT NOT NULL,
                            `needsSync` INTEGER NOT NULL,
                            `remoteId` INTEGER
                        )
                        """.trimIndent()
                ) // this table is now consistent with the Note Scheme Version 2

                Log.d("NoteMigration", "Copying existing app data from `notes_table` if present")
                db.execSQL(
                    """
                        INSERT INTO `notes_table_new`(
                            `id`, `title`, `body`, `category`, `isPinned`,
                            `dateCreated`, `lastModified`, `noteStatus`, `needsSync`, `remoteId`
                        )
                        SELECT
                            `id`,
                            COALESCE(`title`, ''),
                            COALESCE(`body`, ''),
                            0,
                            COALESCE(`isPinned`, 0),
                            COALESCE(`dateCreated`, strftime('%s','now')*1000),
                            COALESCE(`dateCreated`, strftime('%s','now')*1000),
                            COALESCE(`noteStatus`, 'ACTIVE'),
                            1,
                            NULL
                        FROM `notes_table`
                        """.trimIndent()
                ) // Insert the data from the old table into the new table with the new schema

                Log.d("NoteMigration", "Replacing old `notes_table` with new table with new schema")
                db.execSQL("DROP TABLE IF EXISTS `notes_table`")
                db.execSQL("ALTER TABLE `notes_table_new` RENAME TO `notes_table`")

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_notes_table_noteStatus` ON `notes_table` (`noteStatus`)"
                ) // create the expected index

                Log.d("NoteMigration", "Importing external databases into `notes_table`")
                // Migrate bin.db
                val binPath = context.getDatabasePath("bin.db").absolutePath
                if (ExternalDbMigrations.copyDataFromOldDb(binPath, NoteStatus.BINNED, db)) {
                    context.deleteDatabase("bin.db")
                    Log.d("NoteMigration", "Successfully migrated and deleted bin.db")
                }

                // Migrate archive.db
                val archivePath = context.getDatabasePath("archive.db").absolutePath
                if (ExternalDbMigrations.copyDataFromOldDb(archivePath, NoteStatus.ARCHIVED, db)) {
                    context.deleteDatabase("archive.db")
                    Log.d("NoteMigration", "Successfully migrated and deleted archive.db")
                }

                Log.d("NoteMigration", "Migration complete")
            }
        }
    }
}


/**
 * this will only be included once and then immediately deprecated
 */
object ExternalDbMigrations {
    fun copyDataFromOldDb(dbPath: String, status: NoteStatus, newDb: SupportSQLiteDatabase) {
    fun copyDataFromOldDb(dbPath: String, status: NoteStatus, newDb: SupportSQLiteDatabase): Boolean {
        val dbFile = File(dbPath)
        if (!dbFile.exists()) {
            Log.w("NoteMigration", "Old database file does not exist, skipping: $dbPath")
            return true
        }

        var oldDb: SQLiteDatabase? = null
        try {
            oldDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
            Log.d("NoteMigration", "Successfully opened old database: $dbPath")

            val cursor = oldDb.rawQuery("SELECT title, body, isPinned, dateCreated FROM notes_table", null)
            cursor.use { c ->
                if (c.moveToFirst()) {
                    val titleIndex = c.getColumnIndex("title")
                    val bodyIndex = c.getColumnIndex("body")
                    val dateCreatedIndex = c.getColumnIndex("dateCreated")
                    val isPinnedIndex = c.getColumnIndex("isPinned")

                    if (titleIndex == -1 || bodyIndex == -1 || dateCreatedIndex == -1 || isPinnedIndex == -1) {
                        Log.e("NoteMigration", "Column not found in old db: $dbPath")
                        return false
                    }

                    do {
                        val createdAt = c.getLong(dateCreatedIndex)
                        val values = ContentValues().apply {
                            put("title", c.getString(titleIndex))
                            put("body", c.getString(bodyIndex))
                            put("category", 0)
                            put("isPinned", c.getInt(isPinnedIndex))
                            put("dateCreated", createdAt)
                            put("lastModified", createdAt)
                            put("noteStatus", status.name)
                            put("needsSync", 0)
                            putNull("remoteId")
                        }
                        newDb.insert("notes_table", OnConflictStrategy.REPLACE, values)
                    } while (c.moveToNext())
                }
            }
            Log.d("NoteMigration", "Successfully migrated data from $dbPath")
            return true
        } catch (e: Exception) {
            Log.e("NoteMigration", "Error opening old database: $dbPath", e)
            return false
        } finally {
            oldDb?.close()
        }
    }
}
