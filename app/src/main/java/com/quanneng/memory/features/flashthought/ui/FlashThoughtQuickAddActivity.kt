package com.quanneng.memory.features.flashthought.ui

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quanneng.memory.MemoryApp
import com.quanneng.memory.features.flashthought.widget.FlashThoughtWidget
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 闪现快速添加界面
 * 提供简洁的输入体验，支持语音输入
 */
class FlashThoughtQuickAddActivity : ComponentActivity() {

    private var widgetId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        widgetId = intent.getIntExtra(FlashThoughtWidget.EXTRA_WIDGET_ID, 0)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                QuickAddScreen(
                    onBackPressed = { finish() },
                    onSaved = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddScreen(
    onBackPressed: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    // 语音识别启动器
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { text ->
            content = text
        }
    }

    // 启动语音识别
    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出你的想法")
        }
        speechRecognizerLauncher.launch(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("快速添加闪现") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 语音输入按钮
                    IconButton(onClick = { startVoiceInput() }) {
                        Icon(Icons.Default.Mic, contentDescription = "语音输入")
                    }
                    // 保存按钮
                    IconButton(
                        onClick = {
                            scope.launch {
                                val repository = (context.applicationContext as MemoryApp)
                                    .container.flashThoughtRepository
                                if (content.isNotBlank()) {
                                    // 先保存数据
                                    repository.addThought(content, tags)
                                    // 直接调用 WidgetUpdater 刷新所有闪现 Widget
                                    val app = context.applicationContext as MemoryApp
                                    app.container.widgetUpdater.updateFlashThoughtWidgets()
                                    onSaved()
                                }
                            }
                        },
                        enabled = content.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 提示文本
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "记录此刻的想法和灵感",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 内容输入
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("闪现内容") },
                placeholder = { Text("在此输入你的想法...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 5,
                maxLines = 10
            )

            // 标签输入
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("标签（可选）") },
                placeholder = { Text("用逗号分隔多个标签") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 快捷操作提示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "快捷操作",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "• 点击右上角麦克风图标可使用语音输入",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• 输入完成后点击保存按钮",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
