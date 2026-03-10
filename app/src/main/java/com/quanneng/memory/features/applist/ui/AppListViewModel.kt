package com.quanneng.memory.features.applist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quanneng.memory.features.applist.data.AppListRepository
import com.quanneng.memory.features.applist.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 应用列表ViewModel
 * 处理应用列表的业务逻辑
 */
class AppListViewModel(
    private val repository: AppListRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow<AppListUiState>(AppListUiState.Loading)
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    /**
     * 加载应用列表
     */
    fun loadApps(includeSystemApps: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = AppListUiState.Loading
            try {
                val apps = repository.getAllApps(includeSystemApps)
                _uiState.value = AppListUiState.Success(
                    apps = apps,
                    includeSystemApps = includeSystemApps,
                    searchQuery = ""
                )
            } catch (e: Exception) {
                _uiState.value = AppListUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    /**
     * 刷新应用列表（下拉刷新）
     */
    fun refresh() {
        val currentState = _uiState.value
        val includeSystemApps = when (currentState) {
            is AppListUiState.Success -> currentState.includeSystemApps
            else -> false
        }
        loadApps(includeSystemApps)
    }

    /**
     * 搜索应用
     */
    fun searchApps(query: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is AppListUiState.Success) {
                _uiState.value = currentState.copy(searchQuery = query, isSearching = true)
                try {
                    val apps = repository.searchApps(query, currentState.includeSystemApps)
                    _uiState.value = currentState.copy(
                        apps = apps,
                        searchQuery = query,
                        isSearching = false
                    )
                } catch (e: Exception) {
                    _uiState.value = AppListUiState.Error(e.message ?: "搜索失败")
                }
            }
        }
    }

    /**
     * 切换系统应用显示
     */
    fun toggleSystemApps() {
        val currentState = _uiState.value
        val newIncludeState = when (currentState) {
            is AppListUiState.Success -> !currentState.includeSystemApps
            else -> false
        }
        loadApps(newIncludeState)
    }

    /**
     * 卸载应用
     */
    fun uninstallApp(packageName: String) {
        viewModelScope.launch {
            try {
                val success = repository.uninstallApp(packageName)
                if (success) {
                    // 卸载成功，刷新列表
                    refresh()
                } else {
                    _uiState.value = AppListUiState.Error("卸载失败")
                }
            } catch (e: Exception) {
                _uiState.value = AppListUiState.Error(e.message ?: "卸载失败")
            }
        }
    }

    /**
     * 获取应用详情
     */
    fun getAppDetail(packageName: String) {
        viewModelScope.launch {
            try {
                val app = repository.getAppByPackage(packageName)
                if (app != null) {
                    _uiState.value = AppListUiState.AppDetail(app)
                } else {
                    _uiState.value = AppListUiState.Error("应用不存在")
                }
            } catch (e: Exception) {
                _uiState.value = AppListUiState.Error(e.message ?: "获取详情失败")
            }
        }
    }

    /**
     * 关闭详情页
     */
    fun closeDetail() {
        val currentState = _uiState.value
        if (currentState is AppListUiState.AppDetail) {
            // 恢复到之前的列表状态
            refresh()
        }
    }
}

/**
 * UI状态封装
 */
sealed class AppListUiState {
    object Loading : AppListUiState()
    data class Success(
        val apps: List<AppInfo>,
        val includeSystemApps: Boolean,
        val searchQuery: String,
        val isSearching: Boolean = false,
        val isRefreshing: Boolean = false
    ) : AppListUiState()
    data class Error(val message: String) : AppListUiState()
    data class AppDetail(val app: AppInfo) : AppListUiState()
}
