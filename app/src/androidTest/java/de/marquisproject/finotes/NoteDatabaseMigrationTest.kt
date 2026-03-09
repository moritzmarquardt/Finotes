package de.marquisproject.finotes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.sources.NoteDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDatabaseMigrationTest {

    private val dbName = "migration-test-note.db"
    private lateinit var roomDb: NoteDatabase
    private lateinit var context: Context

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NoteDatabase::class.java
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Clean up any leftover databases from previous runs
        context.deleteDatabase(dbName)
        context.deleteDatabase("bin.db")
        context.deleteDatabase("archive.db")
    }

    @After
    fun tearDown() {
        if (::roomDb.isInitialized) roomDb.close()
        context.deleteDatabase(dbName)
        context.deleteDatabase("bin.db")
        context.deleteDatabase("archive.db")
    }

    /**
     * Standard migration test: Checks schema and data preservation for the main table.
     */
    @Test
    fun migrate_1_2_preservesDataAndSchema() {
        helper.createDatabase(dbName, 1).use { db ->
            val values = ContentValues().apply {
                put("title", "Old Title")
                put("body", "Old Body")
                put("dateCreated", 123456789L)
                put("isPinned", 0)
                put("noteStatus", NoteStatus.ACTIVE.name)
                put("color", 0)
            }
            db.insert("notes_table", SQLiteDatabase.CONFLICT_REPLACE, values)
        }

        helper.runMigrationsAndValidate(
            name = dbName,
            version = 2,
            validateDroppedTables = true,
            NoteDatabase.getMigration1to2(context)
        )

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName)
            .allowMainThreadQueries()
            .build()
        val dao = roomDb.dao
        val notes = runBlocking { dao.getAllNotesByStatus(NoteStatus.ACTIVE).first() }

        assertEquals(1, notes.size)
        val note = notes[0]
        assertEquals("Old Title", note.title)
        assertEquals("Old Body", note.body)
        assertFalse(note.isPinned)
        assertEquals(NoteStatus.ACTIVE, note.noteStatus)
        assertEquals(123456789L, note.dateCreated)
        assertEquals(note.dateCreated, note.lastModified)
        assertTrue("New notes from old main DB should default to needsSync = true", note.needsSync)
        assertNull(note.remoteId)
    }

    /**
     * Edge Case: External databases (bin.db, archive.db) do not exist.
     * The migration should still succeed.
     */
    @Test
    fun migrate_1_2_handles_missing_external_dbs() {
        helper.createDatabase(dbName, 1).use { db ->
            db.execSQL("INSERT INTO notes_table (title, body, dateCreated, isPinned, noteStatus, color) VALUES ('Main', 'Content', 1, 0, 'ACTIVE', 0)")
        }

        // Run migration without creating bin.db or archive.db
        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val notes = runBlocking { roomDb.dao.getAllNotes().first() }
        assertEquals(1, notes.size)
    }

    /**
     * Edge Case: External databases exist but are empty.
     */
    @Test
    fun migrate_1_2_handles_empty_external_dbs() {
        createExternalDb("bin.db", emptyList())
        createExternalDb("archive.db", emptyList())

        helper.createDatabase(dbName, 1).use { db ->
            db.execSQL("INSERT INTO notes_table (title, body, dateCreated, isPinned, noteStatus, color) VALUES ('Main', 'Content', 1, 0, 'ACTIVE', 0)")
        }

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val notes = runBlocking { roomDb.dao.getAllNotes().first() }
        assertEquals(1, notes.size)
    }

    /**
     * Edge Case: Legacy/corrupt main table allows NULL values in columns now NOT NULL.
     * Verifies COALESCE fallbacks in migration SQL.
     */
    @Test
    fun migrate_1_2_handles_null_values_in_main_db() {
        helper.createDatabase(dbName, 1).use { db ->
            // Recreate table with nullable columns to simulate legacy/corrupt data that still reaches migration.
            db.execSQL("ALTER TABLE notes_table RENAME TO notes_table_backup")
            db.execSQL(
                """
                CREATE TABLE notes_table (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT,
                    body TEXT,
                    dateCreated INTEGER,
                    isPinned INTEGER NOT NULL,
                    noteStatus TEXT NOT NULL,
                    color INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("INSERT INTO notes_table (title, body, dateCreated, isPinned, noteStatus, color) VALUES (NULL, NULL, NULL, 0, 'ACTIVE', 0)")
            db.execSQL("DROP TABLE notes_table_backup")
        }

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val note = runBlocking { roomDb.dao.getAllNotes().first().first() }
        assertEquals("Title should be empty string if null", "", note.title)
        assertEquals("Body should be empty string if null", "", note.body)
        assertTrue("dateCreated should be backfilled when null", note.dateCreated > 0L)
        assertEquals("lastModified should mirror backfilled dateCreated", note.dateCreated, note.lastModified)
    }

    /**
     * Fuzzy Case: Special characters, emojis, and very long strings.
     */
    @Test
    fun migrate_1_2_handles_fuzzy_data() {
        val longTitle = "A".repeat(2000)
        val emojiBody = "Note with emojis 🚀 🔥 🛠️ and \n new lines \t tabs."
        val sqlInjectionAttempt = "'); DROP TABLE notes_table; --"

        helper.createDatabase(dbName, 1).use { db ->
            val values = ContentValues().apply {
                put("title", longTitle)
                put("body", emojiBody)
                put("dateCreated", 100L)
                put("isPinned", 1)
                put("noteStatus", "ACTIVE")
                put("color", 1)
            }
            db.insert("notes_table", SQLiteDatabase.CONFLICT_REPLACE, values)

            val values2 = ContentValues().apply {
                put("title", "SQL Test")
                put("body", sqlInjectionAttempt)
                put("dateCreated", 200L)
                put("isPinned", 0)
                put("noteStatus", "ACTIVE")
                put("color", 2)
            }
            db.insert("notes_table", SQLiteDatabase.CONFLICT_REPLACE, values2)
        }

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val notes = runBlocking { roomDb.dao.getAllNotes().first().sortedBy { it.dateCreated } }

        assertEquals(longTitle, notes[0].title)
        assertEquals(emojiBody, notes[0].body)
        assertEquals(sqlInjectionAttempt, notes[1].body)
    }

    /**
     * Complex Case: Merging data from all three sources.
     */
    @Test
    fun migrate_1_2_merges_all_sources() {
        createExternalDb("bin.db", listOf(
            NoteData("Bin Note 1", "Body 1", 10L, 0),
            NoteData("Bin Note 2", "Body 2", 11L, 1)
        ))
        createExternalDb("archive.db", listOf(
            NoteData("Archive Note", "Archived", 20L, 0)
        ))

        helper.createDatabase(dbName, 1).use { db ->
            db.execSQL("INSERT INTO notes_table (title, body, dateCreated, isPinned, noteStatus, color) VALUES ('Active Note', 'Active', 30, 1, 'ACTIVE', 0)")
        }

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val dao = roomDb.dao

        runBlocking {
            val allNotes = dao.getAllNotes().first()
            assertEquals("Total notes should be 4 (1 main + 2 bin + 1 archive)", 4, allNotes.size)

            val active = dao.getAllNotesByStatus(NoteStatus.ACTIVE).first()
            assertEquals(1, active.size)
            assertEquals("Active Note", active[0].title)

            val binned = dao.getAllNotesByStatus(NoteStatus.BINNED).first()
            assertEquals(2, binned.size)
            assertTrue(binned.any { it.title == "Bin Note 1" })

            val archived = dao.getAllNotesByStatus(NoteStatus.ARCHIVED).first()
            assertEquals(1, archived.size)
            assertEquals("Archive Note", archived[0].title)
        }
    }

    /**
     * Verifies that timestamps are correctly mapped during migration.
     */
    @Test
    fun migrate_1_2_verifies_timestamps() {
        val created = 123456789L
        helper.createDatabase(dbName, 1).use { db ->
            val values = ContentValues().apply {
                put("title", "Time Test")
                put("body", "...")
                put("dateCreated", created)
                put("isPinned", 0)
                put("noteStatus", "ACTIVE")
                put("color", 0)
            }
            db.insert("notes_table", SQLiteDatabase.CONFLICT_REPLACE, values)
        }

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val note = runBlocking { roomDb.dao.getAllNotes().first().first() }

        assertEquals(created, note.dateCreated)
        assertEquals("lastModified should be initialized to dateCreated during migration", created, note.lastModified)
    }

    /**
     * Verifies imported external rows get the expected defaults for v2-only fields.
     */
    @Test
    fun migrate_1_2_external_rows_have_expected_defaults() {
        createExternalDb("bin.db", listOf(NoteData("Bin Defaults", "B", 10L, 1)))
        createExternalDb("archive.db", listOf(NoteData("Archive Defaults", "A", 20L, 0)))

        helper.createDatabase(dbName, 1).use { db ->
            db.execSQL("INSERT INTO notes_table (title, body, dateCreated, isPinned, noteStatus, color) VALUES ('Main', 'Content', 30, 0, 'ACTIVE', 0)")
        }

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val dao = roomDb.dao

        runBlocking {
            val binned = dao.getAllNotesByStatus(NoteStatus.BINNED).first().single()
            assertEquals("Bin Defaults", binned.title)
            assertEquals(0L, binned.category)
            assertFalse("Imported external rows should default to needsSync = false", binned.needsSync)
            assertNull(binned.remoteId)
            assertEquals(binned.dateCreated, binned.lastModified)

            val archived = dao.getAllNotesByStatus(NoteStatus.ARCHIVED).first().single()
            assertEquals("Archive Defaults", archived.title)
            assertEquals(0L, archived.category)
            assertFalse(archived.needsSync)
            assertNull(archived.remoteId)
            assertEquals(archived.dateCreated, archived.lastModified)
        }
    }

    /**
     * Verifies migration remains successful when an external DB is malformed.
     * Current behavior is best-effort import: malformed source is skipped.
     */
    @Test
    fun migrate_1_2_skips_malformed_external_db_and_keeps_main_data() {
        createMalformedExternalDb("bin.db")
        createExternalDb("archive.db", listOf(NoteData("Archive Note", "Archived", 20L, 0)))

        helper.createDatabase(dbName, 1).use { db ->
            db.execSQL("INSERT INTO notes_table (title, body, dateCreated, isPinned, noteStatus, color) VALUES ('Main', 'Content', 1, 0, 'ACTIVE', 0)")
        }

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val dao = roomDb.dao

        runBlocking {
            val active = dao.getAllNotesByStatus(NoteStatus.ACTIVE).first()
            val binned = dao.getAllNotesByStatus(NoteStatus.BINNED).first()
            val archived = dao.getAllNotesByStatus(NoteStatus.ARCHIVED).first()

            assertEquals(1, active.size)
            assertEquals("Main", active.first().title)
            assertTrue("Malformed bin.db should not contribute imported rows", binned.isEmpty())
            assertEquals(1, archived.size)
            assertEquals("Archive Note", archived.first().title)
        }
    }

    // Helper methods for creating external databases
    private data class NoteData(val title: String, val body: String, val dateCreated: Long, val isPinned: Int)

    private fun createExternalDb(fileName: String, notes: List<NoteData>) {
        val path = context.getDatabasePath(fileName).absolutePath
        SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY).use { db ->
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notes_table (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    body TEXT NOT NULL,
                    dateCreated INTEGER NOT NULL,
                    isPinned INTEGER NOT NULL,
                    noteStatus TEXT NOT NULL,
                    color INTEGER NOT NULL
                )
                """.trimIndent()
            )
            notes.forEach { note ->
                val values = ContentValues().apply {
                    put("title", note.title)
                    put("body", note.body)
                    put("dateCreated", note.dateCreated)
                    put("isPinned", note.isPinned)
                    put("noteStatus", "ACTIVE") // Status in old DBs was usually ACTIVE or implied
                    put("color", 0)
                }
                db.insert("notes_table", null, values)
            }
        }
    }

    private fun createMalformedExternalDb(fileName: String) {
        val path = context.getDatabasePath(fileName).absolutePath
        SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY).use { db ->
            // Intentionally wrong shape: migration expects a notes_table with specific columns.
            db.execSQL("CREATE TABLE IF NOT EXISTS malformed_table (id INTEGER PRIMARY KEY AUTOINCREMENT, payload TEXT)")
            db.execSQL("INSERT INTO malformed_table (payload) VALUES ('bad schema')")
        }
    }
}
