package com.quanneng.memory.features.applist.model

import android.graphics.drawable.Drawable

/**
 * 应用信息数据模型
 * 存储应用的基本信息，用于列表展示和管理
 */
data class AppInfo(
    val packageName: String,
    val label: CharSequence,
    val icon: Drawable?,
    val versionName: String?,
    val versionCode: Long,
    val installTime: Long,
    val lastUpdateTime: Long,
    val isSystemApp: Boolean,
    val flags: Int
)
