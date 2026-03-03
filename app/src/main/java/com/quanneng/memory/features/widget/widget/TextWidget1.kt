package com.quanneng.memory.features.widget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.quanneng.memory.MemoryApp
import com.quanneng.memory.core.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 小部件1 - 名言 Widget
 */
class TextWidget1 : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val widgetUpdater = getWidgetUpdater(context)
        appWidgetIds.forEach { appWidgetId ->
            widgetUpdater.updateWidget(appWidgetId, widgetType = 1)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val repository = getWidgetUpdater(context).repository
        appWidgetIds.forEach { appWidgetId ->
            widgetScope.launch {
                repository.deleteConfig(appWidgetId)
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
        widgetUpdater.updateWidget(appWidgetId, widgetType = 1)
    }

    private fun getWidgetUpdater(context: Context): WidgetUpdater {
        val app = context.applicationContext as MemoryApp
        return app.container.widgetUpdater
    }
}