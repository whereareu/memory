package com.quanneng.memory.features.widget.model

/**
 * 小部件配置数据模型
 * 支持多实例，每个实例由appWidgetId唯一标识
 */
data class WidgetConfig(
    val appWidgetId: Int,
    val text: String,
    val textSize: Float,
    val textColor: Int,
    val backgroundColor: Int,
    val width: Int = 0,
    val height: Int = 0
) {
    companion object {
        /**
         * 创建默认配置
         */
        fun createDefault(appWidgetId: Int): WidgetConfig {
            return WidgetConfig(
                appWidgetId = appWidgetId,
                text = "物体无受力沿着弯曲时空滑落，滑落的轨迹为最短距离，即测地线(geodesic)，过程叫做测地线运动",
                textSize = 14f,
                textColor = 0xFFFFFFFF.toInt(), // 白色
                backgroundColor = 0xDD000000.toInt(), // 黑色 87% 不透明
                width = 0,
                height = 0
            )
        }
    }
}

/**
 * 小部件布局类型
 */
enum class WidgetLayout(val layoutId: Int) {
    SMALL_2X2(0),
    MEDIUM_4X2(1),
    LARGE_4X4(2)
}
