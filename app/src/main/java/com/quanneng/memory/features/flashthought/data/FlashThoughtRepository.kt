package com.quanneng.memory.features.flashthought.data

import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.features.flashthought.model.FlashThought
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * 闪现数据仓库
 * 协调数据源，提供业务层数据访问
 */
class FlashThoughtRepository(
    private val dao: FlashThoughtDao,
    private val dispatchers: DispatcherProvider
) {
    /**
     * 添加新闪现
     * @return 新插入记录的 ID
     */
    suspend fun addThought(content: String, tags: String = ""): Long = withContext(dispatchers.io) {
        val thought = FlashThought(
            content = content,
            tags = tags,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        dao.insert(thought)
    }

    /**
     * 更新闪现
     */
    suspend fun updateThought(thought: FlashThought) = withContext(dispatchers.io) {
        dao.update(thought.copy(updatedAt = Instant.now()))
    }

    /**
     * 更新闪现内容
     */
    suspend fun updateThoughtContent(id: Long, content: String, tags: String) = withContext(dispatchers.io) {
        val existing = dao.getById(id) ?: return@withContext
        dao.update(existing.copy(content = content, tags = tags, updatedAt = Instant.now()))
    }

    /**
     * 删除闪现（软删除）
     */
    suspend fun deleteThought(id: Long) = withContext(dispatchers.io) {
        dao.softDelete(id)
    }

    /**
     * 获取所有闪现
     */
    fun getAllThoughts(): Flow<List<FlashThought>> {
        return dao.getAllUndeleted()
    }

    /**
     * 搜索闪现
     */
    fun searchThoughts(query: String): Flow<List<FlashThought>> {
        return dao.search(query)
    }

    /**
     * 获取最新闪现（用于 Widget）
     */
    suspend fun getLatestThought(): FlashThought? = withContext(dispatchers.io) {
        dao.getLatest()
    }

    /**
     * 根据 ID 获取闪现
     */
    suspend fun getThoughtById(id: Long): FlashThought? = withContext(dispatchers.io) {
        dao.getById(id)
    }

    /**
     * 切换置顶状态
     */
    suspend fun togglePin(id: Long) = withContext(dispatchers.io) {
        dao.togglePin(id)
    }

    /**
     * 获取指定日期范围内的闪现
     */
    fun getThoughtsByDateRange(startTime: Instant, endTime: Instant): Flow<List<FlashThought>> {
        return dao.getByDateRange(startTime, endTime)
    }

    /**
     * 永久删除已标记删除的记录
     */
    suspend fun cleanupDeleted() = withContext(dispatchers.io) {
        dao.deletePermanently()
    }
}
