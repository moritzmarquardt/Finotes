package de.marquisproject.finotes.ui.viewmodels

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class BackupData(
    val notes: List<Note>,
)


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

        Log.e("Export", "Exporting notes to JSON")

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Log.e("Export", "Moshi built")

        // Convert data to JSON
        val jsonAdapter = moshi.adapter(BackupData::class.java)
        val backupJson = jsonAdapter.toJson(BackupData(noteList))

        // Save to file
        //val file = File(context.getExternalFilesDir(null), "notes_backup.json")
        //file.writeText(backupJson)

        Log.e("Export", "Backup JSON: $backupJson")

        _importExportState.value = _importExportState.value.copy(exportJson = backupJson)

        Log.e("Export", "done")

    }

    fun restoreBackup(jsonString: String) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(BackupData::class.java)

        Log.e("Import", "Restoring backup")

        jsonAdapter.fromJson(jsonString)?.let { backupData ->
            Log.e("Import", "Backup data: ${backupData.notes}")
        }
    }

    private fun exportNotesToSQLite(noteList: List<Note>) {
        // export all notes to SQLite
    }

}