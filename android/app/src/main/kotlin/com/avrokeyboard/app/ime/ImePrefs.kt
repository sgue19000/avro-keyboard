package com.avrokeyboard.app.ime

import android.content.Context

/** Mode only. Never stores typed text. */
class ImePrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun loadMode(): KeyboardLanguage {
        return when (prefs.getString(KEY_MODE, ENGLISH)) {
            BANGLA -> KeyboardLanguage.BANGLA
            AVRO -> KeyboardLanguage.AVRO
            else -> KeyboardLanguage.ENGLISH
        }
    }

    fun saveMode(mode: KeyboardLanguage) {
        val value = when (mode) {
            KeyboardLanguage.ENGLISH -> ENGLISH
            KeyboardLanguage.BANGLA -> BANGLA
            KeyboardLanguage.AVRO -> AVRO
        }
        prefs.edit().putString(KEY_MODE, value).apply()
    }

    companion object {
        private const val NAME = "avro_keyboard_ime"
        private const val KEY_MODE = "mode"
        private const val ENGLISH = "en"
        private const val BANGLA = "bn"
        private const val AVRO = "avro"
    }
}
