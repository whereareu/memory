package com.quanneng.memory.features.articles.ui

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.quanneng.memory.MemoryApp
import com.quanneng.memory.features.articles.data.ArticleRepository
import com.quanneng.memory.features.articles.ui.screen.ArticleListScreen

/**
 * 文章列表 Activity
 */
class ArticleListActivity : ComponentActivity() {

    private val repository: ArticleRepository
        get() = (application as MemoryApp).container.articleRepository

    private val viewModel: ArticleViewModel by lazy {
        val factory = ArticleViewModelFactory(repository, application)
        ViewModelProvider(this, factory)[ArticleViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArticleListScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }
}

/**
 * ViewModel Factory
 */
class ArticleViewModelFactory(
    private val repository: ArticleRepository,
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            return ArticleViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
