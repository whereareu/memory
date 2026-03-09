package com.quanneng.memory.features.flashthought.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quanneng.memory.features.flashthought.model.FlashThought

/**
 * 闪现数据库
 * 使用 Room 进行本地持久化存储
 */
@Database(
    entities = [FlashThought::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(FlashThoughtConverters::class)
abstract class FlashThoughtDatabase : RoomDatabase() {
    abstract fun flashThoughtDao(): FlashThoughtDao
}
