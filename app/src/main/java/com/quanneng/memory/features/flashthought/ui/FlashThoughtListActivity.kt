package com.quanneng.memory.features.flashthought.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quanneng.memory.MemoryApp
import com.quanneng.memory.features.flashthought.model.FlashThought
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 闪现历史记录列表界面
 * 展示所有闪现，支持搜索、删除、分享、编辑
 */
class FlashThoughtListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                FlashThoughtListScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashThoughtListScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedThought by remember { mutableStateOf<FlashThought?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val repository = (context.applicationContext as MemoryApp)
        .container.flashThoughtRepository

    val thoughts = produceState(
        initialValue = emptyList(),
        key1 = searchQuery
    ) {
        if (searchQuery.isBlank()) {
            repository.getAllThoughts().collect { value = it }
        } else {
            repository.searchThoughts(searchQuery).collect { value = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("闪现记录") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 快速添加按钮
                    IconButton(onClick = {
                        val intent = Intent(context, FlashThoughtQuickAddActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "添加闪现")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索闪现...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true
            )

            // 闪现列表
            if (thoughts.value.isEmpty()) {
                EmptyState(
                    onQuickAdd = {
                        context.startActivity(
                            Intent(context, FlashThoughtQuickAddActivity::class.java)
                        )
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = thoughts.value,
                        key = { it.id }
                    ) { thought ->
                        ThoughtItem(
                            thought = thought,
                            onClick = { },
                            onEdit = { /* TODO: 编辑功能 */ },
                            onDelete = {
                                selectedThought = thought
                                showDeleteDialog = true
                            },
                            onShare = { shareThought(context, thought) },
                            onCopy = { copyThought(context, thought) },
                            onTogglePin = {
                                scope.launch {
                                    repository.togglePin(thought.id)
                                    // 刷新所有闪现 Widget
                                    val app = context.applicationContext as MemoryApp
                                    app.container.widgetUpdater.updateFlashThoughtWidgets()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && selectedThought != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除闪现") },
            text = { Text("确定要删除这条闪现吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            selectedThought?.id?.let { repository.deleteThought(it) }
                            // 刷新所有闪现 Widget
                            val app = context.applicationContext as MemoryApp
                            app.container.widgetUpdater.updateFlashThoughtWidgets()
                            showDeleteDialog = false
                            selectedThought = null
                        }
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun ThoughtItem(
    thought: FlashThought,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 头部：时间和标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(thought.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (thought.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "置顶",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 内容
            Text(
                text = thought.content,
                style = MaterialTheme.typography.bodyLarge
            )

            // 标签
            if (thought.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    thought.tags.split(",").forEach { tag ->
                        if (tag.isNotBlank()) {
                            SuggestionChip(
                                onClick = { },
                                label = { Text(tag.trim()) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 下拉菜单
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text(if (thought.isPinned) "取消置顶" else "置顶") },
            onClick = {
                onTogglePin()
                showMenu = false
            },
            leadingIcon = {
                Icon(Icons.Default.PushPin, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("复制") },
            onClick = {
                onCopy()
                showMenu = false
            },
            leadingIcon = {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("分享") },
            onClick = {
                onShare()
                showMenu = false
            },
            leadingIcon = {
                Icon(Icons.Default.Share, contentDescription = null)
            }
        )
        Divider()
        DropdownMenuItem(
            text = { Text("删除") },
            onClick = {
                onDelete()
                showMenu = false
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            colors = MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.error
            )
        )
    }
}

@Composable
fun EmptyState(
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "还没有闪现记录",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "点击下面的按钮快速添加你的第一个闪现",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            FilledTonalButton(onClick = onQuickAdd) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("快速添加")
            }
        }
    }
}

// 辅助函数
private fun formatTime(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

private fun shareThought(context: Context, thought: FlashThought) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, thought.content)
    }
    context.startActivity(Intent.createChooser(intent, "分享闪现"))
}

private fun copyThought(context: Context, thought: FlashThought) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("闪现", thought.content)
    clipboard.setPrimaryClip(clip)
}
