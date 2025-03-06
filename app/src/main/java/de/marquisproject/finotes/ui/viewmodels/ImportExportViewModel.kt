package de.marquisproject.finotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.marquisproject.finotes.NoteRoute
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ImportExportViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    private val _notesList = noteRepository.getAllNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _archivedList = noteRepository.getAllArchivedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _importExportState = MutableStateFlow(ImportExportState())
    val importExportState = combine(
        _importExportState,
        _notesList,
        _archivedList
    ) { importExportState, notesList, archivedList ->
        importExportState.copy(
            notesToExport = if (importExportState.exportSettings.includeArchived) {
                notesList + archivedList
            } else {
                notesList
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ImportExportState()
    )

    fun setMode(mode: ImportExportMode) {
        _importExportState.value = _importExportState.value.copy(mode = mode)
    }

    fun updateExportSettings(exportSettings: ExportSettings) {
        _importExportState.value = _importExportState.value.copy(exportSettings = exportSettings)
    }

    fun exportNotes() {
        // export all notes depending on the settings
        exportNotesWithSettings(_importExportState.value.exportSettings)
    }

    private fun exportNotesWithSettings(exportSettings: ExportSettings) {
        // export all notes depending on the settings
        if (exportSettings.includeArchived) {
            _importExportState.value = _importExportState.value.copy(notesToExport = _notesList.value + _archivedList.value)
        } else {
            _importExportState.value = _importExportState.value.copy(notesToExport = _notesList.value)
        }

        when (exportSettings.exportFileFormat) {
            ExportFileFormat.JSON -> exportNotesToJSON(_importExportState.value.notesToExport)
            ExportFileFormat.SQLITE -> exportNotesToSQLite(_importExportState.value.notesToExport)
        }
    }

    private fun exportNotesToJSON(noteList: List<Note>) {
        // export all notes to JSON

    }

    private fun exportNotesToSQLite(noteList: List<Note>) {
        // export all notes to SQLite
    }

}