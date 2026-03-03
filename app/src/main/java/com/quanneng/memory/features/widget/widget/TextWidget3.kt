package com.quanneng.memory.features.widget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.quanneng.memory.MemoryApp
import com.quanneng.memory.core.datastore.DateCounterPreferences
import com.quanneng.memory.core.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 小部件3 - 日期计数器 Widget
 * 显示用户选择的日期以及距离今天已经过的天数
 * 监听日期变化以自动更新显示
 */
class TextWidget3 : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val widgetUpdater = getWidgetUpdater(context)
        appWidgetIds.forEach { appWidgetId ->
            widgetUpdater.updateWidget(appWidgetId, widgetType = 3)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // 监听日期变化和时区变化，自动更新 widget
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, TextWidget3::class.java)
                )
                onUpdate(context, appWidgetManager, widgetIds)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // 删除日期计数器配置
        val dispatchers = com.quanneng.memory.core.dispatchers.DispatcherProvider()
        val prefs = DateCounterPreferences(context, dispatchers)
        appWidgetIds.forEach { appWidgetId ->
            widgetScope.launch {
                prefs.deleteConfig(appWidgetId)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        val widgetUpdater = getWidgetUpdater(context)
        widgetUpdater.updateWidget(appWidgetId, widgetType = 3)
    }

    private fun getWidgetUpdater(context: Context): WidgetUpdater {
        val app = context.applicationContext as MemoryApp
        return app.container.widgetUpdater
    }
}