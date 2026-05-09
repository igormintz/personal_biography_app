package com.personalbiography.data.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted preference store for the OpenAI API key + per-user model
 * preferences. Backed by Jetpack Security's [EncryptedSharedPreferences],
 * which encrypts the values at rest using a key from the Android Keystore.
 *
 * The key never leaves the device — there's no backup, no sync, no
 * extraction without root + keystore compromise.
 */
class SecureSettings(context: Context) {
    private val prefs: SharedPreferences = build(context)

    var openAiApiKey: String?
        get() = prefs.getString(KEY_API_KEY, null)?.takeIf { it.isNotBlank() }
        set(value) =
            prefs.edit().run {
                if (value.isNullOrBlank()) remove(KEY_API_KEY) else putString(KEY_API_KEY, value.trim())
                apply()
            }

    var chatModel: String
        get() = prefs.getString(KEY_CHAT_MODEL, DEFAULT_CHAT_MODEL) ?: DEFAULT_CHAT_MODEL
        set(value) = prefs.edit().putString(KEY_CHAT_MODEL, value).apply()

    var transcribeModel: String
        get() = prefs.getString(KEY_TRANSCRIBE_MODEL, DEFAULT_TRANSCRIBE_MODEL) ?: DEFAULT_TRANSCRIBE_MODEL
        set(value) = prefs.edit().putString(KEY_TRANSCRIBE_MODEL, value).apply()

    var transcribeLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    fun hasApiKey(): Boolean = !openAiApiKey.isNullOrBlank()

    companion object {
        const val DEFAULT_CHAT_MODEL = "gpt-4o-mini"
        const val DEFAULT_TRANSCRIBE_MODEL = "whisper-1"
        const val DEFAULT_LANGUAGE = "he"

        private const val KEY_API_KEY = "openai_api_key"
        private const val KEY_CHAT_MODEL = "chat_model"
        private const val KEY_TRANSCRIBE_MODEL = "transcribe_model"
        private const val KEY_LANGUAGE = "language"

        private const val PREFS_NAME = "secure_settings"

        private fun build(context: Context): SharedPreferences {
            val masterKey =
                MasterKey.Builder(context.applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            return EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }
    }
}
