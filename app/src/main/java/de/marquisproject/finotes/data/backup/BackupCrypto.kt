package de.marquisproject.finotes.data.backup

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encrypts and decrypts backup payloads with AES-256-GCM using a key derived
 * from a user passphrase with PBKDF2 (310_000 iterations, per current OWASP
 * guidance for PBKDF2-HMAC-SHA256).
 *
 * Wire format: [salt(16)][iv(12)][ciphertext+GCM tag]. The salt and IV are
 * randomly generated per file so identical backups never produce identical
 * ciphertext. GCM provides authenticated encryption: tampering or truncation
 * of the ciphertext is detected during decryption.
 */
object BackupCrypto {

    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12
    private const val KEY_LENGTH_BITS = 256
    private const val PBKDF2_ITERATIONS = 310_000
    private const val GCM_TAG_BITS = 128
    private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"

    fun encrypt(plaintext: ByteArray, passphrase: CharArray): ByteArray {
        require(passphrase.isNotEmpty()) { "Passphrase must not be empty" }
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plaintext)
        return salt + iv + ciphertext
    }

    /**
     * @throws BackupDecryptionException for wrong passphrase, tampering,
     * truncation, or malformed encrypted input.
     */
    fun decrypt(encrypted: ByteArray, passphrase: CharArray): ByteArray {
        require(passphrase.isNotEmpty()) { "Passphrase must not be empty" }
        if (encrypted.size < SALT_LENGTH_BYTES + IV_LENGTH_BYTES + 16) {
            throw BackupDecryptionException("Encrypted backup is too short to be valid")
        }
        val salt = encrypted.copyOfRange(0, SALT_LENGTH_BYTES)
        val iv = encrypted.copyOfRange(SALT_LENGTH_BYTES, SALT_LENGTH_BYTES + IV_LENGTH_BYTES)
        val ciphertext = encrypted.copyOfRange(SALT_LENGTH_BYTES + IV_LENGTH_BYTES, encrypted.size)
        val key = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return try {
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            throw BackupDecryptionException(
                "Decryption failed: wrong passphrase or corrupted backup",
                e
            )
        }
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(keyBytes, "AES")
    }
}

class BackupDecryptionException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
