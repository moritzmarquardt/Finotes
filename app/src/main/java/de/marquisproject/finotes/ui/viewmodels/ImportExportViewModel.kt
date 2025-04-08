package de.marquisproject.finotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.marquisproject.finotes.data.notes.model.Note
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


class ImportExportViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    private val _notesList = noteRepository.fetchAllNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _archivedList = noteRepository.fetchAllArchivedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _exportSettings = MutableStateFlow(ExportSettings())
    private val _exportData = combine(
        _notesList,
        _archivedList,
        _exportSettings
    ) { notesList, archivedList, exportSettings ->
        if (exportSettings.includeArchived) {
            ExportData(notesList, archivedList)
        } else {
            ExportData(notesList)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ExportData())

    private val _importData = MutableStateFlow(ExportData())
    private val _loadedData = MutableStateFlow(ExportData())

    private val _importExportMode = MutableStateFlow(ImportExportMode.EXPORT)

    private val _showFileInfoAlert = MutableStateFlow(false)
    private val _showFinalImportAlert = MutableStateFlow(false)

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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    // exposing the state
    val importExportMode : StateFlow<ImportExportMode> = _importExportMode.asStateFlow()
    val exportSettings : StateFlow<ExportSettings> = _exportSettings.asStateFlow()
    val exportData : StateFlow<ExportData> = _exportData
    val importData : StateFlow<ExportData> = _importData.asStateFlow()
    val loadedData : StateFlow<ExportData> = _loadedData.asStateFlow()
    val showFileInfoAlert : StateFlow<Boolean> = _showFileInfoAlert.asStateFlow()
    val showFinalImportAlert : StateFlow<Boolean> = _showFinalImportAlert.asStateFlow()
    val onlyNonDuplicatesInImportData : StateFlow<Boolean> = _onlyNonDuplicatesInImportData

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

    fun createExportDataJson() : String {
        /**
         * Create a JSON string from the export data using Moshi
         */
        val exportData = _exportData.value
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val jsonAdapter = moshi.adapter(ExportData::class.java)
        val exportDataJason = jsonAdapter.toJson(exportData)

        return exportDataJason
    }

    fun loadBackupFile(jsonString: String) {
        viewModelScope.launch {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val jsonAdapter = moshi.adapter(ExportData::class.java)
            val backupData = jsonAdapter.fromJson(jsonString)
                ?: throw IllegalArgumentException("Invalid JSON format")
            val loadedData = ExportData(
                notes = backupData.notes,
                archivedNotes = backupData.archivedNotes
            )
            _loadedData.update { loadedData }
            _importData.update { loadedData }
        }
    }

    fun selectOnlyNonDuplicates() {
        /**
         * Deselect all notes from the inputData that are already in the notesList or archivedList
         * The comparison is done by the note body and title which both have to be the same to be considered a duplicate
         */
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
        /*if (!_inSelectionMode.value) {
            _uiState.value = _uiState.value.copy(inSelectionMode = true)
        }*/
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
    }

    fun importImportData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                noteRepository.insertNotes(_importData.value.notes)
                noteRepository.insertNotesToArchive(_importData.value.archivedNotes)
            }
            _importData.update { ExportData() }
            _loadedData.update { ExportData() }
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
            _importData.update { ExportData(_loadedData.value.notes, _loadedData.value.archivedNotes) }
        }
    }
}