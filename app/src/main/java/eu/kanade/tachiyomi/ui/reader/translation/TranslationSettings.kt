package eu.kanade.tachiyomi.ui.reader.translation

import android.content.Context

object TranslationSettings {
    private const val PREFS_NAME = "manga_translation_prefs"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_TARGET_LANGUAGE = "gemini_target_language"

    fun getApiKey(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
    }

    fun saveApiKey(context: Context, apiKey: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_GEMINI_API_KEY, apiKey).apply()
    }

    fun getTargetLanguage(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_TARGET_LANGUAGE, "English") ?: "English"
    }

    fun saveTargetLanguage(context: Context, language: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_TARGET_LANGUAGE, language).apply()
    }
}
