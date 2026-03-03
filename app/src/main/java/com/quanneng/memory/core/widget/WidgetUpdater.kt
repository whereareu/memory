package com.quanneng.memory.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.quanneng.memory.Edit
import com.quanneng.memory.R
import com.quanneng.memory.core.datastore.DateCounterPreferences
import com.quanneng.memory.core.datastore.EditPreferences
import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.features.widget.data.WidgetRepository
import com.quanneng.memory.features.widget.widget.WidgetSizeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * 小部件更新器
 * 负责更新小部件的RemoteViews
 * widgetType=3 为日期计数器，其他为文本Widget
 */
class WidgetUpdater(
    private val context: Context,
    internal val repository: WidgetRepository,
    private val dispatchers: DispatcherProvider
) {
    private val widgetSizeProvider = WidgetSizeProvider()
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.main)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    /**
     * 更新指定小部件
     * @param appWidgetId 小部件实例ID
     * @param widgetType 小部件类型 (1, 2, 3)
     */
    fun updateWidget(appWidgetId: Int, widgetType: Int = 1) {
        when (widgetType) {
            3 -> updateDateCounterWidget(appWidgetId)
            else -> updateTextWidget(appWidgetId, widgetType)
        }
    }

    /**
     * 更新文本类型小部件
     */
    private fun updateTextWidget(appWidgetId: Int, widgetType: Int) {
        scope.launch(dispatchers.io) {
            // 使用对应类型的配置
            val editPrefs = EditPreferences(context, widgetType)
            val config = com.quanneng.memory.features.widget.model.WidgetConfig(
                appWidgetId = appWidgetId,
                text = editPrefs.getText(),
                textSize = editPrefs.getTextSize(),
                textColor = editPrefs.getTextColor(),
                backgroundColor = editPrefs.getBackgroundColor()
            )

            // 获取小部件尺寸信息
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)

            // 获取尺寸并选择布局
            val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val layout = widgetSizeProvider.selectLayout(widthDp, heightDp)
            val layoutResId = widgetSizeProvider.getLayoutResId(layout)

            // 创建RemoteViews
            val views = RemoteViews(context.packageName, layoutResId)

            // 设置文本
            views.setTextViewText(R.id.widget_text, config.text)

            // 设置文本大小
            views.setFloat(R.id.widget_text, "setTextSize", config.textSize)

            // 设置文本颜色
            views.setTextColor(R.id.widget_text, config.textColor)

            // 设置背景颜色
            views.setInt(R.id.widget_root, "setBackgroundColor", config.backgroundColor)

            // 设置点击事件 - 跳转到编辑页面，传递widgetType
            val intent = Intent(context, Edit::class.java).apply {
                putExtra("widget_type", widgetType)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // 更新小部件
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    /**
     * 更新日期计数器小部件
     */
    private fun updateDateCounterWidget(appWidgetId: Int) {
        scope.launch(dispatchers.io) {
            // 获取配置 - 优先使用全局配置
            val prefs = DateCounterPreferences(context, dispatchers)
            val instanceConfig = prefs.getConfig(appWidgetId)

            val config = if (instanceConfig != null) {
                instanceConfig
            } else {
                // 使用全局配置
                com.quanneng.memory.features.widget.model.DateCounterConfig(
                    appWidgetId = appWidgetId,
                    title = prefs.getGlobalTitle(),
                    targetDate = prefs.getGlobalTargetDate(),
                    titleSize = prefs.getGlobalTitleSize(),
                    dateSize = prefs.getGlobalDateSize(),
                    daysSize = prefs.getGlobalDaysSize(),
                    titleColor = prefs.getGlobalTitleColor(),
                    dateColor = prefs.getGlobalDateColor(),
                    daysColor = prefs.getGlobalDaysColor(),
                    backgroundColor = prefs.getGlobalBackgroundColor()
                )
            }

            // 计算经过天数
            val elapsedDays = config.calculateElapsedDays()

            // 获取小部件尺寸信息
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)

            // 获取尺寸并选择布局
            val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val layout = widgetSizeProvider.selectLayout(widthDp, heightDp)
            val layoutResId = widgetSizeProvider.getDateCounterLayoutResId(layout)

            // 创建RemoteViews
            val views = RemoteViews(context.packageName, layoutResId)

            // 设置标题
            views.setTextViewText(R.id.date_counter_title, config.title)
            views.setFloat(R.id.date_counter_title, "setTextSize", config.titleSize)
            views.setTextColor(R.id.date_counter_title, config.titleColor)

            // 设置目标日期
            views.setTextViewText(R.id.date_counter_target_date, config.targetDate.format(dateFormatter))
            views.setFloat(R.id.date_counter_target_date, "setTextSize", config.dateSize)
            views.setTextColor(R.id.date_counter_target_date, config.dateColor)

            // 设置经过天数
            val daysText = "已过 $elapsedDays 天"
            views.setTextViewText(R.id.date_counter_elapsed_days, daysText)
            views.setFloat(R.id.date_counter_elapsed_days, "setTextSize", config.daysSize)
            views.setTextColor(R.id.date_counter_elapsed_days, config.daysColor)

            // 设置背景颜色
            views.setInt(R.id.widget_root, "setBackgroundColor", config.backgroundColor)

            // 设置点击事件 - 跳转到编辑页面
            val intent = Intent(context, Edit::class.java).apply {
                putExtra("widget_type", 3)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // 更新小部件
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    /**
     * 更新所有小部件
     */
    fun updateAllWidgets() {
        scope.launch {
            val widgetIds = repository.getAllWidgetIds()
            widgetIds.forEach { id ->
                updateWidget(id)
            }
        }
    }
}
