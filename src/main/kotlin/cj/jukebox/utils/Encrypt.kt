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
    val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
    val iv = ByteArray(16)
    val charArray = secretKey.toCharArray()
    for (i in charArray.indices) {
        iv[i] = charArray[i].code.toByte()
    }
    val gcmParameterSpec = GCMParameterSpec(128, iv)

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec)

    val encryptedValue = cipher.doFinal(toByteArray())
    return Base64.getEncoder().encodeToString(encryptedValue)
}

/**
 * Permet de déchiffrer une [String]. Utilise le chiffrage AES-GCM.
 */
fun String.decrypt(): String {
    val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
    val iv = ByteArray(16)
    val charArray = secretKey.toCharArray()
    for (i in charArray.indices) {
        iv[i] = charArray[i].code.toByte()
    }
    val gcmParameterSpec = GCMParameterSpec(128, iv)

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec)

    val decryptedByteValue = cipher.doFinal(Base64.getDecoder().decode(this))
    return String(decryptedByteValue)
}
