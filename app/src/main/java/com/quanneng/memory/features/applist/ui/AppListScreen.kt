package com.quanneng.memory.features.applist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.quanneng.memory.features.applist.data.AppSortType
import com.quanneng.memory.features.applist.model.AppInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 应用列表页面
 * 支持下拉刷新、搜索、应用卸载等功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: AppListViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }

    // 更新刷新状态
    LaunchedEffect(uiState) {
        isRefreshing = when (val state = uiState) {
            is AppListUiState.Success -> state.isRefreshing
            else -> false
        }
    }

    Scaffold(
        topBar = {
            AppListTopBar(
                onSearch = { query -> viewModel.searchApps(query) },
                sortType = viewModel.getCurrentSortType(),
                onSortTypeChange = { sortType -> viewModel.setSortType(sortType) }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is AppListUiState.Loading -> {
                LoadingView(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues))
            }
            is AppListUiState.Success -> {
                SwipeRefreshView(
                    apps = state.apps,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    onAppClick = { app -> viewModel.getAppDetail(app.packageName) },
                    onAppLongClick = { app -> viewModel.showAppMenu(app) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is AppListUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is AppListUiState.AppDetail -> {
                // TODO: 实现详情页面
                AppDetailView(
                    app = state.app,
                    onBack = { viewModel.closeDetail() },
                    onOpenApp = { viewModel.openApp(state.app.packageName) },
                    onUninstall = { viewModel.showUninstallConfirm(state.app.packageName) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is AppListUiState.AppMenu -> {
                // 显示操作菜单
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)) {
                    // 背景显示列表（半透明）
                    val previousState = remember { viewModel.uiState.value }
                    if (previousState is AppListUiState.Success) {
                        SwipeRefreshView(
                            apps = previousState.apps,
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refresh() },
                            onAppClick = { app -> viewModel.getAppDetail(app.packageName) },
                            onAppLongClick = { app -> viewModel.showAppMenu(app) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // 操作菜单弹窗
                    AppMenuDialog(
                        app = state.app,
                        onOpenApp = { viewModel.openApp(state.app.packageName) },
                        onShowDetail = { viewModel.getAppDetail(state.app.packageName) },
                        onShowUninstallConfirm = { viewModel.showUninstallConfirm(state.app.packageName) },
                        onDismiss = { viewModel.closeAppMenu() }
                    )
                }
            }
            is AppListUiState.UninstallConfirm -> {
                // 显示卸载确认弹窗
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)) {
                    // 背景显示列表
                    val previousState = remember { viewModel.uiState.value }
                    if (previousState is AppListUiState.Success) {
                        SwipeRefreshView(
                            apps = previousState.apps,
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refresh() },
                            onAppClick = { app -> viewModel.getAppDetail(app.packageName) },
                            onAppLongClick = { app -> viewModel.showAppMenu(app) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // 卸载确认弹窗
                    UninstallConfirmDialog(
                        app = state.app,
                        onConfirm = { viewModel.uninstallApp(state.app.packageName) },
                        onDismiss = { viewModel.cancelUninstall() }
                    )
                }
            }
        }
    }
}

/**
 * 应用列表顶部栏
 * 支持搜索功能和排序选项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppListTopBar(
    onSearch: (String) -> Unit,
    sortType: AppSortType,
    onSortTypeChange: (AppSortType) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    if (showSearchBar) {
        // 搜索模式
        TopAppBar(
            title = {
                TextField(
                    value = searchText,
                    onValueChange = { newText ->
                        searchText = newText
                        onSearch(newText)
                    },
                    placeholder = { Text("搜索应用...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    showSearchBar = false
                    searchText = ""
                    onSearch("")
                }) {
                    Text("←")
                }
            },
            actions = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = {
                        searchText = ""
                        onSearch("")
                    }) {
                        Text("×")
                    }
                }
            },
            modifier = modifier
        )
    } else {
        // 普通模式
        TopAppBar(
            title = { Text("应用列表") },
            actions = {
                // 排序按钮
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Text("排序")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        val sortOptions = mapOf(
                            AppSortType.BY_NAME to "按名称",
                            AppSortType.BY_INSTALL_TIME to "按安装时间",
                            AppSortType.BY_UPDATE_TIME to "按更新时间"
                        )
                        sortOptions.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onSortTypeChange(type)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (type == sortType) {
                                        Text("✓")
                                    }
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = { showSearchBar = true }) {
                    Text("搜索")
                }
            },
            modifier = modifier
        )
    }
}

/**
 * 下拉刷新视图 - 网格布局
 */
@Composable
private fun SwipeRefreshView(
    apps: List<AppInfo>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 刷新指示器
            if (isRefreshing) {
                item(span = { GridItemSpan(3) }) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }
            }

            // 应用网格
            items(apps) { app ->
                AppGridItem(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }
    }
}

/**
 * 应用网格项
 */
@Composable
private fun AppGridItem(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .then(
                    Modifier.clickable(
                        onClick = onLongClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 应用图标
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(app.icon)
                    .crossfade(true)
                    .build(),
                contentDescription = app.label.toString(),
                modifier = Modifier.size(56.dp)
            )

            // 应用名称
            Text(
                text = app.label.toString(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * 应用列表项（保留用于其他可能的用途）
 */
@Composable
private fun AppListItem(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 应用图标
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(app.icon)
                    .crossfade(true)
                    .build(),
                contentDescription = app.label.toString(),
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 12.dp)
            )

            // 应用信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = app.label.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "版本: ${app.versionName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 系统应用标识
            if (app.isSystemApp) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "系统",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 加载视图
 */
@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("加载应用列表...")
        }
    }
}

/**
 * 错误视图
 */
@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

/**
 * 应用详情视图
 */
@Composable
private fun AppDetailView(
    app: AppInfo,
    onBack: () -> Unit,
    onOpenApp: () -> Unit,
    onUninstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Text("←")
            }
            Text("应用详情", style = MaterialTheme.typography.headlineMedium)
        }

        // 应用图标和基本信息
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(app.icon)
                    .crossfade(true)
                    .build(),
                contentDescription = app.label.toString(),
                modifier = Modifier.size(96.dp)
            )

            Text(
                text = app.label.toString(),
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // 系统应用标识
            if (app.isSystemApp) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "系统应用",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 详细信息卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "详细信息",
                    style = MaterialTheme.typography.titleMedium
                )

                HorizontalDivider()

                DetailRow("版本名称", app.versionName ?: "未知")
                DetailRow("版本号", app.versionCode.toString())
                DetailRow("安装时间", dateFormat.format(Date(app.installTime)))
                DetailRow("更新时间", dateFormat.format(Date(app.lastUpdateTime)))
                DetailRow("系统应用", if (app.isSystemApp) "是" else "否")
                DetailRow("应用标志", "0x${app.flags.toString(16).uppercase()}")
            }
        }

        // 操作按钮
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenApp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("打开应用")
            }

            if (!app.isSystemApp) {
                OutlinedButton(
                    onClick = onUninstall,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("卸载应用")
                }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回列表")
            }
        }
    }
}

