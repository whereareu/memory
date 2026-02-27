package com.quanneng.memory.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.quanneng.memory.Edit
import com.quanneng.memory.R
import com.quanneng.memory.core.datastore.EditPreferences
import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.features.widget.data.WidgetRepository
import com.quanneng.memory.features.widget.widget.WidgetSizeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 小部件更新器
 * 负责更新小部件的RemoteViews
 * 优先使用全局编辑配置，如果没有则使用实例配置
 */
class WidgetUpdater(
    private val context: Context,
    internal val repository: WidgetRepository,
    private val dispatchers: DispatcherProvider
) {
    private val widgetSizeProvider = WidgetSizeProvider()
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.main)

    /**
     * 更新指定小部件
     * @param appWidgetId 小部件实例ID
     * @param widgetType 小部件类型 (1, 2, 3)
     */
    fun updateWidget(appWidgetId: Int, widgetType: Int = 1) {
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
