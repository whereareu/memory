package com.quanneng.memory.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.editDataStore: DataStore<Preferences> by preferencesDataStore(name = "edit_prefs")

class EditPreferences(private val context: Context) {

    private object Keys {
        val TEXT = stringPreferencesKey("edit_text")
        val TEXT_SIZE = floatPreferencesKey("edit_text_size")
        val TEXT_COLOR = intPreferencesKey("edit_text_color")
        val BACKGROUND_COLOR = intPreferencesKey("edit_background_color")
    }

    suspend fun getText(): String {
        return context.editDataStore.data.map { it[Keys.TEXT] ?: "" }.first()
    }

    suspend fun setText(text: String) {
        context.editDataStore.edit { it[Keys.TEXT] = text }
    }

    suspend fun saveText(text: String) {
        context.editDataStore.edit { it[Keys.TEXT] = text }
    }

    suspend fun getTextSize(): Float {
        return context.editDataStore.data.map { it[Keys.TEXT_SIZE] ?: 16f }.first()
    }

    suspend fun saveTextSize(size: Float) {
        context.editDataStore.edit { it[Keys.TEXT_SIZE] = size }
    }

    suspend fun getTextColor(): Int {
        return context.editDataStore.data.map { it[Keys.TEXT_COLOR] ?: 0xFF212121.toInt() }.first()
    }

    suspend fun saveTextColor(color: Int) {
        context.editDataStore.edit { it[Keys.TEXT_COLOR] = color }
    }

    suspend fun getBackgroundColor(): Int {
        return context.editDataStore.data.map { it[Keys.BACKGROUND_COLOR] ?: 0xFFFFFFFF.toInt() }.first()
    }

    suspend fun saveBackgroundColor(color: Int) {
        context.editDataStore.edit { it[Keys.BACKGROUND_COLOR] = color }
    }
}