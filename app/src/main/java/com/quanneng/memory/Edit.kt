package com.quanneng.memory

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quanneng.memory.core.datastore.EditPreferences
import kotlinx.coroutines.launch

class Edit : androidx.activity.ComponentActivity() {
    private var widgetType: Int = 1

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取widgetType参数
        widgetType = intent.getIntExtra("widget_type", 1)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                EditScreen(
                    widgetType = widgetType,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(widgetType: Int, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    var textSize by remember { mutableFloatStateOf(16f) }
    var textColor by remember { mutableStateOf(Color.White) }
    var backgroundColor by remember { mutableStateOf(Color(0xDD000000.toInt())) }

    // 获取默认值
    val defaults = EditPreferences.getDefaultValues(widgetType)
    val defaultText = defaults.text
    val defaultTextSize = defaults.textSize
    val defaultTextColor = Color(defaults.textColor)
    val defaultBackgroundColor = Color(defaults.backgroundColor)

    // 获取小部件名称
    val widgetName = when (widgetType) {
        0 -> "通用 Widget"
        1 -> "质量 Widget"
        2 -> "名言 Widget"
        3 -> "备忘 Widget"
        else -> "Widget $widgetType"
    }

    // 加载保存的配置
    LaunchedEffect(Unit) {
        val prefs = EditPreferences(context, widgetType)
        val savedText = prefs.getText()
        text = if (savedText.isEmpty()) defaultText else savedText
        textSize = prefs.getTextSize().takeIf { it > 0 } ?: defaultTextSize
        textColor = Color(prefs.getTextColor().takeIf { it != 0 } ?: defaultTextColor.toArgb())
        backgroundColor = Color(prefs.getBackgroundColor().takeIf { it != 0 } ?: defaultBackgroundColor.toArgb())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑 $widgetName") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 重置按钮
                    IconButton(onClick = {
                        text = defaultText
                        textSize = defaultTextSize
                        textColor = defaultTextColor
                        backgroundColor = defaultBackgroundColor
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
                    }
                    // 保存按钮
                    IconButton(onClick = {
                        scope.launch {
                            val prefs = EditPreferences(context, widgetType)
                            prefs.saveText(text)
                            prefs.saveTextSize(textSize)
                            prefs.saveTextColor(textColor.toArgb())
                            prefs.saveBackgroundColor(backgroundColor.toArgb())

                            // 更新所有桌面小部件
                            val app = context.applicationContext as MemoryApp
                            app.container.widgetUpdater.updateAllWidgets()

                            onBackPressed()
                        }
                    }) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 预览区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text.ifEmpty { "预览文本" },
                        fontSize = textSize.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 文本输入
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("文本内容") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // 字体大小滑块
            Column {
                Text(
                    "字体大小: ${textSize.toInt()}sp",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = textSize,
                    onValueChange = { textSize = it },
                    valueRange = 12f..48f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 文字颜色选择
            Column {
                Text(
                    "文字颜色",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val colors = listOf(
                        Color.Black,
                        Color(0xFF757575),
                        Color.White,
                        Color(0xFFD32F2F),
                        Color(0xFF1976D2),
                        Color(0xFF388E3C),
                        Color(0xFFF57C00),
                        Color(0xFF7B1FA2),
                        Color(0xFFE91E63),
                        Color(0xFF00BCD4),
                        Color(0xFFFFC107),
                        Color(0xFF795548)
                    )
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .clickable { textColor = color }
                        )
                    }
                }
            }

            // 背景颜色选择
            Column {
                Text(
                    "背景颜色",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val bgColors = listOf(
                    Color.White,
                    Color(0xFFFAFAFA),
                    Color(0xFFF5F5F5),
                    Color(0x80FFFFFF.toInt()),
                    Color(0x80FAFAFA.toInt()),
                    Color(0xCC212121.toInt()),
                    Color(0x80000000.toInt()),
                    Color(0xDD000000.toInt()),
                    Color(0xFF212121),
                    Color(0xFF1976D2),
                    Color(0x801976D2.toInt()),
                    Color(0xFF388E3C),
                    Color(0x80388E3C.toInt()),
                    Color(0xFFD32F2F),
                    Color(0xFFF57C00),
                    Color(0xFF7B1FA2),
                    Color(0xFFE91E63),
                    Color(0xFF00BCD4),
                    Color(0xFFFFC107),
                    Color(0xFF3F51B5),
                    Color(0x803F51B5.toInt()),
                    Color(0xFF607D8B),
                    Color(0xFF9E9E9E),
                    Color(0xFF616161)
                )

                bgColors.chunked(9).forEach { rowColors ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .clickable { backgroundColor = color }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}