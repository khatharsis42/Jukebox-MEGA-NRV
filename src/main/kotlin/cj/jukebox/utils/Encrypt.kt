package cj.jukebox.utils

import cj.jukebox.config
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

val secretKey = config.data.SECRET_PASSWORD_ENCRYPT

/**
 * Permet de chiffrer une [String]. Utilise le chiffrage AES-GCM.
 */
fun String.encrypt(): String {
    val encryptedValue = createCipherFromSecret(true, toByteArray())

    return Base64.getEncoder().encodeToString(encryptedValue)
}

/**
 * Permet de déchiffrer une [String]. Utilise le chiffrage AES-GCM.
 */
fun String.decrypt(): String {
    val decryptedValue = createCipherFromSecret(false, Base64.getDecoder().decode(this))

    return String(decryptedValue)
}

private fun createCipherFromSecret(encrypt: Boolean, value: ByteArray): ByteArray {
    val secretBuffer = secretKey.toByteArray()
    val secretKeySpec = SecretKeySpec(secretBuffer, "AES")
    val gcmParameterSpec = GCMParameterSpec(128, secretBuffer)

    val cipherMode = if (encrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(cipherMode, secretKeySpec, gcmParameterSpec)

    return cipher.doFinal(value)
}
