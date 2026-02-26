package com.quanneng.memory.features.widget.widget

import android.os.Bundle
import com.quanneng.memory.R
import com.quanneng.memory.features.widget.model.WidgetLayout

/**
 * 小部件尺寸提供者
 * 根据小部件尺寸选择合适的布局
 */
class WidgetSizeProvider {

    companion object {
        // 尺寸阈值（单位：dp）
        private const val SMALL_SIZE_MAX_DP = 110
        private const val MEDIUM_WIDTH_MIN_DP = 110
        private const val MEDIUM_HEIGHT_MIN_DP = 110
        private const val LARGE_WIDTH_MIN_DP = 200
        private const val LARGE_HEIGHT_MIN_DP = 200
    }

    /**
     * 选择布局
     * @param widthDp 宽度（dp）
     * @param heightDp 高度（dp）
     * @return 对应的布局类型
     */
    fun selectLayout(widthDp: Int, heightDp: Int): WidgetLayout {
        return when {
            // 大尺寸布局 (4x4)
            widthDp >= LARGE_WIDTH_MIN_DP && heightDp >= LARGE_HEIGHT_MIN_DP -> {
                WidgetLayout.LARGE_4X4
            }
            // 中等尺寸布局 (4x2 或 2x4)
            widthDp >= MEDIUM_WIDTH_MIN_DP || heightDp >= MEDIUM_HEIGHT_MIN_DP -> {
                WidgetLayout.MEDIUM_4X2
            }
            // 小尺寸布局 (2x2)
            else -> {
                WidgetLayout.SMALL_2X2
            }
        }
    }

    /**
     * 获取布局资源ID
     * @param layout 布局类型
     * @return 布局资源ID
     */
    fun getLayoutResId(layout: WidgetLayout): Int {
        return when (layout) {
            WidgetLayout.SMALL_2X2 -> R.layout.widget_text_small
            WidgetLayout.MEDIUM_4X2 -> R.layout.widget_text_medium
            WidgetLayout.LARGE_4X4 -> R.layout.widget_text_large
        }
    }
}
