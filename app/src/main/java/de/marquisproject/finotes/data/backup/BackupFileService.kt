package de.marquisproject.finotes.data.backup

import android.content.ContentResolver
import android.net.Uri
import java.io.File
import java.io.IOException

/**
 * Reads and writes backup files through the Storage Access Framework.
 *
 * Exports are atomic from the app's perspective: the payload is fully written
 * to a local temporary file first and only copied over the SAF document on
 * success, so a crash or disk-full error never leaves a truncated backup that
 * looks valid. Imports are size-capped to protect against out-of-memory on
 * maliciously large files.
 */
class BackupFileService(
    private val contentResolver: ContentResolver,
    private val cacheDir: File
) {

    fun writeBackup(uri: Uri, bytes: ByteArray) {
        val tempFile = File.createTempFile("finotes_export", ".tmp", cacheDir)
        try {
            tempFile.outputStream().use { it.write(bytes) }
            val outputStream = contentResolver.openOutputStream(uri, "wt")
                ?: throw IOException("Could not open the selected destination for writing")
            outputStream.use { out ->
                tempFile.inputStream().use { it.copyTo(out) }
            }
        } finally {
            tempFile.delete()
        }
    }

    /**
     * @throws BackupTooLargeException if the file exceeds [maxImportBytes].
     * @throws IOException if the file cannot be read.
     */
    fun readBackup(uri: Uri, maxImportBytes: Long = MAX_IMPORT_BYTES): ByteArray {
        val fileSize = contentResolver.queryFileSize(uri)
            ?: throw IOException("Could not access the selected file")
        if (fileSize > maxImportBytes) {
            throw BackupTooLargeException(fileSize, maxImportBytes)
        }
        return contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Could not read the selected file")
    }

    private fun ContentResolver.queryFileSize(uri: Uri): Long? =
        query(uri, arrayOf(android.provider.OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst() && !cursor.isNull(sizeIndex)) {
                cursor.getLong(sizeIndex)
            } else {
                null
            }
        }

    companion object {
        const val MAX_IMPORT_BYTES = 64L * 1024 * 1024
    }
}

class BackupTooLargeException(val fileSize: Long, val maxBytes: Long) :
    IOException("Backup file is too large (${fileSize / 1_000_000} MB, limit is ${maxBytes / 1_000_000} MB)")
