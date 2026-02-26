package com.quanneng.memory.features.widget.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quanneng.memory.features.widget.data.WidgetRepository
import com.quanneng.memory.features.widget.model.WidgetConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 小部件配置ViewModel
 * 处理配置界面的业务逻辑
 */
class WidgetConfigViewModel(
    private val repository: WidgetRepository,
    private val appWidgetId: Int
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow<WidgetConfigUiState>(WidgetConfigUiState.Loading)
    val uiState: StateFlow<WidgetConfigUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    /**
     * 加载配置
     */
    private fun loadConfig() {
        viewModelScope.launch {
            val config = repository.getOrCreateConfig(appWidgetId)
            _uiState.value = WidgetConfigUiState.Success(config)
        }
    }

    /**
     * 保存配置
     */
    suspend fun saveConfig(config: WidgetConfig): Boolean {
        return try {
            repository.saveConfig(appWidgetId, config)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 更新文本
     */
    fun updateText(text: String) {
        val currentState = _uiState.value
        if (currentState is WidgetConfigUiState.Success) {
            _uiState.value = currentState.copy(
                config = currentState.config.copy(text = text)
            )
        }
    }

    /**
     * 更新文本大小
     */
    fun updateTextSize(size: Float) {
        val currentState = _uiState.value
        if (currentState is WidgetConfigUiState.Success) {
            _uiState.value = currentState.copy(
                config = currentState.config.copy(textSize = size)
            )
        }
    }

    /**
     * 更新文本颜色
     */
    fun updateTextColor(color: Int) {
        val currentState = _uiState.value
        if (currentState is WidgetConfigUiState.Success) {
            _uiState.value = currentState.copy(
                config = currentState.config.copy(textColor = color)
            )
        }
    }

    /**
     * 更新背景颜色
     */
    fun updateBackgroundColor(color: Int) {
        val currentState = _uiState.value
        if (currentState is WidgetConfigUiState.Success) {
            _uiState.value = currentState.copy(
                config = currentState.config.copy(backgroundColor = color)
            )
        }
    }
}

/**
 * UI状态封装
 */
sealed class WidgetConfigUiState {
    object Loading : WidgetConfigUiState()
    data class Success(val config: WidgetConfig) : WidgetConfigUiState()
    data class Error(val message: String) : WidgetConfigUiState()
}
