package de.marquisproject.finotes.data.notes.sources

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

    companion object {
        val MIGRATION_1_2: Migration
            get() {
                return object : Migration(1, 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Add new columns
                        db.execSQL("ALTER TABLE notes_table ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                        db.execSQL("ALTER TABLE notes_table ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                        db.execSQL("ALTER TABLE notes_table ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0") // SQLite uses 0 for false, 1 for true
                        db.execSQL("ALTER TABLE notes_table ADD COLUMN remoteId INTEGER NOT NULL DEFAULT -1")

                        // Create index for noteStatus
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_table_noteStatus` ON `notes_table` (`noteStatus`)")

                        // 1. Create the new table with the correct schema
                        db.execSQL("""
                            CREATE TABLE notes_table_new (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                title TEXT NOT NULL,
                                body TEXT NOT NULL,
                                dateCreated INTEGER NOT NULL,
                                isPinned INTEGER NOT NULL,
                                noteStatus TEXT NOT NULL DEFAULT 'ACTIVE',
                                category TEXT NOT NULL DEFAULT '',
                                lastModified INTEGER NOT NULL DEFAULT 0,
                                needsSync INTEGER NOT NULL DEFAULT 0,
                                remoteId INTEGER NOT NULL DEFAULT -1
                            )
                        """.trimIndent())

                        // 2. Copy the data from the old table to the new table
                        // Note: We are ignoring the old 'color' column and providing default values for new columns.
                        db.execSQL("""
                            INSERT INTO notes_table_new (id, title, body, dateCreated, isPinned)
                            SELECT id, title, body, dateCreated, isPinned FROM notes_table
                        """.trimIndent())

                        // 3. Drop the old table
                        db.execSQL("DROP TABLE notes_table")

                        // 4. Rename the new table to the original name
                        db.execSQL("ALTER TABLE notes_table_new RENAME TO notes_table")

                        // 5. Create the index on the new table
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_table_noteStatus` ON `notes_table` (`noteStatus`)")

                    }
                }
            }
    }
}