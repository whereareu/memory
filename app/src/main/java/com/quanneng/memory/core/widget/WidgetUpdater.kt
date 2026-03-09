package com.quanneng.memory.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.quanneng.memory.Edit
import com.quanneng.memory.R
import com.quanneng.memory.core.datastore.CountdownPreferences
import com.quanneng.memory.core.datastore.DateCounterPreferences
import com.quanneng.memory.core.datastore.EditPreferences
import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.features.flashthought.ui.FlashThoughtListActivity
import com.quanneng.memory.features.flashthought.ui.FlashThoughtQuickAddActivity
import com.quanneng.memory.features.flashthought.widget.FlashThoughtWidget
import com.quanneng.memory.features.widget.data.WidgetRepository
import com.quanneng.memory.features.widget.widget.TextWidget
import com.quanneng.memory.features.widget.widget.TextWidget1
import com.quanneng.memory.features.widget.widget.TextWidget2
import com.quanneng.memory.features.widget.widget.TextWidget3
import com.quanneng.memory.features.widget.widget.TextWidget4
import com.quanneng.memory.features.widget.widget.WidgetSizeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * 小部件更新器
 * 负责更新小部件的RemoteViews
 * widgetType=3 为日期计数器，widgetType=4 为倒计时
 */
class WidgetUpdater(
    private val context: Context,
    internal val repository: WidgetRepository,
    private val dispatchers: DispatcherProvider
) {
    private val widgetSizeProvider = WidgetSizeProvider()
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.main)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * 更新指定小部件
     * @param appWidgetId 小部件实例ID
     * @param widgetType 小部件类型 (1, 2, 3, 4)
     */
    fun updateWidget(appWidgetId: Int, widgetType: Int = 1) {
        when (widgetType) {
            3 -> updateDateCounterWidget(appWidgetId)
            4 -> updateCountdownWidget(appWidgetId)
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
            views.setFloat(R.id.widget_text, "setTextSize", config.textSize)
            views.setTextColor(R.id.widget_text, config.textColor)
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
     * 更新倒计时小部件
     */
    private fun updateCountdownWidget(appWidgetId: Int) {
        scope.launch(dispatchers.io) {
            // 获取配置 - 优先使用全局配置
            val prefs = CountdownPreferences(context, dispatchers)
            val instanceConfig = prefs.getConfig(appWidgetId)

            val config = if (instanceConfig != null) {
                instanceConfig
            } else {
                // 使用全局配置
                com.quanneng.memory.features.widget.model.CountdownConfig(
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

            // 计算剩余天数
            val remainingDays = config.calculateRemainingDays()

            // 获取小部件尺寸信息
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)

            // 获取尺寸并选择布局
            val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val layout = widgetSizeProvider.selectLayout(widthDp, heightDp)
            val layoutResId = widgetSizeProvider.getCountdownLayoutResId(layout)

            // 创建RemoteViews
            val views = RemoteViews(context.packageName, layoutResId)

            // 设置标题
            views.setTextViewText(R.id.countdown_title, config.title)
            views.setFloat(R.id.countdown_title, "setTextSize", config.titleSize)
            views.setTextColor(R.id.countdown_title, config.titleColor)

            // 设置目标日期
            views.setTextViewText(R.id.countdown_target_date, config.targetDate.format(dateFormatter))
            views.setFloat(R.id.countdown_target_date, "setTextSize", config.dateSize)
            views.setTextColor(R.id.countdown_target_date, config.dateColor)

            // 设置剩余天数
            val daysText = if (remainingDays >= 0) "剩余 $remainingDays 天" else "已过期 ${-remainingDays} 天"
            views.setTextViewText(R.id.countdown_remaining_days, daysText)
            views.setFloat(R.id.countdown_remaining_days, "setTextSize", config.daysSize)
            views.setTextColor(R.id.countdown_remaining_days, config.daysColor)

            // 设置背景颜色
            views.setInt(R.id.widget_root, "setBackgroundColor", config.backgroundColor)

            // 设置点击事件 - 跳转到编辑页面
            val intent = Intent(context, Edit::class.java).apply {
                putExtra("widget_type", 4)
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
     * 更新闪现小部件
     */
    fun updateFlashThoughtWidget(appWidgetId: Int) {
        scope.launch(dispatchers.io) {
            try {
                updateSingleFlashThoughtWidgetSync(appWidgetId)
            } catch (e: Exception) {
                android.util.Log.e("FlashThoughtWidget", "更新Widget失败: $appWidgetId", e)
                // 显示错误信息
                kotlinx.coroutines.withContext(dispatchers.main) {
                    showErrorWidget(appWidgetId, e.message)
                }
            }
        }
    }

    /**
     * 同步更新单个 Widget（内部使用）
     */
    private suspend fun updateSingleFlashThoughtWidgetSync(appWidgetId: Int) {
        android.util.Log.d("FlashThoughtWidget", "开始更新Widget: $appWidgetId")
        // 获取最新闪现（置顶优先，然后按时间倒序）
        val app = context.applicationContext as com.quanneng.memory.MemoryApp
        val latestThought = app.container.flashThoughtRepository.getLatestThought()
        android.util.Log.d("FlashThoughtWidget", "获取闪现结果: ${latestThought?.content ?: "null"}")

        // 切换到主线程更新 UI
        kotlinx.coroutines.withContext(dispatchers.main) {
            updateSingleFlashThoughtWidget(appWidgetId, latestThought)
        }
        android.util.Log.d("FlashThoughtWidget", "Widget更新完成: $appWidgetId")
    }

    /**
     * 更新所有闪现小部件（异步）
     */
    fun updateFlashThoughtWidgets() {
        scope.launch(dispatchers.io) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, com.quanneng.memory.features.flashthought.widget.FlashThoughtWidget::class.java)
            )

            // 获取最新闪现
            val app = context.applicationContext as com.quanneng.memory.MemoryApp
            val latestThought = app.container.flashThoughtRepository.getLatestThought()

            widgetIds.forEach { appWidgetId ->
                // 切换到主线程更新 Widget
                kotlinx.coroutines.withContext(dispatchers.main) {
                    updateSingleFlashThoughtWidget(appWidgetId, latestThought)
                }
            }
        }
    }

    /**
     * 更新单个闪现小部件（内部方法，不使用协程）
     */
    private fun updateSingleFlashThoughtWidget(appWidgetId: Int, latestThought: com.quanneng.memory.features.flashthought.model.FlashThought?) {
        // 获取小部件尺寸信息
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)

        // 获取尺寸并选择布局
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        val layout = widgetSizeProvider.selectLayout(widthDp, heightDp)
        val layoutResId = widgetSizeProvider.getFlashThoughtLayoutResId(layout)

        // 创建RemoteViews
        val views = RemoteViews(context.packageName, layoutResId)

        // 设置内容
        if (latestThought != null) {
            // 设置闪现内容
            views.setTextViewText(R.id.thought_content, latestThought.content)

            // 中等尺寸和大尺寸显示标题
            if (layout == com.quanneng.memory.features.widget.model.WidgetLayout.MEDIUM_4X2 ||
                layout == com.quanneng.memory.features.widget.model.WidgetLayout.LARGE_4X4) {
                val titleText = if (latestThought.isPinned) "置顶闪现" else "今日闪现"
                views.setTextViewText(R.id.widget_title, titleText)
                // 显示置顶标记（小尺寸布局可能没有此 ID）
                runCatching {
                    if (latestThought.isPinned) {
                        views.setViewVisibility(R.id.widget_badge, android.view.View.VISIBLE)
                    } else {
                        views.setViewVisibility(R.id.widget_badge, android.view.View.GONE)
                    }
                }
            }

            // 大尺寸布局显示时间
            if (layout == com.quanneng.memory.features.widget.model.WidgetLayout.LARGE_4X4) {
                // 显示时间
                val timeText = latestThought.createdAt.atZone(java.time.ZoneId.systemDefault())
                    .format(timeFormatter)
                views.setTextViewText(R.id.thought_time, timeText)
            }
        } else {
            // 没有闪现时的提示
            views.setTextViewText(R.id.thought_content, "点击 + 添加你的第一个闪现")

            // 设置默认标题
            if (layout == com.quanneng.memory.features.widget.model.WidgetLayout.MEDIUM_4X2 ||
                layout == com.quanneng.memory.features.widget.model.WidgetLayout.LARGE_4X4) {
                views.setTextViewText(R.id.widget_title, "今日闪现")
                runCatching {
                    views.setViewVisibility(R.id.widget_badge, android.view.View.GONE)
                }
            }

            // 清空时间
            if (layout == com.quanneng.memory.features.widget.model.WidgetLayout.LARGE_4X4) {
                views.setTextViewText(R.id.thought_time, "")
            }
        }

        // 设置背景颜色
        views.setInt(R.id.widget_root, "setBackgroundColor", 0xFF000000.toInt())

        // 设置快速添加按钮点击事件
        val quickAddIntent = Intent(FlashThoughtWidget.ACTION_QUICK_ADD).apply {
            putExtra(FlashThoughtWidget.EXTRA_WIDGET_ID, appWidgetId)
            component = android.content.ComponentName(context, FlashThoughtWidget::class.java)
        }
        val quickAddPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            quickAddIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_quick_add, quickAddPendingIntent)

        // 设置查看列表按钮点击事件
        val listIntent = Intent(FlashThoughtWidget.ACTION_VIEW_LIST).apply {
            component = android.content.ComponentName(context, FlashThoughtWidget::class.java)
        }
        val listPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId + 1,
            listIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_view_list, listPendingIntent)

        // 更新小部件
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * 显示错误状态的Widget
     */
    private fun showErrorWidget(appWidgetId: Int, errorMessage: String?) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val views = RemoteViews(context.packageName, R.layout.widget_flash_thought_small)
        views.setTextViewText(R.id.thought_content, "加载失败，请重试")
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * 更新所有小部件
     */
    fun updateAllWidgets() {
        scope.launch {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            // 更新各类型的小部件
            updateWidgetsByType(appWidgetManager, TextWidget::class.java, 0)
            updateWidgetsByType(appWidgetManager, TextWidget1::class.java, 1)
            updateWidgetsByType(appWidgetManager, TextWidget2::class.java, 2)
            updateWidgetsByType(appWidgetManager, TextWidget3::class.java, 3)
            updateWidgetsByType(appWidgetManager, TextWidget4::class.java, 4)
        }
    }

    /**
     * 更新指定类型的所有小部件
     */
    private fun updateWidgetsByType(
        appWidgetManager: AppWidgetManager,
        widgetClass: Class<*>,
        widgetType: Int
    ) {
        val componentName = android.content.ComponentName(context, widgetClass)
        val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
        widgetIds.forEach { id ->
            updateWidget(id, widgetType)
        }
    }

    /**
     * 更新指定类型的所有小部件
     * @param widgetType 小部件类型 (0, 1, 2, 3, 4)
     */
    fun updateWidgetsByType(widgetType: Int) {
        scope.launch {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetClass = when (widgetType) {
                0 -> TextWidget::class.java
                1 -> TextWidget1::class.java
                2 -> TextWidget2::class.java
                3 -> TextWidget3::class.java
                4 -> TextWidget4::class.java
                else -> return@launch
            }
            updateWidgetsByType(appWidgetManager, widgetClass, widgetType)
        }
    }
}