package de.marquisproject.finotes.ui.viewmodels

import android.app.Application
import de.marquisproject.finotes.data.backup.BackupCodec
import de.marquisproject.finotes.data.backup.BackupCrypto
import de.marquisproject.finotes.data.backup.ExportData
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
import org.junit.Assert.assertFalse
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
        archivedNotes: List<Note> = emptyList(),
        binnedNotes: List<Note> = emptyList()
    ): ImportExportViewModel {
        val fakeDao = object : NoteDAO {
            val insertedNotes = mutableListOf<Note>()
            var failNextInsert = false
            override suspend fun insertNote(note: Note): Long {
                if (failNextInsert) throw RuntimeException("Simulated database failure")
                insertedNotes.add(note)
                return insertedNotes.size.toLong()
            }
            override suspend fun insertNotesTransaction(notes: List<Note>) {
                notes.forEach { insertNote(it) }
            }
            override suspend fun updateNotes(notes: List<Note>) {}
            override suspend fun deleteNotes(note: List<Note>) {}
            override fun getNoteById(noteId: Long): Flow<Note> = flowOf()
            override fun getAllNotes(): Flow<List<Note>> = flowOf(activeNotes + archivedNotes)
            override fun getAllNotesByStatus(noteStatus: NoteStatus): Flow<List<Note>> = when (noteStatus) {
                NoteStatus.ACTIVE -> flowOf(activeNotes)
                NoteStatus.ARCHIVED -> flowOf(archivedNotes)
                NoteStatus.BINNED -> flowOf(binnedNotes)
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
     * Verifies that the ViewModel exports active, archived and binned notes
     * into a valid versioned backup envelope.
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

        assertTrue(jsonOutput.contains("finotes-backup"))
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

    @Test
    fun testViewModel_createBackupFileBytes_isVersionedEnvelopeWithChecksum() = runTest {
        val viewModel = createViewModel(
            activeNotes = listOf(Note(id = 1L, title = "Note", body = "Body"))
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.exportData.collect()
        }

        val envelope = BackupCodec.decode(viewModel.createBackupFileBytes())

        assertEquals("finotes-backup", envelope.format)
        assertEquals(2, envelope.version)
        assertEquals(1, envelope.data.notes.size)
        assertTrue(envelope.checksum!!.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun testViewModel_createBackupFileBytes_encryptedWithPassphrase() = runTest {
        val viewModel = createViewModel(
            activeNotes = listOf(Note(id = 1L, title = "Secret", body = "Body"))
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.exportData.collect()
        }
        viewModel.setExportPassphrase("my-passphrase")

        val fileBytes = viewModel.createBackupFileBytes()

        assertTrue(BackupCodec.isEncrypted(fileBytes))
        val decrypted = BackupCrypto.decrypt(
            BackupCodec.unwrapEncrypted(fileBytes),
            "my-passphrase".toCharArray()
        )
        val envelope = BackupCodec.decode(decrypted)
        assertEquals("Secret", envelope.data.notes.first().title)
    }

    @Test
    fun testViewModel_createBackupFileBytes_unencryptedWithoutPassphrase() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.exportData.collect()
        }

        val fileBytes = viewModel.createBackupFileBytes()

        assertFalse(BackupCodec.isEncrypted(fileBytes))
        assertTrue(fileBytes.decodeToString().contains("finotes-backup"))
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

    @Test
    fun testViewModel_exportIncludesBinnedNotes() = runTest {
        val binnedNote = Note(id = 3L, title = "Binned Note", noteStatus = NoteStatus.BINNED)
        val viewModel = createViewModel(binnedNotes = listOf(binnedNote))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.exportData.collect()
        }

        val envelope = BackupCodec.decode(viewModel.createBackupFileBytes())

        assertEquals(1, envelope.data.binnedNotes.size)
        assertEquals("Binned Note", envelope.data.binnedNotes.first().title)
    }

    @Test
    fun testViewModel_suggestBackupFileName_containsTimestampAndJsonExtension() {
        val viewModel = createViewModel()

        val fileName = viewModel.suggestBackupFileName()

        assertTrue(fileName.matches(Regex("Finotes_backup_\\d{8}_\\d{6}\\.json")))
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

    @Test
    fun testViewModel_loadBackupFile_checksumMismatchRejected() {
        val viewModel = createViewModel()
        val valid = BackupCodec.encode(
            de.marquisproject.finotes.data.backup.BackupEnvelope(
                data = ExportData(notes = listOf(Note(id = 1L, title = "Tampered")))
            )
        ).decodeToString()
        val tampered = valid.replace("Tampered", "Tampered2")

        val loaded = viewModel.loadBackupFile(tampered)

        assertFalse(loaded)
        assertTrue(viewModel.loadedData.value.notes.isEmpty())
    }
}
