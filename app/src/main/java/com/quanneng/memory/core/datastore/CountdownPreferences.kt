package com.quanneng.memory.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.features.widget.model.CountdownConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.countdownDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "countdown_prefs"
)

/**
 * 倒计时配置数据存储
 * 使用DataStore进行持久化缓存
 */
class CountdownPreferences(
    private val context: Context,
    private val dispatchers: DispatcherProvider
) {

    private object Keys {
        // 全局配置键
        const val GLOBAL_TITLE = "countdown_global_title"
        const val GLOBAL_TARGET_DATE = "countdown_global_target_date"
        const val GLOBAL_TITLE_SIZE = "countdown_global_title_size"
        const val GLOBAL_DATE_SIZE = "countdown_global_date_size"
        const val GLOBAL_DAYS_SIZE = "countdown_global_days_size"
        const val GLOBAL_TITLE_COLOR = "countdown_global_title_color"
        const val GLOBAL_DATE_COLOR = "countdown_global_date_color"
        const val GLOBAL_DAYS_COLOR = "countdown_global_days_color"
        const val GLOBAL_BG_COLOR = "countdown_global_bg_color"

        // 实例配置键
        const val PREFIX_TITLE = "countdown_title_"
        const val PREFIX_TARGET_DATE = "countdown_target_date_"
        const val PREFIX_TITLE_SIZE = "countdown_title_size_"
        const val PREFIX_DATE_SIZE = "countdown_date_size_"
        const val PREFIX_DAYS_SIZE = "countdown_days_size_"
        const val PREFIX_TITLE_COLOR = "countdown_title_color_"
        const val PREFIX_DATE_COLOR = "countdown_date_color_"
        const val PREFIX_DAYS_COLOR = "countdown_days_color_"
        const val PREFIX_BG_COLOR = "countdown_bg_color_"
        const val PREFIX_WIDTH = "countdown_width_"
        const val PREFIX_HEIGHT = "countdown_height_"
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * 保存配置
     */
    suspend fun saveConfig(appWidgetId: Int, config: CountdownConfig) {
        context.countdownDataStore.edit { preferences ->
            preferences[stringPreferencesKey(Keys.PREFIX_TITLE + appWidgetId)] = config.title
            preferences[stringPreferencesKey(Keys.PREFIX_TARGET_DATE + appWidgetId)] =
                config.targetDate.format(dateFormatter)
            preferences[floatPreferencesKey(Keys.PREFIX_TITLE_SIZE + appWidgetId)] = config.titleSize
            preferences[floatPreferencesKey(Keys.PREFIX_DATE_SIZE + appWidgetId)] = config.dateSize
            preferences[floatPreferencesKey(Keys.PREFIX_DAYS_SIZE + appWidgetId)] = config.daysSize
            preferences[intPreferencesKey(Keys.PREFIX_TITLE_COLOR + appWidgetId)] = config.titleColor
            preferences[intPreferencesKey(Keys.PREFIX_DATE_COLOR + appWidgetId)] = config.dateColor
            preferences[intPreferencesKey(Keys.PREFIX_DAYS_COLOR + appWidgetId)] = config.daysColor
            preferences[intPreferencesKey(Keys.PREFIX_BG_COLOR + appWidgetId)] = config.backgroundColor
            preferences[intPreferencesKey(Keys.PREFIX_WIDTH + appWidgetId)] = config.width
            preferences[intPreferencesKey(Keys.PREFIX_HEIGHT + appWidgetId)] = config.height
        }
    }

    /**
     * 获取配置
     */
    suspend fun getConfig(appWidgetId: Int): CountdownConfig? {
        return context.countdownDataStore.data.map { preferences ->
            val title = preferences[stringPreferencesKey(Keys.PREFIX_TITLE + appWidgetId)]
            if (title != null) {
                val targetDateString = preferences[stringPreferencesKey(Keys.PREFIX_TARGET_DATE + appWidgetId)]
                val targetDate = if (targetDateString != null) {
                    try {
                        LocalDate.parse(targetDateString, dateFormatter)
                    } catch (e: Exception) {
                        LocalDate.now().plusDays(30)
                    }
                } else {
                    LocalDate.now().plusDays(30)
                }
                CountdownConfig(
                    appWidgetId = appWidgetId,
                    title = title,
                    targetDate = targetDate,
                    titleSize = preferences[floatPreferencesKey(Keys.PREFIX_TITLE_SIZE + appWidgetId)]
                        ?: 14f,
                    dateSize = preferences[floatPreferencesKey(Keys.PREFIX_DATE_SIZE + appWidgetId)]
                        ?: 16f,
                    daysSize = preferences[floatPreferencesKey(Keys.PREFIX_DAYS_SIZE + appWidgetId)]
                        ?: 24f,
                    titleColor = preferences[intPreferencesKey(Keys.PREFIX_TITLE_COLOR + appWidgetId)]
                        ?: 0xFFFFFFFF.toInt(),
                    dateColor = preferences[intPreferencesKey(Keys.PREFIX_DATE_COLOR + appWidgetId)]
                        ?: 0xFFFFFFFF.toInt(),
                    daysColor = preferences[intPreferencesKey(Keys.PREFIX_DAYS_COLOR + appWidgetId)]
                        ?: 0xFFFF5722.toInt(),
                    backgroundColor = preferences[intPreferencesKey(Keys.PREFIX_BG_COLOR + appWidgetId)]
                        ?: 0xDD000000.toInt(),
                    width = preferences[intPreferencesKey(Keys.PREFIX_WIDTH + appWidgetId)] ?: 0,
                    height = preferences[intPreferencesKey(Keys.PREFIX_HEIGHT + appWidgetId)] ?: 0
                )
            } else {
                null
            }
        }.first()
    }

    /**
     * 删除配置
     */
    suspend fun deleteConfig(appWidgetId: Int) {
        context.countdownDataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(Keys.PREFIX_TITLE + appWidgetId))
            preferences.remove(stringPreferencesKey(Keys.PREFIX_TARGET_DATE + appWidgetId))
            preferences.remove(floatPreferencesKey(Keys.PREFIX_TITLE_SIZE + appWidgetId))
            preferences.remove(floatPreferencesKey(Keys.PREFIX_DATE_SIZE + appWidgetId))
            preferences.remove(floatPreferencesKey(Keys.PREFIX_DAYS_SIZE + appWidgetId))
            preferences.remove(intPreferencesKey(Keys.PREFIX_TITLE_COLOR + appWidgetId))
            preferences.remove(intPreferencesKey(Keys.PREFIX_DATE_COLOR + appWidgetId))
            preferences.remove(intPreferencesKey(Keys.PREFIX_DAYS_COLOR + appWidgetId))
            preferences.remove(intPreferencesKey(Keys.PREFIX_BG_COLOR + appWidgetId))
            preferences.remove(intPreferencesKey(Keys.PREFIX_WIDTH + appWidgetId))
            preferences.remove(intPreferencesKey(Keys.PREFIX_HEIGHT + appWidgetId))
        }
    }

    // ========== 全局配置方法 ==========

    suspend fun getGlobalTitle(): String {
        return context.countdownDataStore.data.map {
            it[stringPreferencesKey(Keys.GLOBAL_TITLE)] ?: "目标日期"
        }.first()
    }

    suspend fun saveGlobalTitle(title: String) {
        context.countdownDataStore.edit {
            it[stringPreferencesKey(Keys.GLOBAL_TITLE)] = title
        }
    }

    suspend fun getGlobalTargetDate(): LocalDate {
        return context.countdownDataStore.data.map {
            val dateStr = it[stringPreferencesKey(Keys.GLOBAL_TARGET_DATE)]
            if (dateStr != null) {
                try {
                    LocalDate.parse(dateStr, dateFormatter)
                } catch (e: Exception) {
                    LocalDate.now().plusDays(30)
                }
            } else {
                LocalDate.now().plusDays(30)
            }
        }.first()
    }

    suspend fun saveGlobalTargetDate(date: LocalDate) {
        context.countdownDataStore.edit {
            it[stringPreferencesKey(Keys.GLOBAL_TARGET_DATE)] = date.format(dateFormatter)
        }
    }

    suspend fun getGlobalTitleSize(): Float {
        return context.countdownDataStore.data.map {
            it[floatPreferencesKey(Keys.GLOBAL_TITLE_SIZE)] ?: 14f
        }.first()
    }

    suspend fun saveGlobalTitleSize(size: Float) {
        context.countdownDataStore.edit {
            it[floatPreferencesKey(Keys.GLOBAL_TITLE_SIZE)] = size
        }
    }

    suspend fun getGlobalDateSize(): Float {
        return context.countdownDataStore.data.map {
            it[floatPreferencesKey(Keys.GLOBAL_DATE_SIZE)] ?: 16f
        }.first()
    }

    suspend fun saveGlobalDateSize(size: Float) {
        context.countdownDataStore.edit {
            it[floatPreferencesKey(Keys.GLOBAL_DATE_SIZE)] = size
        }
    }

    suspend fun getGlobalDaysSize(): Float {
        return context.countdownDataStore.data.map {
            it[floatPreferencesKey(Keys.GLOBAL_DAYS_SIZE)] ?: 24f
        }.first()
    }

    suspend fun saveGlobalDaysSize(size: Float) {
        context.countdownDataStore.edit {
            it[floatPreferencesKey(Keys.GLOBAL_DAYS_SIZE)] = size
        }
    }

    suspend fun getGlobalTitleColor(): Int {
        return context.countdownDataStore.data.map {
            it[intPreferencesKey(Keys.GLOBAL_TITLE_COLOR)] ?: 0xFFFFFFFF.toInt()
        }.first()
    }

    suspend fun saveGlobalTitleColor(color: Int) {
        context.countdownDataStore.edit {
            it[intPreferencesKey(Keys.GLOBAL_TITLE_COLOR)] = color
        }
    }

    suspend fun getGlobalDateColor(): Int {
        return context.countdownDataStore.data.map {
            it[intPreferencesKey(Keys.GLOBAL_DATE_COLOR)] ?: 0xFFFFFFFF.toInt()
        }.first()
    }

    suspend fun saveGlobalDateColor(color: Int) {
        context.countdownDataStore.edit {
            it[intPreferencesKey(Keys.GLOBAL_DATE_COLOR)] = color
        }
    }

    suspend fun getGlobalDaysColor(): Int {
        return context.countdownDataStore.data.map {
            it[intPreferencesKey(Keys.GLOBAL_DAYS_COLOR)] ?: 0xFFFF5722.toInt()
        }.first()
    }

    suspend fun saveGlobalDaysColor(color: Int) {
        context.countdownDataStore.edit {
            it[intPreferencesKey(Keys.GLOBAL_DAYS_COLOR)] = color
        }
    }

    suspend fun getGlobalBackgroundColor(): Int {
        return context.countdownDataStore.data.map {
            it[intPreferencesKey(Keys.GLOBAL_BG_COLOR)] ?: 0xDD000000.toInt()
        }.first()
    }

    suspend fun saveGlobalBackgroundColor(color: Int) {
        context.countdownDataStore.edit {
            it[intPreferencesKey(Keys.GLOBAL_BG_COLOR)] = color
        }
    }
}