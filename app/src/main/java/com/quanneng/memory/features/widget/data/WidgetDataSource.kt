package com.quanneng.memory.features.widget.data

import com.quanneng.memory.features.widget.model.WidgetConfig
import kotlinx.coroutines.flow.Flow

/**
 * 小部件数据源接口
 * 定义数据持久化操作
 */
interface WidgetDataSource {
    /**
     * 保存配置
     * @param appWidgetId 小部件实例ID
     * @param config 配置数据
     */
    suspend fun saveConfig(appWidgetId: Int, config: WidgetConfig)

    /**
     * 获取配置
     * @param appWidgetId 小部件实例ID
     * @return 配置数据，不存在则返回null
     */
    suspend fun getConfig(appWidgetId: Int): WidgetConfig?

    /**
     * 观察配置变化
     * @param appWidgetId 小部件实例ID
     * @return 配置数据流
     */
    fun observeConfig(appWidgetId: Int): Flow<WidgetConfig?>

    /**
     * 获取所有小部件ID
     * @return 所有已配置的小部件ID集合
     */
    suspend fun getAllWidgetIds(): Set<Int>

    /**
     * 删除配置
     * @param appWidgetId 小部件实例ID
     */
    suspend fun deleteConfig(appWidgetId: Int)
}
