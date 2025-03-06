package de.marquisproject.finotes.ui.viewmodels

import de.marquisproject.finotes.data.notes.model.Note

enum class ImportExportMode {
    EXPORT,
    IMPORT
}

enum class ExportFileFormat {
    JSON,
}

data class ExportSettings (
    val exportFileFormat: ExportFileFormat = ExportFileFormat.JSON,
    val includeArchived: Boolean = true
)

data class ExportData(
    val notes: List<Note> = emptyList(),
    val archivedNotes: List<Note> = emptyList(),
)

data class ImportExportState(
    val mode: ImportExportMode = ImportExportMode.EXPORT,
    val exportSettings: ExportSettings = ExportSettings(),
    val exportData: ExportData = ExportData(),
    val importData: ExportData = ExportData(),
    val exportJson: String = "",
)