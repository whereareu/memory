package com.quanneng.memory.features.flashthought.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quanneng.memory.features.flashthought.model.FlashThought
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * 闪现数据访问对象
 * 所有方法均为 suspend，在 IO 线程执行
 */
@Dao
interface FlashThoughtDao {

    /**
     * 插入新的闪现
     * @return 新插入记录的 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashThought: FlashThought): Long

    /**
     * 更新闪现
     */
    @Update
    suspend fun update(flashThought: FlashThought)

    /**
     * 软删除闪现
     */
    @Query("UPDATE flash_thoughts SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: Long, updatedAt: Instant = Instant.now())

    /**
     * 永久删除已标记删除的记录
     */
    @Query("DELETE FROM flash_thoughts WHERE is_deleted = 1")
    suspend fun deletePermanently()

    /**
     * 获取所有未删除的闪现（按时间倒序，置顶优先）
     */
    @Query("""
        SELECT * FROM flash_thoughts
        WHERE is_deleted = 0
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun getAllUndeleted(): Flow<List<FlashThought>>

    /**
     * 搜索闪现（支持内容和标签搜索）
     */
    @Query("""
        SELECT * FROM flash_thoughts
        WHERE is_deleted = 0
        AND (content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')
        ORDER BY is_pinned DESC, created_at DESC
    """)
    fun search(query: String): Flow<List<FlashThought>>

    /**
     * 获取最新的一条闪现（用于 Widget 显示）
     */
    @Query("""
        SELECT * FROM flash_thoughts
        WHERE is_deleted = 0
        ORDER BY is_pinned DESC, created_at DESC
        LIMIT 1
    """)
    suspend fun getLatest(): FlashThought?

    /**
     * 根据 ID 获取闪现
     */
    @Query("SELECT * FROM flash_thoughts WHERE id = :id")
    suspend fun getById(id: Long): FlashThought?

    /**
     * 切换置顶状态
     */
    @Query("UPDATE flash_thoughts SET is_pinned = NOT is_pinned, updated_at = :updatedAt WHERE id = :id")
    suspend fun togglePin(id: Long, updatedAt: Instant = Instant.now())

    /**
     * 获取指定日期范围内的闪现
     */
    @Query("""
        SELECT * FROM flash_thoughts
        WHERE is_deleted = 0
        AND created_at BETWEEN :startTime AND :endTime
        ORDER BY created_at DESC
    """)
    fun getByDateRange(startTime: Instant, endTime: Instant): Flow<List<FlashThought>>
}
