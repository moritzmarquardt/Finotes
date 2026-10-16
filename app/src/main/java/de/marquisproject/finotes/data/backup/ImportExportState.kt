package de.marquisproject.finotes.data.backup

import de.marquisproject.finotes.data.notes.model.Note
import kotlinx.serialization.Serializable

enum class ImportExportMode {
    EXPORT,
    IMPORT
}

enum class ExportFileFormat {
    JSON,
}

data class ExportSettings(
    val exportFileFormat: ExportFileFormat = ExportFileFormat.JSON,
    val includeArchived: Boolean = true,
    val includeBinned: Boolean = true
)

@Serializable
data class ExportData(
    val notes: List<Note> = emptyList(),
    val archivedNotes: List<Note> = emptyList(),
    val binnedNotes: List<Note> = emptyList(),
)

/** Result of a backup file operation, surfaced to the UI instead of Toasts fired from the ViewModel. */
data class BackupOperationResult(
    val success: Boolean,
    val message: String
)
