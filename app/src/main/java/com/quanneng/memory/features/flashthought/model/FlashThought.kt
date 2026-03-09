package com.quanneng.memory.features.flashthought.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * 闪现数据模型
 * 存储用户快速记录的想法和灵感
 */
@Entity(tableName = "flash_thoughts")
data class FlashThought(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant = Instant.now(),
    val tags: String = "",           // 逗号分隔的标签
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,   // 是否置顶
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false   // 软删除标记
)
