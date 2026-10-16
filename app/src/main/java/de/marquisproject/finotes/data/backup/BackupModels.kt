package de.marquisproject.finotes.data.backup

import de.marquisproject.finotes.data.backup.ExportData
import kotlinx.serialization.Serializable

/**
 * Versioned, self-describing envelope for Finotes backup files.
 *
 * The envelope makes backups forward compatible: unknown keys inside [data] are
 * ignored by the lenient Json configuration, while the [format] and [version]
 * fields allow the importer to reject foreign files early and to migrate older
 * formats explicitly instead of guessing.
 */
@Serializable
data class BackupEnvelope(
    val format: String = COMPANION_FORMAT,
    val version: Int = CURRENT_BACKUP_VERSION,
    val appVersion: String? = null,
    val createdAt: Long? = null,
    /** SHA-256 hex digest of the encoded [data] payload used to detect truncation and corruption. */
    val checksum: String? = null,
    val data: ExportData,
) {
    companion object {
        const val COMPANION_FORMAT = "finotes-backup"
        const val CURRENT_BACKUP_VERSION = 2
    }
}
