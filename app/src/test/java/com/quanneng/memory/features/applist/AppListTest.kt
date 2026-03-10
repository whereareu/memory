package com.quanneng.memory.features.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.quanneng.memory.features.applist.data.AppListRepository
import com.quanneng.memory.features.applist.model.AppInfo
import com.quanneng.memory.features.applist.ui.AppListUiState
import com.quanneng.memory.features.applist.ui.AppListViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * 应用列表单元测试
 * 测试数据层和业务逻辑
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppListTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockContext = mockk()
        mockPackageManager = mockk()

        every { mockContext.packageManager } returns mockPackageManager
        every { mockContext.startActivity(any()) } returns Unit
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `AppListRepository_getAllApps_excludes_system_apps_by_default`() = runTest {
        // 准备测试数据
        val systemApp = createMockApplicationInfo(
            packageName = "com.android.system",
            flags = ApplicationInfo.FLAG_SYSTEM,
            label = "System App"
        )
        val userApp = createMockApplicationInfo(
            packageName = "com.example.app",
            flags = 0,
            label = "User App"
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) }
            .returns(listOf(systemApp, userApp))

        // 模拟 PackageInfo
        every { mockPackageManager.getPackageInfo("com.android.system", 0) } returns createMockPackageInfo("com.android.system")
        every { mockPackageManager.getPackageInfo("com.example.app", 0) } returns createMockPackageInfo("com.example.app")

        // 执行测试
        val repository = AppListRepository(mockContext)
        val result = repository.getAllApps(includeSystemApps = false)

        // 验证
        assertEquals(1, result.size)
        assertEquals("com.example.app", result[0].packageName)
        assertFalse(result[0].isSystemApp)
    }

    @Test
    fun `AppListRepository_getAllApps_includes_system_apps_when_requested`() = runTest {
        // 准备测试数据
        val apps = listOf(
            createMockApplicationInfo("com.android.system", ApplicationInfo.FLAG_SYSTEM, "System App"),
            createMockApplicationInfo("com.example.app", 0, "User App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        // 执行测试
        val repository = AppListRepository(mockContext)
        val result = repository.getAllApps(includeSystemApps = true)

        // 验证
        assertEquals(2, result.size)
    }

    @Test
    fun `AppListRepository_getAllApps_sorts_by_label_alphabetically`() = runTest {
        // 准备测试数据（乱序）
        val apps = listOf(
            createMockApplicationInfo("com.c", 0, "Charlie"),
            createMockApplicationInfo("com.a", 0, "Alpha"),
            createMockApplicationInfo("com.b", 0, "Bravo")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        // 执行测试
        val repository = AppListRepository(mockContext)
        val result = repository.getAllApps(includeSystemApps = false)

        // 验证排序
        assertEquals("Alpha", result[0].label.toString())
        assertEquals("Bravo", result[1].label.toString())
        assertEquals("Charlie", result[2].label.toString())
    }

    @Test
    fun `AppListRepository_getAppByPackage_returns_app_when_found`() = runTest {
        val packageName = "com.example.app"
        val appInfo = createMockApplicationInfo(packageName, 0, "Test App")

        every { mockPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA) } returns appInfo
        every { mockPackageManager.getPackageInfo(packageName, 0) } returns createMockPackageInfo(packageName)

        // 执行测试
        val repository = AppListRepository(mockContext)
        val result = repository.getAppByPackage(packageName)

        // 验证
        assertNotNull(result)
        assertEquals(packageName, result!!.packageName)
        assertEquals("Test App", result.label.toString())
    }

    @Test
    fun `AppListRepository_getAppByPackage_returns_null_when_not_found`() = runTest {
        val packageName = "com.nonexistent"

        every { mockPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA) }
            .throws(PackageManager.NameNotFoundException())

        // 执行测试
        val repository = AppListRepository(mockContext)
        val result = repository.getAppByPackage(packageName)

        // 验证
        assertNull(result)
    }

    @Test
    fun `AppListRepository_searchApps_filters_by_package_name_and_label`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.music", 0, "Music Player"),
            createMockApplicationInfo("com.example.video", 0, "Video Player"),
            createMockApplicationInfo("com.example.photos", 0, "Photo Gallery")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        // 执行测试
        val repository = AppListRepository(mockContext)
        val result = repository.searchApps("player", includeSystemApps = false)

        // 验证搜索结果
        assertEquals(2, result.size)
        assertTrue(result.any { it.packageName == "com.example.music" })
        assertTrue(result.any { it.packageName == "com.example.video" })
        assertFalse(result.any { it.packageName == "com.example.photos" })
    }

    @Test
    fun `AppListRepository_searchApps_case_insensitive`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        // 执行测试
        val repository = AppListRepository(mockContext)
        val resultLower = repository.searchApps("test", includeSystemApps = false)
        val resultUpper = repository.searchApps("TEST", includeSystemApps = false)
        val resultMixed = repository.searchApps("TeSt", includeSystemApps = false)

        // 验证大小写不敏感
        assertEquals(1, resultLower.size)
        assertEquals(1, resultUpper.size)
        assertEquals(1, resultMixed.size)
    }

    @Test
    fun `AppListRepository_getSystemAppsCount_returns_correct_count`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.android.system1", ApplicationInfo.FLAG_SYSTEM, "System1"),
            createMockApplicationInfo("com.android.system2", ApplicationInfo.FLAG_SYSTEM, "System2"),
            createMockApplicationInfo("com.example.app", 0, "User App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps

        // 执行测试
        val repository = AppListRepository(mockContext)
        val result = repository.getSystemAppsCount()

        // 验证
        assertEquals(2, result)
    }

    @Test
    fun `AppListRepository_getUserAppsCount_returns_correct_count`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.android.system", ApplicationInfo.FLAG_SYSTEM, "System"),
            createMockApplicationInfo("com.example.app1", 0, "User App1"),
            createMockApplicationInfo("com.example.app2", 0, "User App2")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps

        // 执行测试
        val repository = AppListRepository(mockContext)
        val result = repository.getUserAppsCount()

        // 验证
        assertEquals(2, result)
    }

    // ViewModel 测试

    @Test
    fun `AppListViewModel_refresh_sets_isRefreshing_state_correctly`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.app1", 0, "App1"),
            createMockApplicationInfo("com.example.app2", 0, "App2")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()
        var initialState = viewModel.uiState.value
        assertTrue(initialState is AppListUiState.Success)
        assertFalse((initialState as AppListUiState.Success).isRefreshing)

        // 执行刷新
        viewModel.refresh()
        testScheduler.advanceUntilIdle()

        // 验证刷新完成后的状态
        val finalState = viewModel.uiState.value
        assertTrue(finalState is AppListUiState.Success)
        assertFalse((finalState as AppListUiState.Success).isRefreshing)
    }

    @Test
    fun `AppListViewModel_refresh_maintains_includeSystemApps_preference`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.android.system", ApplicationInfo.FLAG_SYSTEM, "System"),
            createMockApplicationInfo("com.example.app", 0, "User App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()

        // 切换到显示系统应用
        viewModel.toggleSystemApps()
        testScheduler.advanceUntilIdle()

        var stateBeforeRefresh = viewModel.uiState.value
        assertTrue(stateBeforeRefresh is AppListUiState.Success)
        assertTrue((stateBeforeRefresh as AppListUiState.Success).includeSystemApps)

        // 执行刷新
        viewModel.refresh()
        testScheduler.advanceUntilIdle()

        // 验证刷新后仍然显示系统应用
        val stateAfterRefresh = viewModel.uiState.value
        assertTrue(stateAfterRefresh is AppListUiState.Success)
        assertTrue((stateAfterRefresh as AppListUiState.Success).includeSystemApps)
        assertEquals(2, stateAfterRefresh.apps.size)
    }

    @Test
    fun `AppListViewModel_refresh_handles_error_gracefully`() = runTest {
        val repository = mockk<AppListRepository>()
        coEvery { repository.getAllApps(any()) } throws Exception("网络错误")

        val viewModel = AppListViewModel(repository)
        testScheduler.advanceUntilIdle()

        // 执行刷新
        viewModel.refresh()
        testScheduler.advanceUntilIdle()

        // 验证错误状态
        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Error)
        if (state is AppListUiState.Error) {
            assertEquals("刷新失败", state.message)
        }
    }

    @Test
    fun `AppListViewModel_searchApps_filters_results_correctly`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.music", 0, "Music Player"),
            createMockApplicationInfo("com.example.video", 0, "Video Player"),
            createMockApplicationInfo("com.example.photos", 0, "Photo Gallery")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()

        // 执行搜索
        viewModel.searchApps("player")
        testScheduler.advanceUntilIdle()

        // 验证搜索结果
        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Success)
        if (state is AppListUiState.Success) {
            assertEquals("player", state.searchQuery)
            assertEquals(2, state.apps.size)
        }
    }

    @Test
    fun `AppListViewModel_searchApps_clears_results_with_empty_query`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.music", 0, "Music Player")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()

        // 先执行搜索
        viewModel.searchApps("music")
        testScheduler.advanceUntilIdle()

        // 清空搜索
        viewModel.searchApps("")
        testScheduler.advanceUntilIdle()

        // 验证返回完整列表
        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Success)
        if (state is AppListUiState.Success) {
            assertEquals("", state.searchQuery)
            assertEquals(1, state.apps.size)
        }
    }

    @Test
    fun `AppListViewModel_showAppMenu_displays_menu_state`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()

        // 获取应用信息
        val initialState = viewModel.uiState.value
        assertTrue(initialState is AppListUiState.Success)
        val app = (initialState as AppListUiState.Success).apps[0]

        // 显示操作菜单
        viewModel.showAppMenu(app)
        testScheduler.advanceUntilIdle()

        // 验证菜单状态
        val menuState = viewModel.uiState.value
        assertTrue(menuState is AppListUiState.AppMenu)
        if (menuState is AppListUiState.AppMenu) {
            assertEquals("com.example.app", menuState.app.packageName)
        }
    }

    @Test
    fun `AppListViewModel_closeAppMenu_returns_to_list_state`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()

        // 获取应用信息并显示菜单
        val initialState = viewModel.uiState.value
        val app = (initialState as AppListUiState.Success).apps[0]
        viewModel.showAppMenu(app)
        testScheduler.advanceUntilIdle()

        // 验证菜单状态
        assertTrue(viewModel.uiState.value is AppListUiState.AppMenu)

        // 关闭菜单
        viewModel.closeAppMenu()
        testScheduler.advanceUntilIdle()

        // 验证返回列表状态
        val finalState = viewModel.uiState.value
        assertTrue(finalState is AppListUiState.Success)
    }

    @Test
    fun `AppListViewModel_openApp_succeeds_for_valid_app`() = runTest {
        val mockIntent = mockk<android.content.Intent>()
        every { mockPackageManager.getLaunchIntentForPackage("com.example.app") } returns mockIntent
        every { mockIntent.addFlags(any()) } returns mockIntent

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 打开应用
        viewModel.openApp("com.example.app")
        testScheduler.advanceUntilIdle()

        // 验证没有错误状态
        val state = viewModel.uiState.value
        assertFalse(state is AppListUiState.Error)
    }

    @Test
    fun `AppListViewModel_openApp_fails_for_invalid_app`() = runTest {
        every { mockPackageManager.getLaunchIntentForPackage("com.invalid.app") } returns null

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 打开不存在的应用
        viewModel.openApp("com.invalid.app")
        testScheduler.advanceUntilIdle()

        // 验证错误状态
        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Error)
        if (state is AppListUiState.Error) {
            assertEquals("无法打开该应用", state.message)
        }
    }

    @Test
    fun `AppListViewModel_showUninstallConfirm_displays_confirm_state`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg())
        }
        every { mockPackageManager.getApplicationInfo("com.example.app", PackageManager.GET_META_DATA) } returns apps[0]

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()

        // 显示卸载确认
        viewModel.showUninstallConfirm("com.example.app")
        testScheduler.advanceUntilIdle()

        // 验证卸载确认状态
        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.UninstallConfirm)
        if (state is AppListUiState.UninstallConfirm) {
            assertEquals("com.example.app", state.app.packageName)
        }
    }

    @Test
    fun `AppListViewModel_cancelUninstall_returns_to_list_state`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg())
        }
        every { mockPackageManager.getApplicationInfo("com.example.app", PackageManager.GET_META_DATA) } returns apps[0]

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()

        // 显示卸载确认
        viewModel.showUninstallConfirm("com.example.app")
        testScheduler.advanceUntilIdle()

        // 验证卸载确认状态
        assertTrue(viewModel.uiState.value is AppListUiState.UninstallConfirm)

        // 取消卸载
        viewModel.cancelUninstall()
        testScheduler.advanceUntilIdle()

        // 验证返回列表状态
        val finalState = viewModel.uiState.value
        assertTrue(finalState is AppListUiState.Success)
    }

    @Test
    fun `AppListViewModel_showUninstallConfirm_handles_nonexistent_app`() = runTest {
        every { mockPackageManager.getApplicationInfo("com.nonexistent", PackageManager.GET_META_DATA) }
            .throws(PackageManager.NameNotFoundException())

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 显示不存在应用的卸载确认
        viewModel.showUninstallConfirm("com.nonexistent")
        testScheduler.advanceUntilIdle()

        // 验证错误状态
        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Error)
        if (state is AppListUiState.Error) {
            assertEquals("应用不存在", state.message)
        }
    }

    @Test
    fun `AppListViewModel_getAppDetail_displays_detail_state`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg())
        }
        every { mockPackageManager.getApplicationInfo("com.example.app", PackageManager.GET_META_DATA) } returns apps[0]

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()

        // 获取应用详情
        viewModel.getAppDetail("com.example.app")
        testScheduler.advanceUntilIdle()

        // 验证详情状态
        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.AppDetail)
        if (state is AppListUiState.AppDetail) {
            assertEquals("com.example.app", state.app.packageName)
            assertEquals("Test App", state.app.label.toString())
        }
    }

    @Test
    fun `AppListViewModel_getAppDetail_handles_nonexistent_app`() = runTest {
        every { mockPackageManager.getApplicationInfo("com.nonexistent", PackageManager.GET_META_DATA) }
            .throws(PackageManager.NameNotFoundException())

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 获取不存在应用的详情
        viewModel.getAppDetail("com.nonexistent")
        testScheduler.advanceUntilIdle()

        // 验证错误状态
        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Error)
        if (state is AppListUiState.Error) {
            assertEquals("应用不存在", state.message)
        }
    }

    @Test
    fun `AppListViewModel_closeDetail_returns_to_list_state`() = runTest {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg())
        }
        every { mockPackageManager.getApplicationInfo("com.example.app", PackageManager.GET_META_DATA) } returns apps[0]

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository)

        // 等待初始加载完成
        testScheduler.advanceUntilIdle()

        // 获取应用详情
        viewModel.getAppDetail("com.example.app")
        testScheduler.advanceUntilIdle()

        // 验证详情状态
        assertTrue(viewModel.uiState.value is AppListUiState.AppDetail)

        // 关闭详情
        viewModel.closeDetail()
        testScheduler.advanceUntilIdle()

        // 验证返回列表状态
        val finalState = viewModel.uiState.value
        assertTrue(finalState is AppListUiState.Success)
    }

    // 辅助函数
    private fun createMockApplicationInfo(
        packageName: String,
        flags: Int,
        label: String
    ): ApplicationInfo {
        val appInfo = mockk<ApplicationInfo>()
        every { appInfo.packageName } returns packageName
        every { appInfo.flags } returns flags
        every { appInfo.loadLabel(any()) } returns label
        every { appInfo.loadIcon(any()) } returns mockk<Drawable>()
        return appInfo
    }

    private fun createMockPackageInfo(packageName: String): PackageInfo {
        val packageInfo = mockk<PackageInfo>()
        packageInfo.packageName = packageName
        packageInfo.versionName = "1.0.0"
        packageInfo.longVersionCode = 1L
        packageInfo.firstInstallTime = System.currentTimeMillis()
        packageInfo.lastUpdateTime = System.currentTimeMillis()
        return packageInfo
    }
}
