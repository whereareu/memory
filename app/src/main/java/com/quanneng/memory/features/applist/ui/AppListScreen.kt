package com.quanneng.memory.features.applist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.quanneng.memory.features.applist.model.AppInfo

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
                onToggleSystemApps = { viewModel.toggleSystemApps() }
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
                    onAppLongClick = { app -> viewModel.uninstallApp(app.packageName) },
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

/**
 * 应用列表顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppListTopBar(
    onSearch: (String) -> Unit,
    onToggleSystemApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }

    TopAppBar(
        title = { Text("应用列表") },
        actions = {
            IconButton(onClick = onToggleSystemApps) {
                Text("显示系统应用")
            }
        },
        modifier = modifier
    )
}

/**
 * 下拉刷新视图
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
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 刷新指示器
            if (isRefreshing) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }
            }

            // 应用列表
            items(apps) { app ->
                AppListItem(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }
    }
}

/**
 * 应用列表项
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("应用详情", style = MaterialTheme.typography.headlineMedium)

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(app.icon)
                .crossfade(true)
                .build(),
            contentDescription = app.label.toString(),
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text("应用名称: ${app.label}")
        Text("包名: ${app.packageName}")
        Text("版本: ${app.versionName} (${app.versionCode})")
        Text("安装时间: ${app.installTime}")
        Text("更新时间: ${app.lastUpdateTime}")
        Text("系统应用: ${if (app.isSystemApp) "是" else "否"}")

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回")
        }
    }
}
