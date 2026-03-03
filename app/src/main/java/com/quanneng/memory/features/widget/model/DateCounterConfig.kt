package com.quanneng.memory.features.widget.model

import java.time.LocalDate

/**
 * 日期计数器配置数据模型
 * 支持多实例，每个实例由appWidgetId唯一标识
 */
data class DateCounterConfig(
    val appWidgetId: Int,
    val title: String,
    val targetDate: LocalDate,
    val titleSize: Float,
    val dateSize: Float,
    val daysSize: Float,
    val titleColor: Int,
    val dateColor: Int,
    val daysColor: Int,
    val backgroundColor: Int,
    val width: Int = 0,
    val height: Int = 0
) {
    companion object {
        /**
         * 创建默认配置
         */
        fun createDefault(appWidgetId: Int): DateCounterConfig {
            return DateCounterConfig(
                appWidgetId = appWidgetId,
                title = "开始日期",
                targetDate = LocalDate.now(),
                titleSize = 14f,
                dateSize = 16f,
                daysSize = 24f,
                titleColor = 0xFFFFFFFF.toInt(),
                dateColor = 0xFFFFFFFF.toInt(),
                daysColor = 0xFF4FC3F7.toInt(),
                backgroundColor = 0xDD000000.toInt(),
                width = 0,
                height = 0
            )
        }
    }

    /**
     * 计算从目标日期到今天经过的天数
     * @return 经过天数（负数表示未来）
     */
    fun calculateElapsedDays(): Int {
        val today = LocalDate.now()
        return java.time.temporal.ChronoUnit.DAYS.between(targetDate, today).toInt()
    }
}