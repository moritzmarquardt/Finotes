package de.marquisproject.finotes.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
            exportData = if (importExportState.exportSettings.includeArchived) {
                ExportData(notesList, archivedList)
            } else {
                ExportData(notesList)
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
        when (_importExportState.value.exportSettings.exportFileFormat) {
            ExportFileFormat.JSON -> createExportJSON(importExportState.value.exportData)
            ExportFileFormat.SQLITE -> createExportSQLITE(importExportState.value.exportData)
        }
    }


    private fun createExportJSON(exportData: ExportData) {
        // export all notes to JSON

        Log.e("Export", "Exporting notes to JSON with data $exportData")

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Log.e("Export", "Moshi built")

        // Convert data to JSON
        val jsonAdapter = moshi.adapter(ExportData::class.java)
        val backupJson = jsonAdapter.toJson(exportData)

        Log.e("Export", "Backup JSON: $backupJson")

        _importExportState.value = _importExportState.value.copy(exportJson = backupJson)

        Log.e("Export", "done")

    }

    fun restoreBackup(jsonString: String) {
        Log.e("Import", "Restoring backup start")
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(ExportData::class.java)

        Log.e("Import", "Restoring backup moshi built")

        jsonAdapter.fromJson(jsonString)?.let { backupData ->
            Log.e("Import", "Backup data: $backupData")
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    noteRepository.insertNotes(backupData.notes)
                    backupData.archivedNotes?.let { noteRepository.insertArchivedNotes(it) }
                }
            }
        }
    }

    private fun createExportSQLITE(exportData: ExportData) {
        // export all notes to SQLite
    }

}