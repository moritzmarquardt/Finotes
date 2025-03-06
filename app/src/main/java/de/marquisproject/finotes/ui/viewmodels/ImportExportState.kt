package de.marquisproject.finotes.ui.viewmodels

import de.marquisproject.finotes.data.notes.model.Note

enum class ImportExportMode {
    EXPORT,
    IMPORT
}

enum class ExportFileFormat {
    JSON,
    SQLITE
}

data class ExportSettings (
    val exportFileFormat: ExportFileFormat = ExportFileFormat.JSON,
    val includeArchived: Boolean = true
)

data class ImportExportState(
    val mode: ImportExportMode = ImportExportMode.EXPORT,
    val exportSettings: ExportSettings = ExportSettings(),
    val notesToImport: List<Note> = emptyList(),
    val notesToExport: List<Note> = emptyList(),
)