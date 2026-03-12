package com.quanneng.memory.features.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.quanneng.memory.features.applist.data.AppListRepository
import com.quanneng.memory.features.applist.data.AppSortType
import com.quanneng.memory.features.applist.model.AppInfo
import com.quanneng.memory.features.applist.ui.AppListUiState
import com.quanneng.memory.features.applist.ui.AppListViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockContext = mockk(relaxed = true)
        mockPackageManager = mockk(relaxed = true)

        every { mockContext.packageManager } returns mockPackageManager
        every { mockContext.startActivity(any()) } returns Unit
        every { mockContext.applicationContext } returns mockContext
        every { mockPackageManager.getLaunchIntentForPackage(any()) } returns null
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `AppListRepository_getAllApps_returns_all_apps_sorted_by_name_by_default`() = runTest(testDispatcher) {
        val systemApp = createMockApplicationInfo(
            packageName = "com.android.system",
            flags = ApplicationInfo.FLAG_SYSTEM,
            label = "System App"
        )
        val userApp1 = createMockApplicationInfo(
            packageName = "com.example.beta",
            flags = 0,
            label = "Beta App"
        )
        val userApp2 = createMockApplicationInfo(
            packageName = "com.example.alpha",
            flags = 0,
            label = "Alpha App"
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) }
            .returns(listOf(systemApp, userApp1, userApp2))

        every { mockPackageManager.getPackageInfo("com.android.system", 0) } returns createMockPackageInfo("com.android.system")
        every { mockPackageManager.getPackageInfo("com.example.beta", 0) } returns createMockPackageInfo("com.example.beta")
        every { mockPackageManager.getPackageInfo("com.example.alpha", 0) } returns createMockPackageInfo("com.example.alpha")

        val repository = AppListRepository(mockContext)
        val result = repository.getAllApps()

        assertEquals(3, result.size)
        assertEquals("Alpha App", result[0].label.toString())
        assertEquals("Beta App", result[1].label.toString())
        assertEquals("System App", result[2].label.toString())
    }

    @Test
    fun `AppListRepository_getAllApps_with_sortType_sorts_by_install_time`() = runTest(testDispatcher) {
        val oldApp = createMockApplicationInfo("com.example.old", 0, "Old App")
        val newApp = createMockApplicationInfo("com.example.new", 0, "New App")

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns listOf(oldApp, newApp)

        val oldPackageInfo = createMockPackageInfo("com.example.old", installTime = 1000L)
        val newPackageInfo = createMockPackageInfo("com.example.new", installTime = 2000L)

        every { mockPackageManager.getPackageInfo("com.example.old", 0) } returns oldPackageInfo
        every { mockPackageManager.getPackageInfo("com.example.new", 0) } returns newPackageInfo

        val repository = AppListRepository(mockContext)
        val result = repository.getAllApps(sortType = AppSortType.BY_INSTALL_TIME)

        assertEquals(2, result.size)
        assertEquals("New App", result[0].label.toString())
        assertEquals("Old App", result[1].label.toString())
    }

    @Test
    fun `AppListRepository_getAllApps_with_BY_NAME_sortType_sorts_alphabetically`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.c", 0, "Charlie"),
            createMockApplicationInfo("com.a", 0, "Alpha"),
            createMockApplicationInfo("com.b", 0, "Bravo")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val result = repository.getAllApps(sortType = AppSortType.BY_NAME)

        assertEquals(3, result.size)
        assertEquals("Alpha", result[0].label.toString())
        assertEquals("Bravo", result[1].label.toString())
        assertEquals("Charlie", result[2].label.toString())
    }

    @Test
    fun `AppListRepository_getAppByPackage_returns_app_when_found`() = runTest(testDispatcher) {
        val packageName = "com.example.app"
        val appInfo = createMockApplicationInfo(packageName, 0, "Test App")

        every { mockPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA) } returns appInfo
        every { mockPackageManager.getPackageInfo(packageName, 0) } returns createMockPackageInfo(packageName)

        val repository = AppListRepository(mockContext)
        val result = repository.getAppByPackage(packageName)

        assertNotNull(result)
        assertEquals(packageName, result!!.packageName)
        assertEquals("Test App", result.label.toString())
    }

    @Test
    fun `AppListRepository_getAppByPackage_returns_null_when_not_found`() = runTest(testDispatcher) {
        val packageName = "com.nonexistent"

        every { mockPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA) }
            .throws(PackageManager.NameNotFoundException())

        val repository = AppListRepository(mockContext)
        val result = repository.getAppByPackage(packageName)

        assertNull(result)
    }

    @Test
    fun `AppListRepository_searchApps_filters_by_package_name_and_label`() = runTest(testDispatcher) {
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
        val result = repository.searchApps("player")

        assertEquals(2, result.size)
        assertTrue(result.any { it.packageName == "com.example.music" })
        assertTrue(result.any { it.packageName == "com.example.video" })
        assertFalse(result.any { it.packageName == "com.example.photos" })
    }

    @Test
    fun `AppListRepository_searchApps_case_insensitive`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val resultLower = repository.searchApps("test")
        val resultUpper = repository.searchApps("TEST")
        val resultMixed = repository.searchApps("TeSt")

        assertEquals(1, resultLower.size)
        assertEquals(1, resultUpper.size)
        assertEquals(1, resultMixed.size)
    }

    @Test
    fun `AppListRepository_getSystemAppsCount_returns_correct_count`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.android.system1", ApplicationInfo.FLAG_SYSTEM, "System1"),
            createMockApplicationInfo("com.android.system2", ApplicationInfo.FLAG_SYSTEM, "System2"),
            createMockApplicationInfo("com.example.app", 0, "User App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps

        val repository = AppListRepository(mockContext)
        val result = repository.getSystemAppsCount()

        assertEquals(2, result)
    }

    @Test
    fun `AppListRepository_getUserAppsCount_returns_correct_count`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.android.system", ApplicationInfo.FLAG_SYSTEM, "System"),
            createMockApplicationInfo("com.example.app1", 0, "User App1"),
            createMockApplicationInfo("com.example.app2", 0, "User App2")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps

        val repository = AppListRepository(mockContext)
        val result = repository.getUserAppsCount()

        assertEquals(2, result)
    }

    // ViewModel 测试

    @Test
    fun `AppListViewModel_init_loads_apps_successfully`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app1", 0, "App1"),
            createMockApplicationInfo("com.example.app2", 0, "App2")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待异步操作完成
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Success)
        if (state is AppListUiState.Success) {
            assertEquals(2, state.apps.size)
            assertFalse(state.isRefreshing)
        }
    }

    @Test
    fun `AppListViewModel_refresh_sets_isRefreshing_state_correctly`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app1", 0, "App1"),
            createMockApplicationInfo("com.example.app2", 0, "App2")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertTrue(initialState is AppListUiState.Success)
        assertFalse((initialState as AppListUiState.Success).isRefreshing)

        viewModel.refresh()

        // 等待刷新完成
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is AppListUiState.Success)
        assertFalse((finalState as AppListUiState.Success).isRefreshing)
    }

    @Test
    fun `AppListViewModel_refresh_maintains_sortType_preference`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app1", 0, "App1"),
            createMockApplicationInfo("com.example.app2", 0, "App2")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.setSortType(AppSortType.BY_INSTALL_TIME)

        // 等待 setSortType 完成
        advanceUntilIdle()

        val stateBeforeRefresh = viewModel.uiState.value
        assertTrue(stateBeforeRefresh is AppListUiState.Success)
        assertEquals(AppSortType.BY_INSTALL_TIME, viewModel.getCurrentSortType())

        viewModel.refresh()

        // 等待刷新完成
        advanceUntilIdle()

        val stateAfterRefresh = viewModel.uiState.value
        assertTrue(stateAfterRefresh is AppListUiState.Success)
        assertEquals(AppSortType.BY_INSTALL_TIME, viewModel.getCurrentSortType())
        if (stateAfterRefresh is AppListUiState.Success) {
            assertEquals(2, stateAfterRefresh.apps.size)
        }
    }

    @Test
    fun `AppListViewModel_refresh_handles_error_gracefully`() = runTest(testDispatcher) {
        val mockRepository = mockk<AppListRepository>()
        coEvery { mockRepository.getAllApps(any()) } throws Exception("网络错误")

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns emptyList()

        val viewModel = AppListViewModel(mockRepository, mockContext)

        // 等待初始化完成（会失败）
        advanceUntilIdle()

        viewModel.refresh()

        // 等待刷新完成（会失败）
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Error)
        if (state is AppListUiState.Error) {
            assertEquals("网络错误", state.message)
        }
    }

    @Test
    fun `AppListViewModel_searchApps_filters_results_correctly`() = runTest(testDispatcher) {
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
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.searchApps("player")

        // 等待搜索完成
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Success)
        if (state is AppListUiState.Success) {
            assertEquals("player", state.searchQuery)
            assertEquals(2, state.apps.size)
            assertTrue(state.apps.any { it.packageName == "com.example.music" })
            assertTrue(state.apps.any { it.packageName == "com.example.video" })
        }
    }

    @Test
    fun `AppListViewModel_searchApps_clears_results_with_empty_query`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.music", 0, "Music Player")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.searchApps("music")

        // 等待搜索完成
        advanceUntilIdle()

        viewModel.searchApps("")

        // 等待清空搜索完成
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Success)
        if (state is AppListUiState.Success) {
            assertEquals("", state.searchQuery)
            assertEquals(1, state.apps.size)
        }
    }

    @Test
    fun `AppListViewModel_showAppMenu_displays_menu_state`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertTrue(initialState is AppListUiState.Success)
        val app = (initialState as AppListUiState.Success).apps[0]

        viewModel.showAppMenu(app)

        val menuState = viewModel.uiState.value
        assertTrue(menuState is AppListUiState.AppMenu)
        if (menuState is AppListUiState.AppMenu) {
            assertEquals("com.example.app", menuState.app.packageName)
        }
    }

    @Test
    fun `AppListViewModel_closeAppMenu_returns_to_list_state`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg<String>())
        }

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        val initialState = viewModel.uiState.value
        val app = (initialState as AppListUiState.Success).apps[0]
        viewModel.showAppMenu(app)

        assertTrue(viewModel.uiState.value is AppListUiState.AppMenu)

        viewModel.closeAppMenu()

        // 等待刷新完成
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is AppListUiState.Success)
    }

    @Test
    fun `AppListViewModel_openApp_succeeds_for_valid_app`() = runTest(testDispatcher) {
        val mockIntent = mockk<android.content.Intent>(relaxed = true)
        every { mockPackageManager.getLaunchIntentForPackage("com.example.app") } returns mockIntent
        every { mockIntent.addFlags(any()) } returns mockIntent

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns emptyList()

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.openApp("com.example.app")

        // 等待操作完成
        advanceUntilIdle()

        verify { mockPackageManager.getLaunchIntentForPackage("com.example.app") }
        verify { mockIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
    }

    @Test
    fun `AppListViewModel_openApp_fails_for_invalid_app`() = runTest(testDispatcher) {
        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns emptyList()
        every { mockPackageManager.getLaunchIntentForPackage("com.invalid.app") } returns null

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.openApp("com.invalid.app")

        // 等待操作完成
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Error)
        if (state is AppListUiState.Error) {
            assertEquals("无法打开该应用", state.message)
        }
    }

    @Test
    fun `AppListViewModel_showUninstallConfirm_displays_confirm_state`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg())
        }
        every { mockPackageManager.getApplicationInfo("com.example.app", PackageManager.GET_META_DATA) } returns apps[0]

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.showUninstallConfirm("com.example.app")

        // 等待操作完成
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.UninstallConfirm)
        if (state is AppListUiState.UninstallConfirm) {
            assertEquals("com.example.app", state.app.packageName)
        }
    }

    @Test
    fun `AppListViewModel_cancelUninstall_returns_to_list_state`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg())
        }
        every { mockPackageManager.getApplicationInfo("com.example.app", PackageManager.GET_META_DATA) } returns apps[0]

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.showUninstallConfirm("com.example.app")

        // 等待操作完成
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AppListUiState.UninstallConfirm)

        viewModel.cancelUninstall()

        // 等待刷新完成
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is AppListUiState.Success)
    }

    @Test
    fun `AppListViewModel_showUninstallConfirm_handles_nonexistent_app`() = runTest(testDispatcher) {
        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns emptyList()
        every { mockPackageManager.getApplicationInfo("com.nonexistent", PackageManager.GET_META_DATA) }
            .throws(PackageManager.NameNotFoundException())

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.showUninstallConfirm("com.nonexistent")

        // 等待操作完成
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Error)
        if (state is AppListUiState.Error) {
            assertEquals("应用不存在", state.message)
        }
    }

    @Test
    fun `AppListViewModel_getAppDetail_displays_detail_state`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg())
        }
        every { mockPackageManager.getApplicationInfo("com.example.app", PackageManager.GET_META_DATA) } returns apps[0]

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.getAppDetail("com.example.app")

        // 等待操作完成
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.AppDetail)
        if (state is AppListUiState.AppDetail) {
            assertEquals("com.example.app", state.app.packageName)
            assertEquals("Test App", state.app.label.toString())
        }
    }

    @Test
    fun `AppListViewModel_getAppDetail_handles_nonexistent_app`() = runTest(testDispatcher) {
        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns emptyList()
        every { mockPackageManager.getApplicationInfo("com.nonexistent", PackageManager.GET_META_DATA) }
            .throws(PackageManager.NameNotFoundException())

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.getAppDetail("com.nonexistent")

        // 等待操作完成
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AppListUiState.Error)
        if (state is AppListUiState.Error) {
            assertEquals("应用不存在", state.message)
        }
    }

    @Test
    fun `AppListViewModel_closeDetail_returns_to_list_state`() = runTest(testDispatcher) {
        val apps = listOf(
            createMockApplicationInfo("com.example.app", 0, "Test App")
        )

        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns apps
        every { mockPackageManager.getPackageInfo(any<String>(), 0) } answers {
            createMockPackageInfo(firstArg())
        }
        every { mockPackageManager.getApplicationInfo("com.example.app", PackageManager.GET_META_DATA) } returns apps[0]

        val repository = AppListRepository(mockContext)
        val viewModel = AppListViewModel(repository, mockContext)

        // 等待初始化完成
        advanceUntilIdle()

        viewModel.getAppDetail("com.example.app")

        // 等待操作完成
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AppListUiState.AppDetail)

        viewModel.closeDetail()

        // 等待刷新完成
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is AppListUiState.Success)
    }

    // 辅助函数

    private fun createMockApplicationInfo(
        packageName: String,
        flags: Int,
        label: String
    ): ApplicationInfo {
        val appInfo = ApplicationInfo().apply {
            this.packageName = packageName
            this.flags = flags
        }
        val mockAppInfo = spyk(appInfo)
        every { mockAppInfo.loadLabel(any<PackageManager>()) } returns label
        every { mockAppInfo.loadIcon(any<PackageManager>()) } returns mockk<Drawable>(relaxed = true)
        return mockAppInfo
    }

    private fun createMockPackageInfo(
        packageName: String,
        installTime: Long = System.currentTimeMillis(),
        lastUpdateTime: Long = System.currentTimeMillis()
    ): PackageInfo {
        return PackageInfo().apply {
            this.packageName = packageName
            versionName = "1.0.0"
            try {
                val firstInstallTimeField = PackageInfo::class.java.getDeclaredField("firstInstallTime")
                firstInstallTimeField.isAccessible = true
                firstInstallTimeField.setLong(this, installTime)

                val lastUpdateTimeField = PackageInfo::class.java.getDeclaredField("lastUpdateTime")
                lastUpdateTimeField.isAccessible = true
                lastUpdateTimeField.setLong(this, lastUpdateTime)

                val versionCodeField = PackageInfo::class.java.getDeclaredField("versionCode")
                versionCodeField.isAccessible = true
                versionCodeField.setInt(this, 1)
            } catch (e: Exception) {
                // 忽略反射异常
            }
        }
    }
}
