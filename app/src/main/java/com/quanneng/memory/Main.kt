package com.quanneng.memory

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.quanneng.memory.R

class Main : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                MainScreen(
                    onEditWidget0 = { editWidget(0) },
                    onEditWidget1 = { editWidget(1) },
                    onEditWidget2 = { editWidget(2) },
                    onEditWidget3 = { editWidget(3) },
                    onEditWidget4 = { editWidget(4) },
                    onFlashThoughtList = { openFlashThoughtList() },
                    onOpenAppList = { openAppList() },
                    onOpenDailyQuestion = { openDailyQuestion() },
                    onOpenArticleList = { openArticleList() },
                    onCreateShortcut = { createShortcut() }
                )
            }
        }
    }

    private fun editWidget(widgetType: Int) {
        startActivity(Intent(this, Edit::class.java).apply {
            putExtra("widget_type", widgetType)
        })
    }

    private fun openFlashThoughtList() {
        startActivity(Intent(this, com.quanneng.memory.features.flashthought.ui.FlashThoughtListActivity::class.java))
    }

    private fun openAppList() {
        startActivity(Intent(this, com.quanneng.memory.features.applist.ui.AppListActivity::class.java))
    }

    private fun openDailyQuestion() {}

    private fun openArticleList() {
        startActivity(Intent(this, com.quanneng.memory.features.articles.ui.ArticleListActivity::class.java))
    }

    private fun createShortcut() {
        val intent = Intent(this, Main::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val icon = IconCompat.createWithResource(this, R.drawable.ic_launcher_foreground)

        val shortcutInfo = ShortcutInfoCompat.Builder(this, "memory_main_shortcut")
            .setShortLabel("Memory")
            .setLongLabel("Memory - 小部件管理")
            .setIcon(icon)
            .setIntent(intent)
            .build()

        try {
            ShortcutManagerCompat.requestPinShortcut(this, shortcutInfo, null)
        } catch (_: Exception) {
            // 某些设备可能不支持
        }
    }
}

@Composable
fun MainScreen(
    onEditWidget0: () -> Unit,
    onEditWidget1: () -> Unit,
    onEditWidget2: () -> Unit,
    onEditWidget3: () -> Unit,
    onEditWidget4: () -> Unit,
    onFlashThoughtList: () -> Unit,
    onOpenAppList: () -> Unit,
    onOpenDailyQuestion: () -> Unit,
    onOpenArticleList: () -> Unit,
    onCreateShortcut: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "欢迎使用 Memory",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "支持6个独立小部件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 快速访问区域
                Text(
                    text = "快速跳转",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAccessItem(
                        icon = Icons.Default.Edit,
                        label = "通用",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onEditWidget0
                    )
                    QuickAccessItem(
                        icon = Icons.Default.Star,
                        label = "质量",
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = onEditWidget1
                    )
                    QuickAccessItem(
                        icon = Icons.Default.Info,
                        label = "名言",
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = onEditWidget2
                    )
                    QuickAccessItem(
                        icon = Icons.Default.DateRange,
                        label = "日期计数",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onEditWidget3
                    )
                    QuickAccessItem(
                        icon = Icons.Default.Notifications,
                        label = "倒计时",
                        color = MaterialTheme.colorScheme.errorContainer,
                        onClick = onEditWidget4
                    )
                    QuickAccessItem(
                        icon = Icons.Default.Lightbulb,
                        label = "闪现",
                        color = Color(0xFFFFB74D),
                        onClick = onFlashThoughtList
                    )
                    QuickAccessItem(
                        icon = Icons.Default.Apps,
                        label = "应用",
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = onOpenAppList
                    )
                    QuickAccessItem(
                        icon = Icons.Default.MenuBook,
                        label = "每日一问",
                        color = Color(0xFF9575CD),
                        onClick = onOpenDailyQuestion
                    )
                    QuickAccessItem(
                        icon = Icons.Default.Article,
                        label = "文章",
                        color = Color(0xFF4DB6AC),
                        onClick = onOpenArticleList
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 添加桌面快捷方式按钮
                FilledTonalButton(
                    onClick = onCreateShortcut,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加桌面快捷方式")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4个小部件编辑按钮
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "编辑小部件",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )

                        Button(
                            onClick = onEditWidget0,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("通用 Widget")
                        }

                        Button(
                            onClick = onEditWidget1,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("质量 Widget")
                        }

                        Button(
                            onClick = onEditWidget2,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("名言 Widget")
                        }

                        Button(
                            onClick = onEditWidget3,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("日期计数器 Widget")
                        }

                        Button(
                            onClick = onEditWidget4,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("倒计时 Widget")
                        }

                        Button(
                            onClick = onFlashThoughtList,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("闪现 Widget")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "使用说明",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = "1. 长按应用图标可快速访问小部件",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "2. 点击上方按钮添加桌面快捷方式",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "3. 可选择6种不同类型的小部件",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "4. 每种小部件配置独立保存",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        MainScreen(
            onEditWidget0 = {},
            onEditWidget1 = {},
            onEditWidget2 = {},
            onEditWidget3 = {},
            onEditWidget4 = {},
            onFlashThoughtList = {},
            onOpenAppList = {},
            onOpenDailyQuestion = {},
            onOpenArticleList = {},
            onCreateShortcut = {}
        )
    }
}

/**
 * 快速访问入口项
 */
@Composable
private fun QuickAccessItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}