package com.mnmyounus.ymr.util
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher; import javax.crypto.SecretKeyFactory
import javax.crypto.spec.*

object CryptoUtil {
    private const val ALGO = "AES/GCM/NoPadding"
    private const val SALT = "YMR_SALT_MNMYOUNUS_2024"
    fun encrypt(plain: String, pw: String): String {
        val key = deriveKey(pw)
        val c = Cipher.getInstance(ALGO); c.init(Cipher.ENCRYPT_MODE, key)
        val iv = c.iv; val enc = c.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + enc, Base64.NO_WRAP)
    }
    fun decrypt(s: String, pw: String): String? = try {
        val key = deriveKey(pw); val data = Base64.decode(s, Base64.NO_WRAP)
        val c = Cipher.getInstance(ALGO)
        c.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, data.copyOfRange(0,12)))
        String(c.doFinal(data.copyOfRange(12, data.size)), Charsets.UTF_8)
    } catch(e: Exception) { null }
    fun hash(pw: String): String = Base64.encodeToString(
        MessageDigest.getInstance("SHA-256").digest(pw.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    private fun deriveKey(pw: String) = SecretKeySpec(
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(pw.toCharArray(), SALT.toByteArray(), 10000, 256)).encoded, "AES")
}
