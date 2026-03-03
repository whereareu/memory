package com.quanneng.memory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        } catch (e: Exception) {
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
    onCreateShortcut: () -> Unit
) {
    val context = LocalContext.current

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
                    text = "支持4个独立小部件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            text = "3. 可选择4种不同类型的小部件",
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
            onCreateShortcut = {}
        )
    }
}