/**
 * 详情行组件
 */
@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 应用操作菜单弹窗
 */
@Composable
private fun AppMenuDialog(
    app: AppInfo,
    onOpenApp: () -> Unit,
    onShowDetail: () -> Unit,
    onShowUninstallConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(app.icon)
                        .crossfade(true)
                        .build(),
                    contentDescription = app.label.toString(),
                    modifier = Modifier.size(32.dp)
                )
                Text(app.label.toString())
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                Text("版本: ${app.versionName}", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {},
        dismissButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        onOpenApp()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("打开应用")
                }
                OutlinedButton(
                    onClick = {
                        onShowDetail()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用详情")
                }
                if (!app.isSystemApp) {
                    OutlinedButton(
                        onClick = {
                            onShowUninstallConfirm()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("卸载应用")
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    )
}

/**
 * 卸载确认弹窗
 */
@Composable
private fun UninstallConfirmDialog(
    app: AppInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("确认卸载")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(app.icon)
                            .crossfade(true)
                            .build(),
                        contentDescription = app.label.toString(),
                        modifier = Modifier.size(48.dp)
                    )
                    Column {
                        Text(
                            text = app.label.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                Text(
                    text = "确定要卸载此应用吗？卸载后将删除该应用及其所有数据。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("卸载")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
