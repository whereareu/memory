package com.quanneng.memory.features.applist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quanneng.memory.features.applist.data.AppListRepository

/**
 * AppListViewModel 工厂类
 * 用于创建带有 Repository 的 ViewModel 实例
 */
class AppListViewModelFactory(
    private val repository: AppListRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppListViewModel::class.java)) {
            return AppListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
