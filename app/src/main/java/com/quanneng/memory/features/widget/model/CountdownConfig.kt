package com.quanneng.memory.features.widget.model

import java.time.LocalDate

/**
 * 倒计时配置数据模型
 * 支持多实例，每个实例由appWidgetId唯一标识
 */
data class CountdownConfig(
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
        fun createDefault(appWidgetId: Int): CountdownConfig {
            return CountdownConfig(
                appWidgetId = appWidgetId,
                title = "目标日期",
                targetDate = LocalDate.now().plusDays(30),
                titleSize = 14f,
                dateSize = 16f,
                daysSize = 24f,
                titleColor = 0xFFFFFFFF.toInt(),
                dateColor = 0xFFFFFFFF.toInt(),
                daysColor = 0xFFFF5722.toInt(),
                backgroundColor = 0xDD000000.toInt(),
                width = 0,
                height = 0
            )
        }
    }

    /**
     * 计算从今天到目标日期的剩余天数
     * @return 剩余天数（负数表示已过期）
     */
    fun calculateRemainingDays(): Int {
        val today = LocalDate.now()
        return java.time.temporal.ChronoUnit.DAYS.between(today, targetDate).toInt()
    }
}