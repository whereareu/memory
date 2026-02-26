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
 * 文本小部件Provider
 * 处理小部件的生命周期事件
 */
class TextWidget : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 小部件更新时调用
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val widgetUpdater = getWidgetUpdater(context)

        // 更新每个小部件实例
        appWidgetIds.forEach { appWidgetId ->
            widgetUpdater.updateWidget(appWidgetId)
        }
    }

    /**
     * 小部件被删除时调用
     * 清理该实例的配置数据
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val repository = getWidgetUpdater(context).repository

        // 清理已删除小部件的配置
        appWidgetIds.forEach { appWidgetId ->
            widgetScope.launch {
                repository.deleteConfig(appWidgetId)
            }
        }
    }

    /**
     * 小部件尺寸变化时调用
     * 重新计算布局并更新
     */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        val widgetUpdater = getWidgetUpdater(context)
        widgetUpdater.updateWidget(appWidgetId)
    }

    /**
     * 获取WidgetUpdater实例
     */
    private fun getWidgetUpdater(context: Context): WidgetUpdater {
        val app = context.applicationContext as MemoryApp
        return app.container.widgetUpdater
    }
}
