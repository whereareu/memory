package com.quanneng.memory.features.articles.data

import android.content.Context
import android.util.Log
import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.core.dispatchers.IoDispatcher
import com.quanneng.memory.features.articles.model.Article
import com.quanneng.memory.features.articles.model.ArticleData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * GitHub Raw URL 常量
 */
private const val GITHUB_RAW_URL = "https://raw.githubusercontent.com/whereareu/memory-data/main/articles.json"

/**
 * 文章数据仓库
 */
class ArticleRepository(
    private val dispatcherProvider: DispatcherProvider,
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
        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorClient", message)
                }
            }
        }
    }

    /**
     * 从 GitHub 获取文章数据
     */
    @IoDispatcher
    suspend fun fetchArticles(): Result<ArticleData> = withContext(dispatcherProvider.io) {
        try {
            Log.d("ArticleRepository", "开始请求 $GITHUB_RAW_URL")
            Log.d("ArticleRepository", "当前线程: ${Thread.currentThread().name}")

            val startTime = System.currentTimeMillis()
            Log.d("ArticleRepository", "发起HTTP请求...")

            val response = client.get(GITHUB_RAW_URL) {
                headers {
                    append("Accept", "application/json, text/plain, */*")
                }
            }
            Log.d("ArticleRepository", "收到响应，状态: ${response.status.value}, Content-Type: ${response.headers["Content-Type"]}, 耗时: ${System.currentTimeMillis() - startTime}ms")

            // 手动获取响应文本并解析JSON（GitHub Raw返回text/plain）
            val responseText = response.bodyAsText()
            Log.d("ArticleRepository", "响应体长度: ${responseText.length} 字符")

            val body = json.decodeFromString<ArticleData>(responseText)
            Log.d("ArticleRepository", "成功解析JSON，文章数: ${body.articles.size}")
            Log.d("ArticleRepository", "总耗时: ${System.currentTimeMillis() - startTime}ms")

            Result.success(body)
        } catch (e: Exception) {
            Log.e("ArticleRepository", "获取数据失败 - 类型: ${e.javaClass.simpleName}", e)
            Log.e("ArticleRepository", "错误消息: ${e.message}")
            e.stackTrace.take(10).forEach {
                Log.e("ArticleRepository", "    at $it")
            }
            Result.failure(e)
        }
    }

    /**
     * 获取所有文章
     */
    @IoDispatcher
    suspend fun getAllArticles(): Result<List<Article>> = withContext(dispatcherProvider.io) {
        fetchArticles().mapCatching { it.articles }
    }

    /**
     * 按来源筛选文章
     */
    @IoDispatcher
    suspend fun getArticlesBySource(source: com.quanneng.memory.features.articles.model.ArticleSource): Result<List<Article>> = withContext(dispatcherProvider.io) {
        fetchArticles().mapCatching { data ->
            data.articles.filter { it.source == source }
        }
    }

    /**
     * 搜索文章
     */
    @IoDispatcher
    suspend fun searchArticles(query: String): Result<List<Article>> = withContext(dispatcherProvider.io) {
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
    suspend fun getSources(): Result<List<com.quanneng.memory.features.articles.model.ArticleSourceInfo>> = withContext(dispatcherProvider.io) {
        fetchArticles().mapCatching { it.sources }
    }
}
