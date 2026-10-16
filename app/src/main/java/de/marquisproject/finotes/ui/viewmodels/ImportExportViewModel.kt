package de.marquisproject.finotes.ui.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.data.backup.BackupCodec
import de.marquisproject.finotes.data.backup.BackupCrypto
import de.marquisproject.finotes.data.backup.BackupDecryptionException
import de.marquisproject.finotes.data.backup.BackupEnvelope
import de.marquisproject.finotes.data.backup.BackupFileService
import de.marquisproject.finotes.data.backup.BackupFormatException
import de.marquisproject.finotes.data.backup.BackupTooLargeException
import de.marquisproject.finotes.data.backup.ExportData
import de.marquisproject.finotes.data.backup.ImportExportMode
import de.marquisproject.finotes.data.backup.ExportSettings
import de.marquisproject.finotes.data.backup.BackupOperationResult
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    application: Application
) : AndroidViewModel(application) {

    private val backupFileService by lazy {
        BackupFileService(
            getApplication<Application>().contentResolver,
            getApplication<Application>().cacheDir
        )
    }

    private val _notesList = noteRepository.fetchAllNotesByStatus(NoteStatus.ACTIVE).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _archivedList = noteRepository.fetchAllNotesByStatus(NoteStatus.ARCHIVED).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _binnedList = noteRepository.fetchAllNotesByStatus(NoteStatus.BINNED).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _exportSettings = MutableStateFlow(ExportSettings())
    private val _exportData = combine(
        _notesList,
        _archivedList,
        _binnedList,
        _exportSettings
    ) { notesList, archivedList, binnedList, exportSettings ->
        ExportData(
            notes = notesList,
            archivedNotes = if (exportSettings.includeArchived) archivedList else emptyList(),
            binnedNotes = if (exportSettings.includeBinned) binnedList else emptyList(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExportData())

    private val _importData = MutableStateFlow(ExportData())
    private val _loadedData = MutableStateFlow(ExportData())
    private val _importExportMode = MutableStateFlow(ImportExportMode.EXPORT)
    private val _showFileInfoAlert = MutableStateFlow(false)
    private val _showFinalImportAlert = MutableStateFlow(false)
    private val _showImportLoading = MutableStateFlow(false)
    private val _exportPassphrase = MutableStateFlow("")
    private val _importPassphrase = MutableStateFlow("")
    private val _operationResult = MutableStateFlow<BackupOperationResult?>(null)

    private val _onlyNonDuplicatesInImportData = combine(
        _loadedData,
        _importData,
        _notesList,
        _archivedList
    ) { loadedData, importData, notesList, archivedList ->
        val notesNonDuplicates = loadedData.notes.filter { note ->
            !notesList.any { it.title == note.title && it.body == note.body }
        }
        val archivedNotesNonDuplicates = loadedData.archivedNotes.filter { note ->
            !archivedList.any { it.title == note.title && it.body == note.body }
        }
        val notesNonDuplicatesSelected = importData.notes.toSet() == notesNonDuplicates.toSet()
        val archivedNotesNonDuplicatesSelected = importData.archivedNotes.toSet() == archivedNotesNonDuplicates.toSet()
        return@combine notesNonDuplicatesSelected && archivedNotesNonDuplicatesSelected
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // exposing the state
    val importExportMode : StateFlow<ImportExportMode> = _importExportMode.asStateFlow()
    val exportSettings : StateFlow<ExportSettings> = _exportSettings.asStateFlow()
    val exportData : StateFlow<ExportData> = _exportData
    val importData : StateFlow<ExportData> = _importData.asStateFlow()
    val loadedData : StateFlow<ExportData> = _loadedData.asStateFlow()
    val showFileInfoAlert : StateFlow<Boolean> = _showFileInfoAlert.asStateFlow()
    val showFinalImportAlert : StateFlow<Boolean> = _showFinalImportAlert.asStateFlow()
    val onlyNonDuplicatesInImportData : StateFlow<Boolean> = _onlyNonDuplicatesInImportData
    val showImportLoading : StateFlow<Boolean> = _showImportLoading.asStateFlow()
    val exportPassphrase : StateFlow<String> = _exportPassphrase.asStateFlow()
    val importPassphrase : StateFlow<String> = _importPassphrase.asStateFlow()
    val operationResult : StateFlow<BackupOperationResult?> = _operationResult.asStateFlow()

    fun setMode(mode: ImportExportMode) {
        _importExportMode.update { mode }
    }

    fun setExportSettings(exportSettings: ExportSettings) {
        _exportSettings.update { exportSettings }
    }

    fun setShowFileInfoAlert(show: Boolean) {
        _showFileInfoAlert.update { show }
    }

    fun setShowFinalImportAlert(show: Boolean) {
        _showFinalImportAlert.update { show }
    }

    fun setExportPassphrase(passphrase: String) {
        _exportPassphrase.update { passphrase }
    }

    fun setImportPassphrase(passphrase: String) {
        _importPassphrase.update { passphrase }
    }

    fun consumeOperationResult() {
        _operationResult.update { null }
    }

    /**
     * Exports the current notes as a versioned backup file, optionally
     * encrypted with the user's passphrase. Returns the result through
     * [operationResult] instead of firing Toasts from the ViewModel.
     */
    fun exportNotesToFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                val payload = createBackupFileBytes()
                backupFileService.writeBackup(uri, payload)
                BackupOperationResult(
                    success = true,
                    message = "Backup saved successfully"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Export failed", e)
                BackupOperationResult(
                    success = false,
                    message = userMessage(e)
                )
            }
            publishResult(result)
        }
    }

    /**
     * Imports notes from a backup file. Encrypted backups are decrypted with
     * the entered passphrase; the parsed notes are shown for review before
     * anything is written to the database.
     */
    fun importNotesFromFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                val fileBytes = backupFileService.readBackup(uri)
                val envelope = parseBackupFile(fileBytes)
                applyLoadedData(envelope.data)
                BackupOperationResult(
                    success = true,
                    message = "Backup loaded: ${envelope.data.notes.size} notes, " +
                        "${envelope.data.archivedNotes.size} archived"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                BackupOperationResult(
                    success = false,
                    message = userMessage(e)
                )
            }
            publishResult(result)
        }
    }

    private fun parseBackupFile(fileBytes: ByteArray): BackupEnvelope {
        if (BackupCodec.isEncrypted(fileBytes)) {
            val passphrase = _importPassphrase.value
            if (passphrase.isEmpty()) {
                throw BackupDecryptionException("This backup is encrypted: enter its passphrase first")
            }
            val decrypted = BackupCrypto.decrypt(
                BackupCodec.unwrapEncrypted(fileBytes),
                passphrase.toCharArray()
            )
            return BackupCodec.decode(decrypted)
        }
        return try {
            BackupCodec.decode(fileBytes)
        } catch (e: BackupFormatException) {
            if (e.cause is SerializationException) {
                BackupCodec.decodeLegacy(fileBytes)
            } else {
                throw e
            }
        }
    }

    private fun applyLoadedData(exportData: ExportData) {
        _loadedData.update { exportData }
        _importData.update { exportData }
    }

    private suspend fun publishResult(result: BackupOperationResult) {
        withContext(Dispatchers.Main) {
            _operationResult.update { result }
        }
    }

    private fun userMessage(e: Exception): String = when (e) {
        is BackupDecryptionException -> e.message ?: "Wrong passphrase or corrupted backup"
        is BackupFormatException -> e.message ?: "Invalid backup file"
        is BackupTooLargeException -> e.message ?: "Backup file is too large"
        is IOException -> "Could not access the file"
        else -> "Unexpected error while processing the backup"
    }

    /**
     * Builds the complete backup file: a versioned envelope with checksum,
     * optionally encrypted with AES-GCM.
     */
    fun createBackupFileBytes(): ByteArray {
        val envelope = BackupEnvelope(
            appVersion = appVersion(),
            createdAt = System.currentTimeMillis(),
            data = _exportData.value
        )
        val plainBytes = BackupCodec.encode(envelope)
        val passphrase = _exportPassphrase.value
        return if (passphrase.isNotEmpty()) {
            BackupCodec.wrapEncrypted(BackupCrypto.encrypt(plainBytes, passphrase.toCharArray()))
        } else {
            plainBytes
        }
    }

    private fun appVersion(): String = try {
        getApplication<Application>().packageManager
            .getPackageInfo(getApplication<Application>().packageName, 0).versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }

    fun loadBackupFile(jsonString: String): Boolean {
        return try {
            val envelope = when {
                jsonString.contains("\"format\"") || jsonString.contains("\"version\"") -> {
                    BackupCodec.decode(jsonString.encodeToByteArray())
                }
                else -> BackupCodec.decodeLegacy(jsonString.encodeToByteArray())
            }
            applyLoadedData(envelope.data)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse backup JSON", e)
            false
        }
    }

    fun selectOnlyNonDuplicates() {
        viewModelScope.launch {
            val notes = _loadedData.value.notes.filter { note ->
                !_notesList.value.any { it.title == note.title && it.body == note.body }
            }
            val archivedNotes = _loadedData.value.archivedNotes.filter { note ->
                !_archivedList.value.any { it.title == note.title && it.body == note.body }
            }

            _importData.update { _importData.value.copy(notes = notes, archivedNotes = archivedNotes) }
        }
    }

    fun longClickSelect(note: Note) {
        if (_loadedData.value.notes.contains(note)) {
            if (!_importData.value.notes.contains(note)) {
                _importData.update { it.copy(notes = it.notes + note) }
            } else {
                _importData.update { it.copy(notes = it.notes - note) }
            }
        }
        if (_loadedData.value.archivedNotes.contains(note)) {
            if (!_importData.value.archivedNotes.contains(note)) {
                _importData.update { it.copy(archivedNotes = it.archivedNotes + note) }
            } else {
                _importData.update { it.copy(archivedNotes = it.archivedNotes - note) }
            }
        }
        if (_loadedData.value.binnedNotes.contains(note)) {
            if (!_importData.value.binnedNotes.contains(note)) {
                _importData.update { it.copy(binnedNotes = it.binnedNotes + note) }
            } else {
                _importData.update { it.copy(binnedNotes = it.binnedNotes - note) }
            }
        }
    }

    /**
     * Writes the selected notes to the database in a single transaction.
     * On failure the database is left untouched and the error is surfaced
     * through [operationResult].
     */
    fun importImportData() {
        viewModelScope.launch {
            _showImportLoading.update { true }
            val result = try {
                withContext(Dispatchers.IO) {
                    noteRepository.insertNotes(
                        _importData.value.notes.map { it.copy(id = null, noteStatus = NoteStatus.ACTIVE) } +
                            _importData.value.archivedNotes.map { it.copy(id = null, noteStatus = NoteStatus.ARCHIVED) } +
                            _importData.value.binnedNotes.map { it.copy(id = null, noteStatus = NoteStatus.BINNED) }
                    )
                }
                _importData.update { ExportData() }
                _loadedData.update { ExportData() }
                BackupOperationResult(success = true, message = "Notes imported successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                BackupOperationResult(success = false, message = "Import failed: the database was not modified")
            } finally {
                _showImportLoading.update { false }
            }
            publishResult(result)
        }
    }

    fun clearImportData() {
        viewModelScope.launch {
            _importData.update { ExportData() }
            _loadedData.update { ExportData() }
        }
    }

    fun deselectAllNotes() {
        viewModelScope.launch {
            _importData.update { ExportData() }
        }
    }

    fun selectAllNotes() {
        viewModelScope.launch {
            _importData.update { _loadedData.value }
        }
    }

    /**
     * The unencrypted backup envelope as a JSON string.
     */
    fun createExportDataJson(): String {
        return createBackupFileBytes().decodeToString()
    }

    fun suggestBackupFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "Finotes_backup_$timestamp.json"
    }

    companion object {
        private const val TAG = "ImportExportViewModel"
        internal val json = BackupCodec.json
    }
}
