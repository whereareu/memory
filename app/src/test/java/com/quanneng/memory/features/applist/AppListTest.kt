package com.quanneng.memory.features.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.quanneng.memory.features.applist.data.AppListRepository
import com.quanneng.memory.features.applist.model.AppInfo
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
