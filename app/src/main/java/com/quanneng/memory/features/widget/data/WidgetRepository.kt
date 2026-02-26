package com.quanneng.memory.features.widget.data

import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.features.widget.model.WidgetConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 小部件数据仓库
 * 协调数据源，提供业务层数据访问
 */
class WidgetRepository(
    private val dataSource: WidgetDataSource,
    private val dispatchers: DispatcherProvider
) {
    /**
     * 保存配置
     */
    suspend fun saveConfig(appWidgetId: Int, config: WidgetConfig) {
        dataSource.saveConfig(appWidgetId, config)
    }

    /**
     * 获取配置
     */
    suspend fun getConfig(appWidgetId: Int): WidgetConfig? {
        return dataSource.getConfig(appWidgetId)
    }

    /**
     * 观察配置变化
     */
    fun observeConfig(appWidgetId: Int): Flow<WidgetConfig?> {
        return dataSource.observeConfig(appWidgetId)
    }

    /**
     * 获取所有小部件ID
     */
    suspend fun getAllWidgetIds(): Set<Int> {
        return dataSource.getAllWidgetIds()
    }

    /**
     * 删除配置
     */
    suspend fun deleteConfig(appWidgetId: Int) {
        dataSource.deleteConfig(appWidgetId)
    }

    /**
     * 获取或创建默认配置
     */
    suspend fun getOrCreateConfig(appWidgetId: Int): WidgetConfig {
        return withContext(dispatchers.io) {
            getConfig(appWidgetId) ?: WidgetConfig.createDefault(appWidgetId)
        }
    }
}
