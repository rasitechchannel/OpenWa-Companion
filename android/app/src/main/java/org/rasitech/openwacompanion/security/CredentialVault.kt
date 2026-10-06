package org.rasitech.openwacompanion.security

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.File

class CredentialVault(context: Context) {
    private val app = context.applicationContext
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs = EncryptedSharedPreferences.create(
        "openwa_secure_prefs",
        masterKeyAlias,
        app,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK, false)
    fun setAppLockEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply()

    fun isUnlockedSession(): Boolean = prefs.getBoolean(KEY_UNLOCKED, false)
    fun setUnlockedSession(value: Boolean) =
        prefs.edit().putBoolean(KEY_UNLOCKED, value).apply()

    fun wrapSensitiveFile(plain: File, encrypted: File) {
        if (!plain.exists()) return
        val enc = EncryptedFile.Builder(
            encrypted,
            app,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
        plain.inputStream().use { input ->
            enc.openFileOutput().use { output -> input.copyTo(output) }
        }
    }

    companion object {
        private const val KEY_APP_LOCK = "app_lock"
        private const val KEY_UNLOCKED = "unlocked_session"
    }
}

object LogRedactor {
    private val patterns = listOf(
        Regex("\"(noiseKey|signedIdentityKey|signedPreKey|advSecretKey|registrationId)\"\\s*:\\s*\"[^\"]*\""),
    )

    fun redact(message: String): String {
        var out = message
        for (p in patterns) {
            out = p.replace(out) { "\"${it.groupValues[1]}\":\"[REDACTED]\"" }
        }
        return out
    }

    fun i(tag: String, message: String) = Log.i(tag, redact(message))
    fun w(tag: String, message: String, t: Throwable? = null) {
        if (t == null) Log.w(tag, redact(message)) else Log.w(tag, redact(message), t)
    }
}
