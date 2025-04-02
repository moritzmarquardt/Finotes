package de.marquisproject.finotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ImportExportViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    private val _notesList = noteRepository.fetchAllNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _archivedList = noteRepository.fetchAllArchivedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _importExportState = MutableStateFlow(ImportExportState())
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

    val importExportState = combine(
        _importExportState,
        _exportData,
        _exportSettings,
    ) { importExportState, exportData, exportSettings ->
        importExportState.copy(
            exportData = exportData,
            exportSettings = exportSettings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ImportExportState()
    )

    fun setMode(mode: ImportExportMode) {
        _importExportState.value = _importExportState.value.copy(mode = mode)
    }

    fun setExportSettings(exportSettings: ExportSettings) {
        _exportSettings.value = exportSettings
    }

    fun setExportJson() {
        val exportData = _exportData.value
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val jsonAdapter = moshi.adapter(ExportData::class.java)
        val backupJson = jsonAdapter.toJson(exportData)

        _importExportState.value = _importExportState.value.copy(exportJson = backupJson)
    }

    fun restoreBackup(jsonString: String) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(ExportData::class.java)

        jsonAdapter.fromJson(jsonString)?.let { backupData ->
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    noteRepository.insertNotes(backupData.notes)
                    noteRepository.insertNotesToArchive(backupData.archivedNotes)
                }
            }
        }
    }
}