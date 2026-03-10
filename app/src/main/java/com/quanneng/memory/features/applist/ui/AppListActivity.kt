package com.quanneng.memory.features.applist.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import com.quanneng.memory.core.di.AppContainer

/**
 * 应用列表管理页面
 * 显示手机所有已安装应用，支持搜索、卸载、查看详情等功能
 */
class AppListActivity : ComponentActivity() {

    private val viewModel: AppListViewModel by viewModels {
        val container = AppContainer(applicationContext)
        AppListViewModelFactory(container.appListRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                AppListScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
