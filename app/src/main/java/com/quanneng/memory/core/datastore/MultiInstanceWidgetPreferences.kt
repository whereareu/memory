package com.quanneng.memory.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.features.widget.data.WidgetDataSource
import com.quanneng.memory.features.widget.model.WidgetConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * 基于DataStore的多实例小部件配置存储实现
 * 每个小部件实例由appWidgetId唯一标识
 */
class MultiInstanceWidgetPreferences(
    private val dataStore: DataStore<Preferences>,
    private val dispatchers: DispatcherProvider
) : WidgetDataSource {

    companion object {
        private const val PREFIX_TEXT = "widget_text_"
        private const val PREFIX_TEXT_SIZE = "widget_text_size_"
        private const val PREFIX_TEXT_COLOR = "widget_text_color_"
        private const val PREFIX_BG_COLOR = "widget_bg_color_"
        private const val PREFIX_WIDTH = "widget_width_"
        private const val PREFIX_HEIGHT = "widget_height_"
    }

    /**
     * 将float转换为String进行存储
     */
    private fun floatToString(value: Float): String = value.toString()
    private fun stringToFloat(value: String?): Float = value?.toFloatOrNull() ?: 16f

    override suspend fun saveConfig(appWidgetId: Int, config: WidgetConfig) {
        withContext(dispatchers.io) {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(PREFIX_TEXT + appWidgetId)] = config.text
                preferences[stringPreferencesKey(PREFIX_TEXT_SIZE + appWidgetId)] = floatToString(config.textSize)
                preferences[intPreferencesKey(PREFIX_TEXT_COLOR + appWidgetId)] = config.textColor
                preferences[intPreferencesKey(PREFIX_BG_COLOR + appWidgetId)] = config.backgroundColor
                preferences[intPreferencesKey(PREFIX_WIDTH + appWidgetId)] = config.width
                preferences[intPreferencesKey(PREFIX_HEIGHT + appWidgetId)] = config.height
            }
        }
    }

    override suspend fun getConfig(appWidgetId: Int): WidgetConfig? {
        return withContext(dispatchers.io) {
            dataStore.data.catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                val text = preferences[stringPreferencesKey(PREFIX_TEXT + appWidgetId)]
                if (text != null) {
                    WidgetConfig(
                        appWidgetId = appWidgetId,
                        text = text,
                        textSize = stringToFloat(preferences[stringPreferencesKey(PREFIX_TEXT_SIZE + appWidgetId)]),
                        textColor = preferences[intPreferencesKey(PREFIX_TEXT_COLOR + appWidgetId)] ?: 0xFF212121.toInt(),
                        backgroundColor = preferences[intPreferencesKey(PREFIX_BG_COLOR + appWidgetId)] ?: 0xFFFFFFFF.toInt(),
                        width = preferences[intPreferencesKey(PREFIX_WIDTH + appWidgetId)] ?: 0,
                        height = preferences[intPreferencesKey(PREFIX_HEIGHT + appWidgetId)] ?: 0
                    )
                } else {
                    null
                }
            }.first()
        }
    }

    override fun observeConfig(appWidgetId: Int): Flow<WidgetConfig?> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val text = preferences[stringPreferencesKey(PREFIX_TEXT + appWidgetId)]
            if (text != null) {
                WidgetConfig(
                    appWidgetId = appWidgetId,
                    text = text,
                    textSize = stringToFloat(preferences[stringPreferencesKey(PREFIX_TEXT_SIZE + appWidgetId)]),
                    textColor = preferences[intPreferencesKey(PREFIX_TEXT_COLOR + appWidgetId)] ?: 0xFF212121.toInt(),
                    backgroundColor = preferences[intPreferencesKey(PREFIX_BG_COLOR + appWidgetId)] ?: 0xFFFFFFFF.toInt(),
                    width = preferences[intPreferencesKey(PREFIX_WIDTH + appWidgetId)] ?: 0,
                    height = preferences[intPreferencesKey(PREFIX_HEIGHT + appWidgetId)] ?: 0
                )
            } else {
                null
            }
        }
    }

    override suspend fun getAllWidgetIds(): Set<Int> {
        return withContext(dispatchers.io) {
            dataStore.data.catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences.asMap()
                    .keys
                    .filterIsInstance<Preferences.Key<String>>()
                    .filter { it.name.startsWith(PREFIX_TEXT) }
                    .mapNotNull { key ->
                        key.name.removePrefix(PREFIX_TEXT).toIntOrNull()
                    }
                    .toSet()
            }.first()
        }
    }

    override suspend fun deleteConfig(appWidgetId: Int) {
        withContext(dispatchers.io) {
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey(PREFIX_TEXT + appWidgetId))
                preferences.remove(stringPreferencesKey(PREFIX_TEXT_SIZE + appWidgetId))
                preferences.remove(intPreferencesKey(PREFIX_TEXT_COLOR + appWidgetId))
                preferences.remove(intPreferencesKey(PREFIX_BG_COLOR + appWidgetId))
                preferences.remove(intPreferencesKey(PREFIX_WIDTH + appWidgetId))
                preferences.remove(intPreferencesKey(PREFIX_HEIGHT + appWidgetId))
            }
        }
    }
}
