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

class EditPreferences(private val context: Context, private val widgetType: Int = 1) {

    private object Keys {
        fun textKey(type: Int) = stringPreferencesKey("widget_${type}_text")
        fun textSizeKey(type: Int) = floatPreferencesKey("widget_${type}_text_size")
        fun textColorKey(type: Int) = intPreferencesKey("widget_${type}_text_color")
        fun backgroundColorKey(type: Int) = intPreferencesKey("widget_${type}_background_color")
    }

    private val textKey = Keys.textKey(widgetType)
    private val textSizeKey = Keys.textSizeKey(widgetType)
    private val textColorKey = Keys.textColorKey(widgetType)
    private val backgroundColorKey = Keys.backgroundColorKey(widgetType)

    // 默认值
    private val defaults = getDefaultValues(widgetType)

    suspend fun getText(): String {
        return context.editDataStore.data.map { it[textKey] ?: defaults.text }.first()
    }

    suspend fun saveText(text: String) {
        context.editDataStore.edit { it[textKey] = text }
    }

    suspend fun getTextSize(): Float {
        return context.editDataStore.data.map { it[textSizeKey] ?: defaults.textSize }.first()
    }

    suspend fun saveTextSize(size: Float) {
        context.editDataStore.edit { it[textSizeKey] = size }
    }

    suspend fun getTextColor(): Int {
        return context.editDataStore.data.map { it[textColorKey] ?: defaults.textColor }.first()
    }

    suspend fun saveTextColor(color: Int) {
        context.editDataStore.edit { it[textColorKey] = color }
    }

    suspend fun getBackgroundColor(): Int {
        return context.editDataStore.data.map { it[backgroundColorKey] ?: defaults.backgroundColor }.first()
    }

    suspend fun saveBackgroundColor(color: Int) {
        context.editDataStore.edit { it[backgroundColorKey] = color }
    }

    companion object {
        data class DefaultValues(
            val text: String,
            val textSize: Float,
            val textColor: Int,
            val backgroundColor: Int
        )

        fun getDefaultValues(widgetType: Int): DefaultValues {
            return when (widgetType) {
                0 -> DefaultValues(
                    text = "通用 Widget - 自定义你的内容",
                    textSize = 16f,
                    textColor = 0xFFFFFFFF.toInt(),
                    backgroundColor = 0xDD000000.toInt()
                )
                1 -> DefaultValues(
                    text = "质量扭曲时空：物体无受力沿着弯曲时空滑落，滑落的轨迹为最短距离，即测地线(geodesic)，过程叫做测地线运动",
                    textSize = 14f,
                    textColor = 0xFFFFFFFF.toInt(),
                    backgroundColor = 0xDD000000.toInt()
                )
                2 -> DefaultValues(
                    text = "名言 Widget - 记录你的灵感",
                    textSize = 16f,
                    textColor = 0xFFFFFFFF.toInt(),
                    backgroundColor = 0xDD1976D2.toInt()
                )
                3 -> DefaultValues(
                    text = "日期计数器 Widget",
                    textSize = 15f,
                    textColor = 0xFF000000.toInt(),
                    backgroundColor = 0xDDFFEB3B.toInt()
                )
                else -> DefaultValues(
                    text = "",
                    textSize = 16f,
                    textColor = 0xFFFFFFFF.toInt(),
                    backgroundColor = 0xDD000000.toInt()
                )
            }
        }
    }
}