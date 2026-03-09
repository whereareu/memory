package com.quanneng.memory.features.flashthought.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.quanneng.memory.MemoryApp
import com.quanneng.memory.R
import com.quanneng.memory.core.widget.WidgetUpdater
import com.quanneng.memory.features.flashthought.ui.FlashThoughtListActivity
import com.quanneng.memory.features.flashthought.ui.FlashThoughtQuickAddActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * 闪现 Widget
 * 显示最新的闪现，支持快速添加和查看列表
 */
class FlashThoughtWidget : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val widgetUpdater = getWidgetUpdater(context)
        appWidgetIds.forEach { appWidgetId ->
            widgetUpdater.updateFlashThoughtWidget(appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_QUICK_ADD -> {
                // 启动快速添加界面
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0)
                val quickAddIntent = Intent(context, FlashThoughtQuickAddActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_WIDGET_ID, widgetId)
                }
                context.startActivity(quickAddIntent)
            }
            ACTION_VIEW_LIST -> {
                // 启动列表界面
                val listIntent = Intent(context, FlashThoughtListActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(listIntent)
            }
            ACTION_REFRESH -> {
                // 刷新 Widget 显示
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, FlashThoughtWidget::class.java)
                )
                onUpdate(context, appWidgetManager, widgetIds)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // 闪现 Widget 不需要清理配置，数据存储在数据库中
        super.onDeleted(context, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        val widgetUpdater = getWidgetUpdater(context)
        widgetUpdater.updateFlashThoughtWidget(appWidgetId)
    }

    private fun getWidgetUpdater(context: Context): WidgetUpdater {
        val app = context.applicationContext as MemoryApp
        return app.container.widgetUpdater
    }

    companion object {
        const val ACTION_QUICK_ADD = "com.quanneng.memory.FLASH_THOUGHT_QUICK_ADD"
        const val ACTION_VIEW_LIST = "com.quanneng.memory.FLASH_THOUGHT_VIEW_LIST"
        const val ACTION_REFRESH = "com.quanneng.memory.FLASH_THOUGHT_REFRESH"
        const val EXTRA_WIDGET_ID = "widget_id"
    }
}
