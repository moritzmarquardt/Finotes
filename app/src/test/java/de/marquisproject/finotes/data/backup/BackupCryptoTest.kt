package de.marquisproject.finotes.data.backup

import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.backup.ExportData
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoTest {

    private val passphrase = "correct horse battery staple".toCharArray()

    @Test
    fun encryptDecrypt_roundTrip_returnsOriginalPlaintext() {
        val plaintext = "secret notes".encodeToByteArray()

        val decrypted = BackupCrypto.decrypt(BackupCrypto.encrypt(plaintext, passphrase), passphrase)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun encrypt_samePlaintextTwice_producesDifferentCiphertext() {
        val plaintext = "secret notes".encodeToByteArray()

        val first = BackupCrypto.encrypt(plaintext, passphrase)
        val second = BackupCrypto.encrypt(plaintext, passphrase)

        assertFalse(first.contentEquals(second))
    }

    @Test
    fun decrypt_wrongPassphrase_throwsBackupDecryptionException() {
        val encrypted = BackupCrypto.encrypt("secret".encodeToByteArray(), passphrase)

        assertThrows(BackupDecryptionException::class.java) {
            BackupCrypto.decrypt(encrypted, "wrong passphrase".toCharArray())
        }
    }

    @Test
    fun decrypt_tamperedCiphertext_throwsBackupDecryptionException() {
        val encrypted = BackupCrypto.encrypt("secret".encodeToByteArray(), passphrase)
        val tampered = encrypted.copyOf().also { it[it.size - 1] = (it[it.size - 1].toInt() xor 0x01).toByte() }

        assertThrows(BackupDecryptionException::class.java) {
            BackupCrypto.decrypt(tampered, passphrase)
        }
    }

    @Test
    fun decrypt_truncatedInput_throwsBackupDecryptionException() {
        val encrypted = BackupCrypto.encrypt("secret".encodeToByteArray(), passphrase)

        assertThrows(BackupDecryptionException::class.java) {
            BackupCrypto.decrypt(encrypted.copyOfRange(0, encrypted.size / 2), passphrase)
        }
    }

    @Test
    fun encrypt_emptyPassphrase_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.encrypt("data".encodeToByteArray(), CharArray(0))
        }
    }
}

class BackupCodecTest {

    private fun sampleEnvelope() = BackupEnvelope(
        appVersion = "v1.2.0",
        createdAt = 1_700_000_000_000L,
        data = ExportData(
            notes = listOf(Note(id = 1L, title = "Note", body = "Body")),
            archivedNotes = listOf(Note(id = 2L, title = "Archived", noteStatus = de.marquisproject.finotes.data.notes.model.NoteStatus.ARCHIVED)),
            binnedNotes = listOf(Note(id = 3L, title = "Binned", noteStatus = de.marquisproject.finotes.data.notes.model.NoteStatus.BINNED)),
        )
    )

    @Test
    fun encodeDecode_roundTrip_preservesEnvelope() {
        val envelope = sampleEnvelope()

        val decoded = BackupCodec.decode(BackupCodec.encode(envelope))

        assertEquals(BackupEnvelope.COMPANION_FORMAT, decoded.format)
        assertEquals(BackupEnvelope.CURRENT_BACKUP_VERSION, decoded.version)
        assertEquals("v1.2.0", decoded.appVersion)
        assertEquals(1_700_000_000_000L, decoded.createdAt)
        assertEquals(envelope.data, decoded.data)
    }

    @Test
    fun encode_containsValidSha256Checksum() {
        val encoded = BackupCodec.encode(sampleEnvelope())
        val decoded = BackupCodec.decode(encoded)

        val checksum = decoded.checksum
        assertTrue(checksum != null && checksum.length == 64 && checksum.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun decode_corruptedPayload_failsChecksum() {
        val encoded = BackupCodec.encode(sampleEnvelope()).decodeToString()
        val corrupted = encoded.replace("Note", "N0te").encodeToByteArray()

        assertThrows(BackupFormatException::class.java) {
            BackupCodec.decode(corrupted)
        }
    }

    @Test
    fun decode_truncatedFile_failsChecksumOrParsing() {
        val encoded = BackupCodec.encode(sampleEnvelope())
        val truncated = encoded.copyOfRange(0, encoded.size / 2)

        try {
            BackupCodec.decode(truncated)
            throw AssertionError("Truncated backup must not decode successfully")
        } catch (expected: BackupFormatException) {
        }
    }

    @Test
    fun decode_futureVersion_isRejected() {
        val future = BackupCodec.encode(sampleEnvelope()).decodeToString()
            .replace("\"version\":2", "\"version\":99")
            .encodeToByteArray()

        val exception = assertThrows(BackupFormatException::class.java) {
            BackupCodec.decode(future)
        }
        assertTrue(exception.message!!.contains("newer"))
    }

    @Test
    fun decode_foreignFormat_isRejected() {
        val foreign = """{"format":"other-app","version":1,"notes":[]}""".encodeToByteArray()

        assertThrows(BackupFormatException::class.java) {
            BackupCodec.decode(foreign)
        }
    }

    @Test
    fun decode_legacyPlainExportData_isAccepted() {
        val legacy = """
            {
              "notes": [
                {"id": 12, "title": "Legacy Note", "body": "Legacy Body", "noteStatus": "ACTIVE"}
              ]
            }
        """.trimIndent().encodeToByteArray()

        val envelope = BackupCodec.decodeLegacy(legacy)

        assertEquals(1, envelope.data.notes.size)
        assertEquals("Legacy Note", envelope.data.notes.first().title)
    }

    @Test
    fun encryptedFiles_areIdentifiableByMagicPrefix() {
        val encrypted = BackupCodec.wrapEncrypted(
            BackupCrypto.encrypt("""{"notes":[]}""".encodeToByteArray(), "pw".toCharArray())
        )

        assertTrue(BackupCodec.isEncrypted(encrypted))
        assertFalse(BackupCodec.isEncrypted("""{"notes":[]}""".encodeToByteArray()))
    }

    @Test
    fun encryptedFile_bytesAreNotPlaintextJson() {
        val plain = BackupCodec.encode(sampleEnvelope())

        val encryptedFile = BackupCodec.wrapEncrypted(BackupCrypto.encrypt(plain, "pw".toCharArray()))

        val asText = encryptedFile.decodeToString()
        assertFalse(asText.contains("Note"))
        assertFalse(asText.contains("finotes-backup"))
    }
}
