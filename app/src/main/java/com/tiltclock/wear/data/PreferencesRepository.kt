package com.tiltclock.wear.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.tiltPreferences by preferencesDataStore("tiltclock")

data class TiltPreferences(
    val avatar: String = "holo",
    val scale: Float = 1f,
    val theme: String = "light",
    val language: String = "es",
    val importedModel: String? = null,
)

class PreferencesRepository(private val context: Context) {
    private val avatar = stringPreferencesKey("avatar")
    private val scale = floatPreferencesKey("scale")
    private val theme = stringPreferencesKey("theme")
    private val language = stringPreferencesKey("language")
    private val importedModel = stringPreferencesKey("imported_model")
    val preferences = context.tiltPreferences.data.map {
        TiltPreferences(
            avatar = it[avatar] ?: "holo",
            scale = (it[scale] ?: 1f).takeIf(Float::isFinite)?.coerceIn(.3f, 3f) ?: 1f,
            theme = it[theme]?.takeIf { value -> value in setOf("light", "dark") } ?: "light",
            language = it[language]?.takeIf { value -> value in setOf("es", "en") } ?: "es",
            importedModel = it[importedModel],
        )
    }

    suspend fun setScale(value: Float) {
        require(value.isFinite())
        context.tiltPreferences.edit { it[scale] = value.coerceIn(.3f, 3f) }
    }
    suspend fun setTheme(value: String) {
        require(value == "light" || value == "dark")
        context.tiltPreferences.edit { it[theme] = value }
    }
    suspend fun setLanguage(value: String) {
        require(value == "es" || value == "en")
        context.tiltPreferences.edit { it[language] = value }
    }
    /** Call only after the renderer has successfully accepted the candidate. */
    suspend fun commitAvatar(id: String, privateModelPath: String? = null) {
        require(id in setOf("holo", "fox", "pajaro", "mapache", "moneda", "riot", "custom"))
        require(id != "custom" || !privateModelPath.isNullOrBlank())
        context.tiltPreferences.edit {
            it[avatar] = id
            if (privateModelPath == null) it.remove(importedModel) else it[importedModel] = privateModelPath
        }
    }
}
