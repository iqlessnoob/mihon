package eu.kanade.tachiyomi.data.preference

import android.content.Context

object UpscalePreference {
    private const val PREFS_NAME = "upscale_prefs"
    private const val KEY_AUTO_UPSCALE = "auto_upscale_manga"

    fun isAutoUpscaleEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_UPSCALE, false)
    }

    fun setAutoUpscale(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_UPSCALE, enabled).apply()
    }
}
