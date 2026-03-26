package com.quanneng.memory.features.articles.data

import android.content.Context
import com.quanneng.memory.core.dispatchers.IoDispatcher
import com.quanneng.memory.features.articles.model.Article
import com.quanneng.memory.features.articles.model.ArticleData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GitHub Raw URL 常量
 */
private const val GITHUB_RAW_URL = "https://raw.githubusercontent.com/whereareu/memory-data/main/articles.json"

/**
 * 文章数据仓库
 */
@Singleton
class ArticleRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * 从 GitHub 获取文章数据
     */
    @IoDispatcher
    suspend fun fetchArticles(): Result<ArticleData> = withContext(ioDispatcher) {
        try {
            val response: ArticleData = client.get(GITHUB_RAW_URL).body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取所有文章
     */
    @IoDispatcher
    suspend fun getAllArticles(): Result<List<Article>> = withContext(ioDispatcher) {
        fetchArticles().mapCatching { it.articles }
    }

    /**
     * 按来源筛选文章
     */
    @IoDispatcher
    suspend fun getArticlesBySource(source: com.quanneng.memory.features.articles.model.ArticleSource): Result<List<Article>> = withContext(ioDispatcher) {
        fetchArticles().mapCatching { data ->
            data.articles.filter { it.source == source }
        }
    }

    /**
     * 搜索文章
     */
    @IoDispatcher
    suspend fun searchArticles(query: String): Result<List<Article>> = withContext(ioDispatcher) {
        fetchArticles().mapCatching { data ->
            if (query.isBlank()) {
                data.articles
            } else {
                val lowerQuery = query.lowercase()
                data.articles.filter { article ->
                    article.title.lowercase().contains(lowerQuery) ||
                    article.summary.lowercase().contains(lowerQuery) ||
                    article.tags.any { it.lowercase().contains(lowerQuery) }
                }
            }
        }
    }

    /**
     * 获取数据源信息
     */
    @IoDispatcher
    suspend fun getSources(): Result<List<com.quanneng.memory.features.articles.model.ArticleSourceInfo>> = withContext(ioDispatcher) {
        fetchArticles().mapCatching { it.sources }
    }
}
