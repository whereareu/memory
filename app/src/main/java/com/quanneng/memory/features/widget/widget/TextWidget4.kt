package com.quanneng.memory.features.widget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.quanneng.memory.MemoryApp
import com.quanneng.memory.core.datastore.CountdownPreferences
import com.quanneng.memory.core.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 小部件4 - 倒计时 Widget
 * 显示用户选择的目标日期以及距离该日期的剩余天数
 * 监听日期变化以自动更新显示
 */
class TextWidget4 : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val widgetUpdater = getWidgetUpdater(context)
        appWidgetIds.forEach { appWidgetId ->
            widgetUpdater.updateWidget(appWidgetId, widgetType = 4)
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
                    android.content.ComponentName(context, TextWidget4::class.java)
                )
                onUpdate(context, appWidgetManager, widgetIds)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // 删除倒计时配置
        val dispatchers = com.quanneng.memory.core.dispatchers.DispatcherProvider()
        val prefs = CountdownPreferences(context, dispatchers)
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
        widgetUpdater.updateWidget(appWidgetId, widgetType = 4)
    }

    private fun getWidgetUpdater(context: Context): WidgetUpdater {
        val app = context.applicationContext as MemoryApp
        return app.container.widgetUpdater
    }
}