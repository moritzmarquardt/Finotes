package de.marquisproject.finotes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
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

    // Old version 1 Note class all nullable to allow for testing of corrupt data
    data class NoteV1(
        val id: Long? = -1L,
        val title: String? = "",
        val body: String? = "",
        val dateCreated: Long? = System.currentTimeMillis(),
        val isPinned: Boolean? = false,
        val noteStatus: NoteStatus? = NoteStatus.ACTIVE,
        val color: Int? = 0,
    )

    fun insertNoteV1(note: NoteV1, db: SupportSQLiteDatabase, tableName: String): Long {
        val noteValue = ContentValues().apply {
            put("title", note.title)
            put("body", note.body)
            put("dateCreated", note.dateCreated)
            put("isPinned", note.isPinned)
            put("noteStatus", note.noteStatus!!.name)
            put("color", note.color)
        }
        return db.insert(tableName, SQLiteDatabase.CONFLICT_REPLACE, noteValue)
    }

    val standardNoteV1 : NoteV1 = NoteV1(title = "Active Note", body = "Main", dateCreated = 1, isPinned = false, noteStatus = NoteStatus.ACTIVE)

    /**
     * Check that data is migrated correctly from v1 to v2.
     */
    @Test
    fun migrate_1_2_preservesDataAndSchema() {
        val dbV1 = helper.createDatabase(dbName, 1)

        // Create the main database at version 1 filled with test data
        val testNotesV1 = listOf(
            NoteV1(title = "Note 1", body = "Body 1", dateCreated = 123456789L, isPinned = false, noteStatus = NoteStatus.ACTIVE),
            NoteV1(title = "Note 2", body = "Body 2", dateCreated = 0, isPinned = true, noteStatus = NoteStatus.ACTIVE),
            NoteV1(title = "Note 3", body = "Body 3", dateCreated = 0L, isPinned = true, noteStatus = NoteStatus.ACTIVE),
            NoteV1(id = -1L, title = "Note 4", body = "Body 4", dateCreated = 283L, isPinned = true, noteStatus = NoteStatus.ACTIVE),
            NoteV1(id = 0, title = "Note 5", body = "Body 5", dateCreated = 283L, isPinned = true, noteStatus = NoteStatus.ACTIVE),
            NoteV1(id = -20293L, title = "Note 5", body = "Body 5", dateCreated = 283L, isPinned = true, noteStatus = NoteStatus.ACTIVE),
            NoteV1(title = "fuzzy note with 🎼 and \n new lines \t tabs", body = "note with emojis 🎼️ and \n new lines \t tabs.", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.ACTIVE),
            NoteV1(title = "SQL Injection Attempt", body = "'); DROP TABLE notes_table; --", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.ACTIVE),
            NoteV1(title = "missing body", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.ACTIVE),
            NoteV1(title = "missing dateCreated", body = "missing dateCreated", isPinned = false, noteStatus = NoteStatus.ACTIVE),
            NoteV1(body = "missing title", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.ACTIVE),
            NoteV1(title = "missing status", body = "missing status", dateCreated = 0, isPinned = false),
            NoteV1(title = "missing color", body = "missing color", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.ACTIVE),
            NoteV1(),
            NoteV1(title = "Archived note 1", body = "Body 1", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.ARCHIVED),
            NoteV1(title = "Archived note 2", body = "Body 2", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.ARCHIVED),
            NoteV1(title = "Archived note 3", body = "Body 3", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.ARCHIVED),
            NoteV1(title = "Binned note 1", body = "Body 1", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.BINNED),
            NoteV1(title = "Binned note 2", body = "Body 2", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.BINNED),
            NoteV1(title = "Binned note 3", body = "Body 3", dateCreated = 0, isPinned = false, noteStatus = NoteStatus.BINNED),
        )
        testNotesV1.forEach {
            insertNoteV1(it, dbV1, "notes_table")
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
        val notes = runBlocking { dao.getAllNotes().first() }

        assertEquals("check number of transferred notes",testNotesV1.size, notes.size)
        assertEquals("check number of active notes in db", testNotesV1.filter { it.noteStatus == NoteStatus.ACTIVE }.size, notes.filter { it.noteStatus == NoteStatus.ACTIVE }.size)
        assertEquals("check number of archived notes in db", testNotesV1.filter { it.noteStatus == NoteStatus.ARCHIVED }.size, notes.filter { it.noteStatus == NoteStatus.ARCHIVED }.size)
        assertEquals("check number of binned notes in db", testNotesV1.filter { it.noteStatus == NoteStatus.BINNED }.size, notes.filter { it.noteStatus == NoteStatus.BINNED }.size)

        testNotesV1.forEach { note ->
            val dbNote = notes.find { it.title == note.title && it.body == note.body }
            assertNotNull(dbNote)
            assertEquals(note.title, dbNote!!.title)
            assertEquals(note.body, dbNote.body)
            assertEquals(note.isPinned, dbNote.isPinned)
            assertEquals(note.noteStatus, dbNote.noteStatus)
            assertEquals(note.dateCreated, dbNote.dateCreated)
            assertEquals(note.color, dbNote.category.toInt())
            assertNull(dbNote.remoteId)
            assertTrue(dbNote.needsSync)
            assertEquals(dbNote.lastModified, dbNote.dateCreated)
        }
    }

    /**
     * Edge Case: External databases (bin.db, archive.db) do not exist.
     * The migration should still succeed.
     */
    @Test
    fun migrate_1_2_handles_missing_external_dbs() {
        val dbV1 = helper.createDatabase(dbName, 1)
        insertNoteV1(standardNoteV1, dbV1, "notes_table")

        // Run migration without creating bin.db or archive.db
        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val notes = runBlocking { roomDb.dao.getAllNotes().first() }
        val dbNote = notes[0]
        assertNotNull(dbNote)
        assertEquals(standardNoteV1.title, dbNote.title)
        assertEquals(standardNoteV1.body, dbNote.body)
        assertEquals(standardNoteV1.isPinned, dbNote.isPinned)
        assertEquals(standardNoteV1.noteStatus, dbNote.noteStatus)
        assertEquals(standardNoteV1.dateCreated, dbNote.dateCreated)
        assertEquals(standardNoteV1.color, dbNote.category.toInt())
        assertNull(dbNote.remoteId)
        assertTrue(dbNote.needsSync)
        assertEquals(dbNote.lastModified, dbNote.dateCreated)
    }

    /**
     * Edge Case: External databases exist but are empty.
     */
    @Test
    fun migrate_1_2_handles_empty_external_dbs() {
        val dbV1 = helper.createDatabase(dbName, 1)
        insertNoteV1(standardNoteV1, dbV1, "notes_table")

        createExternalDb("bin.db", emptyList())
        createExternalDb("archive.db", emptyList())

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val notes = runBlocking { roomDb.dao.getAllNotes().first() }
        val dbNote = notes[0]
        assertNotNull(dbNote)
        assertEquals(standardNoteV1.title, dbNote.title)
        assertEquals(standardNoteV1.body, dbNote.body)
        assertEquals(standardNoteV1.isPinned, dbNote.isPinned)
        assertEquals(standardNoteV1.noteStatus, dbNote.noteStatus)
        assertEquals(standardNoteV1.dateCreated, dbNote.dateCreated)
        assertEquals(standardNoteV1.color, dbNote.category.toInt())
        assertNull(dbNote.remoteId)
        assertTrue(dbNote.needsSync)
        assertEquals(dbNote.lastModified, dbNote.dateCreated)
    }

    /**
     * Edge Case: Legacy/corrupt main table allows NULL values in columns now NOT NULL.
     * Verifies COALESCE fallbacks in migration SQL.
     */
    @Test
    fun migrate_1_2_handles_null_values_in_main_db() {
        val dbV1 = helper.createDatabase(dbName, 1)
        insertNoteV1(standardNoteV1, dbV1, "notes_table")

        // Recreate table with nullable columns to simulate legacy/corrupt data that still reaches migration.
        dbV1.execSQL("ALTER TABLE notes_table RENAME TO notes_table_backup")
        dbV1.execSQL(
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
        dbV1.execSQL("INSERT INTO notes_table (title, body, dateCreated, isPinned, noteStatus, color) VALUES (NULL, NULL, NULL, 0, 'ACTIVE', 0)")
        dbV1.execSQL("DROP TABLE notes_table_backup")

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val note = runBlocking { roomDb.dao.getAllNotes().first().first() }
        assertEquals("Title should be empty string if null", "", note.title)
        assertEquals("Body should be empty string if null", "", note.body)
        assertTrue("dateCreated should be backfilled when null", note.dateCreated > 0L)
        assertEquals("lastModified should mirror backfilled dateCreated", note.dateCreated, note.lastModified)
    }

    /**
     * Complex Case: Merging data from all three sources.
     */
    @Test
    fun migrate_1_2_merges_all_sources() {
        val dbV1 = helper.createDatabase(dbName, 1)
        insertNoteV1(standardNoteV1, dbV1, "notes_table")

        createExternalDb("bin.db", listOf(standardNoteV1.copy(title = "Bin Note", noteStatus = NoteStatus.BINNED)))
        createExternalDb("archive.db", listOf(standardNoteV1.copy(title = "Archive Note", noteStatus = NoteStatus.ARCHIVED)))

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val dao = roomDb.dao

        runBlocking {
            val allNotes = dao.getAllNotes().first()
            val active = dao.getAllNotesByStatus(NoteStatus.ACTIVE).first()
            val binned = dao.getAllNotesByStatus(NoteStatus.BINNED).first()
            val archived = dao.getAllNotesByStatus(NoteStatus.ARCHIVED).first()

            assertEquals(3, allNotes.size)
            assertEquals(1, active.size)
            assertEquals(1, binned.size)
            assertEquals(1, archived.size)
            assertEquals("Active Note", active.first().title)
            assertEquals("Bin Note", binned.first().title)
            assertEquals("Archive Note", archived.first().title)
            assertEquals(NoteStatus.ACTIVE, active.first().noteStatus)
            assertEquals(NoteStatus.BINNED, binned.first().noteStatus)
            assertEquals(NoteStatus.ARCHIVED, archived.first().noteStatus)
        }
    }

    /**
     * Verifies migration remains successful when an external DB is malformed.
     */
    @Test
    fun migrate_1_2_skips_malformed_external_db_and_keeps_main_data() {
        val dbV1 = helper.createDatabase(dbName, 1)
        insertNoteV1(standardNoteV1, dbV1, "notes_table")

        createMalformedExternalDb("bin.db")
        createExternalDb("archive.db", listOf(standardNoteV1.copy(title = "Archive Note", noteStatus = NoteStatus.ARCHIVED)))

        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName).build()
        val dao = roomDb.dao

        runBlocking {
            val active = dao.getAllNotesByStatus(NoteStatus.ACTIVE).first()
            val binned = dao.getAllNotesByStatus(NoteStatus.BINNED).first()
            val archived = dao.getAllNotesByStatus(NoteStatus.ARCHIVED).first()

            assertEquals(1, active.size)
            assertEquals(standardNoteV1.title, active.first().title)
            assertTrue("Malformed bin.db should not contribute imported rows", binned.isEmpty())
            assertEquals(1, archived.size)
            assertEquals( "Archive Note", archived.first().title)
        }
    }

    @Test
    fun migrate_1_2_verifies_successful_import_and_file_cleanup() {
        val dbV1 = helper.createDatabase(dbName, 1)
        insertNoteV1(standardNoteV1, dbV1, "notes_table")

        // 1. GIVEN: Create the old external databases
        createExternalDb("bin.db", listOf(standardNoteV1.copy(title = "Bin Note", noteStatus = NoteStatus.BINNED)))
        createExternalDb("archive.db", listOf(standardNoteV1.copy(title = "Archive Note", noteStatus = NoteStatus.ARCHIVED)))

        // 3. WHEN: Run migration
        helper.runMigrationsAndValidate(dbName, 2, true, NoteDatabase.getMigration1to2(context))

        // 4. THEN: Verify data using Room
        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Match the cleanup logic from your DatabaseModule.kt
                    context.deleteDatabase("bin.db")
                    context.deleteDatabase("archive.db")
                }
            })
            .build()
        val dao = roomDb.dao

        runBlocking {
            val allNotes = dao.getAllNotes().first()
            assertEquals(3, allNotes.size)

            val binned = allNotes.find { it.title == "Bin Note" }
            assertEquals(NoteStatus.BINNED, binned?.noteStatus)
            assertEquals("Bin Note", binned?.title)

            val archived = allNotes.find { it.title == "Archive Note" }
            assertEquals(NoteStatus.ARCHIVED, archived?.noteStatus)
            assertEquals("Archive Note", archived?.title)
        }

        // 5. THEN: Verify files were cleaned up (if your logic handles this)
        // Note: If cleanup happens in a Callback, you might need to trigger a DB operation
        // to ensure the callback has run.
        assertFalse("bin.db should be deleted after migration", context.getDatabasePath("bin.db").exists())
        assertFalse("archive.db should be deleted after migration", context.getDatabasePath("archive.db").exists())
    }

    private fun createExternalDb(fileName: String, notes: List<NoteV1>) {
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
