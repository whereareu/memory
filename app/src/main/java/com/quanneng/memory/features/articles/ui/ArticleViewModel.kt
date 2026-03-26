package com.quanneng.memory.features.articles.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quanneng.memory.features.articles.data.ArticleRepository
import com.quanneng.memory.features.articles.model.Article
import com.quanneng.memory.features.articles.model.ArticleSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 文章列表 UI 状态
 */
sealed class ArticleUiState {
    object Loading : ArticleUiState()
    data class Success(
        val articles: List<Article>,
        val filteredArticles: List<Article> = articles,
        val sources: List<ArticleSource> = emptyList(),
        val selectedSource: ArticleSource? = null,
        val searchQuery: String = ""
    ) : ArticleUiState()
    data class Error(val message: String) : ArticleUiState()
}

/**
 * 文章列表 ViewModel
 */
class ArticleViewModel(
    private val repository: ArticleRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleUiState>(ArticleUiState.Loading)
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
    }

    /**
     * 加载文章
     */
    fun loadArticles() {
        viewModelScope.launch {
            _uiState.value = ArticleUiState.Loading

            repository.fetchArticles()
                .onSuccess { data ->
                    val sources = data.articles.map { it.source }.distinct()
                    _uiState.value = ArticleUiState.Success(
                        articles = data.articles,
                        filteredArticles = data.articles,
                        sources = sources
                    )
                }
                .onFailure { e ->
                    _uiState.value = ArticleUiState.Error(
                        message = e.message ?: "加载失败，请检查网络连接"
                    )
                }
        }
    }

    /**
     * 按来源筛选
     */
    fun filterBySource(source: ArticleSource?) {
        val currentState = _uiState.value
        if (currentState !is ArticleUiState.Success) return

        _uiState.value = currentState.copy(
            filteredArticles = if (source == null) {
                currentState.articles
            } else {
                currentState.articles.filter { it.source == source }
            },
            selectedSource = source
        )
    }

    /**
     * 搜索文章
     */
    fun searchArticles(query: String) {
        val currentState = _uiState.value
        if (currentState !is ArticleUiState.Success) return

        val filtered = if (query.isBlank()) {
            currentState.articles
        } else {
            val lowerQuery = query.lowercase()
            currentState.articles.filter { article ->
                article.title.lowercase().contains(lowerQuery) ||
                article.summary.lowercase().contains(lowerQuery) ||
                article.tags.any { it.lowercase().contains(lowerQuery) }
            }
        }

        _uiState.value = currentState.copy(
            filteredArticles = filtered,
            searchQuery = query
        )
    }

    /**
     * 刷新文章
     */
    fun refresh() {
        loadArticles()
    }
}
