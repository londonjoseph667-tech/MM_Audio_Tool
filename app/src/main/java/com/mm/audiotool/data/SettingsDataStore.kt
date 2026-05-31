package com.mm.audiotool.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property — one DataStore instance per process
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mm_audio_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val THEME_MODE_KEY    = stringPreferencesKey("theme_mode")
        val STORAGE_TYPE_KEY  = stringPreferencesKey("storage_type")

        const val THEME_AUTO   = "Auto"
        const val THEME_DARK   = "Dark"
        const val THEME_WHITE  = "White"

        const val STORAGE_INTERNAL = "Internal"
        const val STORAGE_EXTERNAL = "External"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY] ?: THEME_AUTO
    }

    val storageType: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[STORAGE_TYPE_KEY] ?: STORAGE_INTERNAL
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[THEME_MODE_KEY] = mode }
    }

    suspend fun setStorageType(type: String) {
        context.dataStore.edit { prefs -> prefs[STORAGE_TYPE_KEY] = type }
    }
}
