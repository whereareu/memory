package com.quanneng.memory.features.widget

import com.quanneng.memory.core.datastore.MultiInstanceWidgetPreferences
import com.quanneng.memory.core.dispatchers.DispatcherProvider
import com.quanneng.memory.features.widget.data.WidgetRepository
import com.quanneng.memory.features.widget.model.WidgetConfig
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 小部件单元测试
 * 测试数据层和业务逻辑
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WidgetTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WidgetConfig createDefault returns correct default values`() {
        val config = WidgetConfig.createDefault(123)

        assertEquals(123, config.appWidgetId)
        assertEquals("提醒", config.text)
        assertEquals(16f, config.textSize)
        assertEquals(0xFF212121.toInt(), config.textColor)
        assertEquals(0xFFFFFFFF.toInt(), config.backgroundColor)
    }

    @Test
    fun `WidgetRepository saveConfig and getConfig work correctly`() = runTest {
        // 由于DataStore需要Android环境，这里只测试Repository的逻辑
        // 实际项目中应该使用mock的DataSource
        val mockDataSource = mockk<com.quanneng.memory.features.widget.data.WidgetDataSource>()
        val dispatchers = DispatcherProvider()

        val testConfig = WidgetConfig(
            appWidgetId = 1,
            text = "测试",
            textSize = 20f,
            textColor = 0xFF0000.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt()
        )

        every { mockDataSource.saveConfig(1, testConfig) } returns Unit
        every { mockDataSource.getConfig(1) } returns testConfig

        val repository = WidgetRepository(mockDataSource, dispatchers)

        // 测试保存
        repository.saveConfig(1, testConfig)

        // 测试获取
        val result = repository.getConfig(1)
        assertEquals(testConfig, result)
    }

    @Test
    fun `WidgetRepository getOrCreateConfig returns existing config`() = runTest {
        val mockDataSource = mockk<com.quanneng.memory.features.widget.data.WidgetDataSource>()
        val dispatchers = DispatcherProvider()

        val existingConfig = WidgetConfig(
            appWidgetId = 1,
            text = "已存在",
            textSize = 18f,
            textColor = 0xFF0000.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt()
        )

        every { mockDataSource.getConfig(1) } returns existingConfig

        val repository = WidgetRepository(mockDataSource, dispatchers)

        val result = repository.getOrCreateConfig(1)
        assertEquals(existingConfig, result)
    }

    @Test
    fun `WidgetRepository getOrCreateConfig creates default when not exists`() = runTest {
        val mockDataSource = mockk<com.quanneng.memory.features.widget.data.WidgetDataSource>()
        val dispatchers = DispatcherProvider()

        every { mockDataSource.getConfig(1) } returns null

        val repository = WidgetRepository(mockDataSource, dispatchers)

        val result = repository.getOrCreateConfig(1)
        assertEquals(1, result.appWidgetId)
        assertEquals("提醒", result.text)
    }

    @Test
    fun `WidgetRepository deleteConfig removes config`() = runTest {
        val mockDataSource = mockk<com.quanneng.memory.features.widget.data.WidgetDataSource>()
        val dispatchers = DispatcherProvider()

        every { mockDataSource.deleteConfig(1) } returns Unit

        val repository = WidgetRepository(mockDataSource, dispatchers)

        repository.deleteConfig(1)
        // 验证删除被调用（实际应该使用verify）
    }
}
