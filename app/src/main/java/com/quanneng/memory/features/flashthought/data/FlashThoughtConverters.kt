package com.quanneng.memory.features.flashthought.data

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Room 类型转换器
 * 处理 Instant 类型的存储
 */
class FlashThoughtConverters {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }
}
