package com.quanneng.memory.features.articles.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 文章来源
 */
@Serializable
enum class ArticleSource {
    @SerialName("掘金")
    JUEJIN,

    @SerialName("CSDN")
    CSDN,

    @SerialName("Medium")
    MEDIUM,

    @SerialName("Android Developers Blog")
    ANDROID_DEVELOPERS_BLOG;

    val displayName: String
        get() = when (this) {
            JUEJIN -> "掘金"
            CSDN -> "CSDN"
            MEDIUM -> "Medium"
            ANDROID_DEVELOPERS_BLOG -> "Android Developers"
        }
}

/**
 * 单篇文章数据
 */
@Serializable
data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val author: String,
    val source: ArticleSource,
    val url: String,
    @SerialName("cover_image")
    val coverImage: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("published_at")
    val publishedAt: String,
    @SerialName("read_time_minutes")
    val readTimeMinutes: Int = 5
) {
    /**
     * 格式化发布时间
     */
    fun getFormattedPublishTime(): String {
        return try {
            val instant = Instant.parse(publishedAt)
            val dateTime = instant.atZone(ZoneId.systemDefault())
            val now = Instant.now().atZone(ZoneId.systemDefault())

            val hoursDiff = java.time.Duration.between(dateTime, now).toHours()
            when {
                hoursDiff < 1 -> "刚刚"
                hoursDiff < 24 -> "${hoursDiff}小时前"
                hoursDiff < 48 -> "昨天"
                hoursDiff < 24 * 7 -> "${hoursDiff / 24}天前"
                else -> dateTime.format(DateTimeFormatter.ofPattern("MM-dd"))
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 获取阅读时间文本
     */
    fun getReadTimeText(): String {
        return "${readTimeMinutes}分钟"
    }
}

/**
 * 数据源信息
 */
@Serializable
data class ArticleSourceInfo(
    val name: String,
    val icon: String,
    val url: String
)

/**
 * 文章数据容器
 */
@Serializable
data class ArticleData(
    val version: String,
    @SerialName("last_updated")
    val lastUpdated: String,
    val sources: List<ArticleSourceInfo> = emptyList(),
    val articles: List<Article> = emptyList()
)

/**
 * 文章标签
 */
@Serializable
data class ArticleTag(
    val name: String,
    val isSelected: Boolean = false
)
