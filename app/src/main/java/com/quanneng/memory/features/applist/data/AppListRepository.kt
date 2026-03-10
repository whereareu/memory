package com.quanneng.memory.features.applist.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.quanneng.memory.core.dispatchers.IoDispatcher
import com.quanneng.memory.features.applist.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 应用列表数据仓库
 * 负责获取、过滤和管理已安装应用信息
 */
class AppListRepository(
    private val context: Context
) {
    private val packageManager: PackageManager
        get() = context.packageManager

    /**
     * 获取所有已安装应用
     * @param includeSystemApps 是否包含系统应用
     * @return 应用列表
     */
    @IoDispatcher
    suspend fun getAllApps(includeSystemApps: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        packages.mapNotNull { packageInfo ->
            try {
                packageInfo.toAppInfo(packageManager)
            } catch (e: Exception) {
                null
            }
        }.filter { appInfo ->
            includeSystemApps || !appInfo.isSystemApp
        }.sortedBy { it.label.toString().lowercase() }
    }

    /**
     * 获取应用详情
     * @param packageName 包名
     * @return 应用信息，未找到返回 null
     */
    @IoDispatcher
    suspend fun getAppByPackage(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            applicationInfo.toAppInfo(packageManager)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * 打开应用
     * @param packageName 要打开的包名
     * @return 是否成功启动应用
     */
    @IoDispatcher
    suspend fun openApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 卸载应用
     * 使用 ACTION_DELETE Intent 启动系统卸载流程
     * 适用于所有 Android API 版本，无需特殊权限声明
     * @param packageName 要卸载的包名
     * @return 是否成功启动卸载流程
     */
    @IoDispatcher
    suspend fun uninstallApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 验证应用是否存在
            packageManager.getPackageInfo(packageName, 0)

            // 创建卸载 Intent
            // ACTION_DELETE 适用于所有 Android 版本
            // 系统会弹出确认对话框，用户确认后执行卸载
            val intent = android.content.Intent(android.content.Intent.ACTION_DELETE).apply {
                data = android.net.Uri.parse("package:$packageName")
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            // 应用不存在
            false
        } catch (e: Exception) {
            // 其他错误（如 Activity 不存在、权限问题等）
            false
        }
    }

    /**
     * 搜索应用
     * @param query 搜索关键词
     * @param includeSystemApps 是否包含系统应用
     * @return 匹配的应用列表
     */
    @IoDispatcher
    suspend fun searchApps(query: String, includeSystemApps: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        val allApps = getAllApps(includeSystemApps)
        if (query.isBlank()) {
            allApps
        } else {
            val lowerQuery = query.lowercase()
            allApps.filter { appInfo ->
                appInfo.label.toString().lowercase().contains(lowerQuery) ||
                appInfo.packageName.lowercase().contains(lowerQuery)
            }
        }
    }

    /**
     * 获取系统应用数量
     */
    @IoDispatcher
    suspend fun getSystemAppsCount(): Int = withContext(Dispatchers.IO) {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .count { it.flags and ApplicationInfo.FLAG_SYSTEM != 0 }
    }

    /**
     * 获取用户应用数量
     */
    @IoDispatcher
    suspend fun getUserAppsCount(): Int = withContext(Dispatchers.IO) {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .count { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
    }
}

/**
 * 扩展函数：将 ApplicationInfo 转换为 AppInfo
 */
private fun ApplicationInfo.toAppInfo(packageManager: PackageManager): AppInfo? {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        AppInfo(
            packageName = packageName,
            label = loadLabel(packageManager),
            icon = try {
                loadIcon(packageManager)
            } catch (e: Exception) {
                null
            },
            versionName = packageInfo.versionName,
            versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            },
            installTime = packageInfo.firstInstallTime,
            lastUpdateTime = packageInfo.lastUpdateTime,
            isSystemApp = flags and ApplicationInfo.FLAG_SYSTEM != 0,
            flags = flags
        )
    } catch (e: Exception) {
        null
    }
}
