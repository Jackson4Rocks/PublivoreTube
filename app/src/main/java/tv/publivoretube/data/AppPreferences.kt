package tv.publivoretube.data

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var autoplay: Boolean
        get() = prefs.getBoolean(KEY_AUTOPLAY, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTOPLAY, value).apply()

    var captions: Boolean
        get() = prefs.getBoolean(KEY_CAPTIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_CAPTIONS, value).apply()

    var rememberPosition: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_POSITION, true)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_POSITION, value).apply()

    var showThumbnails: Boolean
        get() = prefs.getBoolean(KEY_THUMBNAILS, true)
        set(value) = prefs.edit().putBoolean(KEY_THUMBNAILS, value).apply()

    var reduceAnimations: Boolean
        get() = prefs.getBoolean(KEY_REDUCE_ANIMATIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_REDUCE_ANIMATIONS, value).apply()

    var highContrast: Boolean
        get() = prefs.getBoolean(KEY_HIGH_CONTRAST, false)
        set(value) = prefs.edit().putBoolean(KEY_HIGH_CONTRAST, value).apply()

    var quality: String
        get() = prefs.getString(KEY_QUALITY, "Auto") ?: "Auto"
        set(value) = prefs.edit().putString(KEY_QUALITY, value).apply()

    private companion object {
        const val PREFS = "app_preferences"
        const val KEY_AUTOPLAY = "autoplay"
        const val KEY_CAPTIONS = "captions"
        const val KEY_REMEMBER_POSITION = "remember_position"
        const val KEY_THUMBNAILS = "show_thumbnails"
        const val KEY_REDUCE_ANIMATIONS = "reduce_animations"
        const val KEY_HIGH_CONTRAST = "high_contrast"
        const val KEY_QUALITY = "quality"
    }
}
