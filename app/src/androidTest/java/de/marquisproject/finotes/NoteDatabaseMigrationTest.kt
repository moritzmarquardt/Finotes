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
import de.marquisproject.finotes.data.notes.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.collections.get
import kotlin.or
import kotlin.text.insert

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
        context.deleteDatabase(dbName)
    }

    @Test
    fun migrate_1_to_2_preservesDataAndSchema() {
        helper.createDatabase(dbName, 1).use { db ->
            val values = ContentValues().apply {
                put("title", "Old Title")
                put("body", "Old Body")
                put("dateCreated", System.currentTimeMillis())
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
            NoteDatabase.MIGRATION_1_2(context)
        )

        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName)
            .allowMainThreadQueries()
            .build()
        val dao = roomDb.dao
        val note: Note = runBlocking {
            dao.getAllNotesByStatus(NoteStatus.ACTIVE).first().single()
        }

        assertNotNull(note)
        assertEquals(1, runBlocking { dao.getAllNotesByStatus(NoteStatus.ACTIVE).first().size })
        assertEquals("Old Title", note.title)
        assertEquals("Old Body", note.body)
        assertFalse("'isPinned' should be false after migration.", note.isPinned)
        assertEquals(NoteStatus.ACTIVE, note.noteStatus)
        assertEquals(0L, note.category)
        assertEquals(note.dateCreated, note.lastModified)
        assertTrue("New 'needsSync' column should default to true.", note.needsSync)
        assertNull(note.remoteId)

        roomDb.close()
    }

    @Test
    fun migrate_1_2_imports_dbs_correctly() {
        val binDbPath = context.getDatabasePath("bin.db").absolutePath
        val archiveDbPath = context.getDatabasePath("archive.db").absolutePath

        // Create old bin database with binned notes
        SQLiteDatabase.openDatabase(binDbPath, null, SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY).use { binDb ->
            binDb.execSQL(
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
            val binValues = ContentValues().apply {
                put("title", "Binned Note")
                put("body", "This note is in bin")
                put("dateCreated", 1000L)
                put("isPinned", 0)
                put("noteStatus", "ACTIVE")
                put("color", 0)
            }
            binDb.insertWithOnConflict("notes_table", null, binValues, SQLiteDatabase.CONFLICT_REPLACE)
        }

        // Create old archive database with archived notes
        SQLiteDatabase.openDatabase(archiveDbPath, null, SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY).use { archiveDb ->
            archiveDb.execSQL(
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
            val archiveValues = ContentValues().apply {
                put("title", "Archived Note")
                put("body", "This note is archived")
                put("dateCreated", 2000L)
                put("isPinned", 1)
                put("noteStatus", "ACTIVE")
                put("color", 0)
            }
            archiveDb.insertWithOnConflict("notes_table", null, archiveValues, SQLiteDatabase.CONFLICT_REPLACE)
        }

        // Create main database v1 with one active note
        helper.createDatabase(dbName, 1).use { db ->
            val mainValues = ContentValues().apply {
                put("title", "Active Note")
                put("body", "This is active")
                put("dateCreated", 3000L)
                put("isPinned", 0)
                put("noteStatus", "ACTIVE")
                put("color", 0)
            }
            db.insert("notes_table", SQLiteDatabase.CONFLICT_REPLACE, mainValues)
        }

        // Run migration
        helper.runMigrationsAndValidate(
            name = dbName,
            version = 2,
            validateDroppedTables = true,
            NoteDatabase.MIGRATION_1_2(context)
        )

        // Verify all notes are in the migrated database with correct statuses
        roomDb = Room.databaseBuilder(context, NoteDatabase::class.java, dbName)
            .allowMainThreadQueries()
            .build()
        val dao = roomDb.dao

        val activeNotes = runBlocking { dao.getAllNotesByStatus(NoteStatus.ACTIVE).first() }
        val binnedNotes = runBlocking { dao.getAllNotesByStatus(NoteStatus.BINNED).first() }
        val archivedNotes = runBlocking { dao.getAllNotesByStatus(NoteStatus.ARCHIVED).first() }

        assertEquals(1, activeNotes.size)
        assertEquals("Active Note", activeNotes[0].title)
        assertEquals("This is active", activeNotes[0].body)

        assertEquals(1, binnedNotes.size)
        assertEquals("Binned Note", binnedNotes[0].title)
        assertEquals("This note is in bin", binnedNotes[0].body)
        assertEquals(NoteStatus.BINNED, binnedNotes[0].noteStatus)

        assertEquals(1, archivedNotes.size)
        assertEquals("Archived Note", archivedNotes[0].title)
        assertEquals("This note is archived", archivedNotes[0].body)
        assertEquals(NoteStatus.ARCHIVED, archivedNotes[0].noteStatus)
        assertTrue(archivedNotes[0].isPinned)

        // Cleanup old databases
        context.deleteDatabase("bin.db")
        context.deleteDatabase("archive.db")
    }


    @After
    fun tearDown() {
        if (::roomDb.isInitialized) roomDb.close()
    }
}
