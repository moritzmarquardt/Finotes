package de.marquisproject.finotes.ui.viewmodels

import android.app.Application
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import de.marquisproject.finotes.data.notes.sources.NoteDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportExportJsonTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        activeNotes: List<Note> = emptyList(),
        archivedNotes: List<Note> = emptyList()
    ): ImportExportViewModel {
        val fakeDao = object : NoteDAO {
            override suspend fun insertNote(note: Note): Long = 1L
            override suspend fun updateNotes(notes: List<Note>) {}
            override suspend fun deleteNotes(note: List<Note>) {}
            override fun getNoteById(noteId: Long): Flow<Note> = flowOf()
            override fun getAllNotes(): Flow<List<Note>> = flowOf(activeNotes + archivedNotes)
            override fun getAllNotesByStatus(noteStatus: NoteStatus): Flow<List<Note>> = when (noteStatus) {
                NoteStatus.ACTIVE -> flowOf(activeNotes)
                NoteStatus.ARCHIVED -> flowOf(archivedNotes)
                else -> flowOf(emptyList())
            }
            override fun getNotesWithQuery(
                searchQuery: String?,
                isPinned: Boolean?,
                categories: List<Long>?,
                noteStatus: NoteStatus?,
                hasCategories: Boolean
            ): Flow<List<Note>> = flowOf(emptyList())
        }
        val repository = NoteRepository(fakeDao)
        val application = Application()
        return ImportExportViewModel(repository, application)
    }

    /**
     * Tests ImportExportViewModel.createExportDataJson() directly.
     * Verifies that the ViewModel exports active and archived notes into a valid JSON string.
     */
    @Test
    fun testViewModel_createExportDataJson() = runTest {
        val activeNote = Note(id = 1L, title = "Active Note", body = "Active Body", noteStatus = NoteStatus.ACTIVE)
        val archivedNote = Note(id = 2L, title = "Archived Note", body = "Archived Body", noteStatus = NoteStatus.ARCHIVED)

        val viewModel = createViewModel(
            activeNotes = listOf(activeNote),
            archivedNotes = listOf(archivedNote)
        )

        // necessary to collect the flow in the background when not in production
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.exportData.collect()
        }

        val jsonOutput = viewModel.createExportDataJson()

        assertTrue(jsonOutput.contains("Active Note"))
        assertTrue(jsonOutput.contains("Archived Note"))
        assertTrue(jsonOutput.contains("Active Body"))
        assertTrue(jsonOutput.contains("Archived Body"))
        assertTrue(jsonOutput.contains("ACTIVE"))
        assertTrue(jsonOutput.contains("ARCHIVED"))
        assertTrue(jsonOutput.contains("id"))
        assertTrue(jsonOutput.contains("title"))
        assertTrue(jsonOutput.contains("body"))
        assertTrue(jsonOutput.contains("noteStatus"))
    }

    /**
     * Tests ImportExportViewModel.loadBackupFile() with the real user legacy database export.
     */
    @Test
    fun testViewModel_loadBackupFile_userProvidedLegacyDatabaseExport() {
        val viewModel = createViewModel()
        val userLegacyJson = """
            {
              "notes": [
                {
                  "id": 12,
                  "title": "Note Title example",
                  "body": "Note Body example",
                  "dateCreated": 1743798479384,
                  "isPinned": true,
                  "noteStatus": "ACTIVE",
                  "color": 0
                },
                {
                  "id": 1,
                  "title": "watchlist",
                  "body": "watchlist body",
                  "dateCreated": 1743708129176,
                  "isPinned": true,
                  "noteStatus": "ACTIVE",
                  "color": 0
                }
              ]
            }
        """.trimIndent()

        viewModel.loadBackupFile(userLegacyJson)

        val loadedData = viewModel.loadedData.value
        assertEquals(2, loadedData.notes.size)
        assertEquals("Note Title example", loadedData.notes.first().title)
        assertEquals("watchlist", loadedData.notes[1].title)
    }

    /**
     * Tests round-trip export & import using ViewModel functions:
     * createExportDataJson() -> loadBackupFile()
     */
    @Test
    fun testViewModel_roundTripExportAndImport() = runTest {
        val activeNote = Note(id = 10L, title = "Round Trip Note", body = "Testing ViewModel functions")
        val viewModelExport = createViewModel(activeNotes = listOf(activeNote))

        // necessary to collect the flow in the background when not in production
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModelExport.exportData.collect()
        }

        val exportedJson = viewModelExport.createExportDataJson()

        val viewModelImport = createViewModel()
        viewModelImport.loadBackupFile(exportedJson)

        val importedData = viewModelImport.loadedData.value
        assertEquals(1, importedData.notes.size)
        assertEquals("Round Trip Note", importedData.notes.first().title)
        assertEquals("Testing ViewModel functions", importedData.notes.first().body)
    }

    /**
     * Tests ImportExportViewModel.loadBackupFile() with unknown keys.
     */
    @Test
    fun testViewModel_loadBackupFile_unknownFutureKeys() {
        val viewModel = createViewModel()
        val jsonWithExtraKeys = """
            {
              "appVersion": "2.0.0",
              "notes": [
                {
                  "id": 1,
                  "title": "Future Note",
                  "body": "Note with future fields",
                  "newFutureField": "ignored_value"
                }
              ]
            }
        """.trimIndent()

        viewModel.loadBackupFile(jsonWithExtraKeys)

        val loadedData = viewModel.loadedData.value
        assertEquals(1, loadedData.notes.size)
        assertEquals("Future Note", loadedData.notes.first().title)
    }

    /**
     * Tests ImportExportViewModel.loadBackupFile() when JSON contains null values for fields with defaults.
     */
    @Test
    fun testViewModel_loadBackupFile_nullValuesForFieldsWithDefaults() {
        val viewModel = createViewModel()
        val jsonWithNulls = """
            {
              "notes": [
                {
                  "id": null,
                  "title": null,
                  "body": null,
                  "category": null,
                  "isPinned": null,
                  "noteStatus": null
                }
              ]
            }
        """.trimIndent()

        viewModel.loadBackupFile(jsonWithNulls)

        val loadedData = viewModel.loadedData.value
        assertEquals(1, loadedData.notes.size)
        val note = loadedData.notes.first()
        assertNull(note.id)
        assertEquals("", note.title)
        assertEquals("", note.body)
        assertEquals(0L, note.category)
        assertEquals(false, note.isPinned)
        assertEquals(NoteStatus.ACTIVE, note.noteStatus)
    }

    /**
     * Tests that ImportExportViewModel.loadBackupFile() handles corrupted JSON gracefully.
     */
    @Test
    fun testViewModel_loadBackupFile_corruptedJsonHandledGracefully() {
        val viewModel = createViewModel()
        val corruptedJson = "INVALID_JSON_CONTENT_<<<>>>"

        viewModel.loadBackupFile(corruptedJson)

        val loadedData = viewModel.loadedData.value
        assertTrue(loadedData.notes.isEmpty())
        assertTrue(loadedData.archivedNotes.isEmpty())
    }

    /**
     * Helper function to read test resource files from src/test/backup
     * @param path the path to the resource file relative to src/test/backup
     * @return the content of the file as a String
     */
    private fun readTestResource(path: String): String {
        return javaClass.getResourceAsStream("/$path")?.bufferedReader()?.readText()
            ?: throw IllegalStateException("Test resource not found: $path.")
    }

    /**
     * Tests ImportExportViewModel.loadBackupFile() with an actual backup file
     * created by the old Moshi implementation.
     *
     * This is the most critical test to ensure backward compatibility with old backups.
     *
     * INSTRUCTIONS:
     * 1. Create the directory: app/src/test/backup/
     * 2. Add your actual Moshi-generated backup file as: app/src/test/backup/moshi_backup_export.json
     * 3. Run this test to verify the backup can be loaded
     *
     * If this test fails, users with old backups will NOT be able to restore their data!
     */
    @Test
    fun testViewModel_loadBackupFile_actualMoshiBackup() {
        val viewModel = createViewModel()

        // Read the actual Moshi backup file from test backup
        val moshiBackupJson = readTestResource("backup/backup_version_1.json")
        
        // Load the backup - this should succeed with actual Moshi data
        val result = viewModel.loadBackupFile(moshiBackupJson)
        
        // Verify the backup loaded successfully
        assertTrue("Moshi backup should load successfully", result)
        
        // Add your specific assertions based on the content of your backup file
        // Example:
        val loadedData = viewModel.loadedData.value
        assertTrue("Backup should contain at least one note", loadedData.notes.isNotEmpty())
        
        // Verify the first note has expected data
        // Adjust these assertions to match your actual backup content
        assertEquals("koop", loadedData.notes.first().title)
        assertEquals("- \n- zwiebelkuchen\n\n\n- Tee puderzucker sieb ding,\n- *PFEFFMÜHLE*\n- glascontainer für meine haferflocken, zucker\n- ventilator\n\nideen:\n- pilz bourginion\n- kaiserschmarrn in neuer pfanne\n- spinat mit lachs\n- früchtejoghurt mascarpone", loadedData.notes.first().body)
        assertEquals(NoteStatus.ACTIVE, loadedData.notes.first().noteStatus)
        
        // If your backup contains archived notes:
        assertEquals(1, loadedData.archivedNotes.size)
    }

    /**
     * Tests ImportExportViewModel.loadBackupFile() with special characters and unicode.
     * This ensures that notes with newlines, tabs, emojis, and non-ASCII characters are handled correctly.
     */
    @Test
    fun testViewModel_loadBackupFile_withSpecialCharacters() {
        val viewModel = createViewModel()
        val jsonWithSpecialChars = """
            {
              "notes": [
                {
                  "id": 1,
                  "title": "Note with newlines\nand\ttabs",
                  "body": "Line 1\nLine 2\n\tIndented",
                  "noteStatus": "ACTIVE"
                },
                {
                  "id": 2,
                  "title": "Unicode: 你好世界 🎉",
                  "body": "Emoji test: ❤️ 🎯 ✅",
                  "noteStatus": "ACTIVE"
                },
                {
                  "id": 3,
                  "title": "Special chars: \"quotes\" and 'apostrophes'",
                  "body": "Backslash: \\ Forward slash: /",
                  "noteStatus": "ARCHIVED"
                }
              ],
              "archivedNotes": []
            }
        """.trimIndent()

        val result = viewModel.loadBackupFile(jsonWithSpecialChars)
        assertTrue("Special characters should be handled", result)

        val loadedData = viewModel.loadedData.value
        assertEquals(3, loadedData.notes.size)
        assertEquals("Note with newlines\nand\ttabs", loadedData.notes[0].title)
        assertEquals("Unicode: 你好世界 🎉", loadedData.notes[1].title)
        assertEquals("Special chars: \"quotes\" and 'apostrophes'", loadedData.notes[2].title)
    }
}
