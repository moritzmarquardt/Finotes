package de.marquisproject.finotes.data.backup

import de.marquisproject.finotes.data.backup.ExportData
import kotlinx.serialization.json.Json
import java.security.MessageDigest

/**
 * Encodes and decodes the versioned backup envelope, including the
 * SHA-256 checksum over the encoded [ExportData] payload.
 *
 * Decoding of unencrypted backups verifies the checksum when present and
 * accepts checksum-less v1 files for backward compatibility with existing
 * user backups.
 */
object BackupCodec {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    fun encode(envelope: BackupEnvelope): ByteArray {
        val dataJson = json.encodeToString(envelope.data)
        val withChecksum = envelope.copy(
            checksum = sha256Hex(dataJson.encodeToByteArray())
        )
        return json.encodeToString(withChecksum).encodeToByteArray()
    }

    /**
     * @throws BackupFormatException if the input is not a Finotes backup,
     * is a future version, or fails checksum verification.
     */
    fun decode(bytes: ByteArray): BackupEnvelope {
        val envelope = try {
            json.decodeFromString<BackupEnvelope>(bytes.decodeToString())
        } catch (e: Exception) {
            throw BackupFormatException("Not a valid Finotes backup file", e)
        }
        if (envelope.format != BackupEnvelope.COMPANION_FORMAT) {
            throw BackupFormatException(
                "Unexpected backup format '${envelope.format}', expected '${BackupEnvelope.COMPANION_FORMAT}'"
            )
        }
        if (envelope.version > BackupEnvelope.CURRENT_BACKUP_VERSION) {
            throw BackupFormatException(
                "Backup version ${envelope.version} is newer than the supported version ${BackupEnvelope.CURRENT_BACKUP_VERSION}. Please update the app."
            )
        }
        envelope.checksum?.let { expected ->
            val actual = sha256Hex(json.encodeToString(envelope.data).encodeToByteArray())
            if (!expected.equals(actual, ignoreCase = true)) {
                throw BackupFormatException("Backup checksum mismatch: the file is corrupted or truncated")
            }
        }
        return envelope
    }

    /**
     * Decodes a legacy v1 backup that has no envelope (plain ExportData).
     */
    fun decodeLegacy(bytes: ByteArray): BackupEnvelope {
        val data = try {
            json.decodeFromString<ExportData>(bytes.decodeToString())
        } catch (e: Exception) {
            throw BackupFormatException("Not a valid Finotes backup file", e)
        }
        return BackupEnvelope(data = data)
    }

    /**
     * Encrypted backups carry a magic prefix so the importer can distinguish
     * them from plain JSON without trial-decryption.
     */
    private val encryptedMagic: ByteArray = "FNOTESENC1".encodeToByteArray()

    fun isEncrypted(bytes: ByteArray): Boolean {
        if (bytes.size <= encryptedMagic.size) return false
        return bytes.copyOfRange(0, encryptedMagic.size).contentEquals(encryptedMagic)
    }

    fun wrapEncrypted(encrypted: ByteArray): ByteArray = encryptedMagic + encrypted

    fun unwrapEncrypted(bytes: ByteArray): ByteArray =
        bytes.copyOfRange(encryptedMagic.size, bytes.size)

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }
}

class BackupFormatException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
