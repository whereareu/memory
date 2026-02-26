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
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                EditScreen(onBackPressed = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    var textSize by remember { mutableFloatStateOf(16f) }
    var textColor by remember { mutableStateOf(Color.White) }
    var backgroundColor by remember { mutableStateOf(Color(0xDD000000.toInt())) }

    // 默认内容
    val defaultText = "物体无受力沿着弯曲时空滑落，滑落的轨迹为最短距离，即测地线(geodesic)，过程叫做测地线运动"
    val defaultTextSize = 14f
    val defaultTextColor = Color.White
    val defaultBackgroundColor = Color(0xDD000000.toInt())

    // 加载保存的配置
    LaunchedEffect(Unit) {
        val prefs = EditPreferences(context)
        val savedText = prefs.getText()
        text = if (savedText.isEmpty()) defaultText else savedText
        textSize = prefs.getTextSize().takeIf { it > 0 } ?: defaultTextSize
        textColor = Color(prefs.getTextColor().takeIf { it != 0 } ?: defaultTextColor.toArgb())
        backgroundColor = Color(prefs.getBackgroundColor().takeIf { it != 0 } ?: defaultBackgroundColor.toArgb())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑内容") },
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
                            val prefs = EditPreferences(context)
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
                        Color.Black,                     // 黑色
                        Color(0xFF757575),               // 灰色
                        Color.White,                     // 白色
                        Color(0xFFD32F2F),               // 红色
                        Color(0xFF1976D2),               // 蓝色
                        Color(0xFF388E3C),               // 绿色
                        Color(0xFFF57C00),               // 橙色
                        Color(0xFF7B1FA2),               // 紫色
                        Color(0xFFE91E63),               // 粉色
                        Color(0xFF00BCD4),               // 青色
                        Color(0xFFFFC107),               // 琥珀色
                        Color(0xFF795548)                // 棕色
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
                // 分成两行显示，每行12个颜色
                val bgColors = listOf(
                    Color.White,                     // 纯白色
                    Color(0xFFFAFAFA),               // 浅灰色
                    Color(0xFFF5F5F5),               // 灰白色
                    Color(0x80FFFFFF.toInt()),       // 白色 50% 透明
                    Color(0x80FAFAFA.toInt()),       // 浅灰 50% 透明
                    Color(0xCC212121.toInt()),       // 黑色 80% 不透明
                    Color(0x80000000.toInt()),       // 黑色 50% 透明
                    Color(0xDD000000.toInt()),       // 黑色 87% 不透明
                    Color(0xFF212121),               // 深黑色
                    Color(0xFF1976D2),               // 蓝色
                    Color(0x801976D2.toInt()),       // 蓝色 50% 透明
                    Color(0xFF388E3C),               // 绿色
                    Color(0x80388E3C.toInt()),       // 绿色 50% 透明
                    Color(0xFFD32F2F),               // 红色
                    Color(0xFFF57C00),               // 橙色
                    Color(0xFF7B1FA2),               // 紫色
                    Color(0xFFE91E63),               // 粉色
                    Color(0xFF00BCD4),               // 青色
                    Color(0xFFFFC107),               // 琥珀色
                    Color(0xFF3F51B5),               // 靛蓝色
                    Color(0x803F51B5.toInt()),       // 靛蓝 50% 透明
                    Color(0xFF607D8B),               // 蓝灰色
                    Color(0xFF9E9E9E),               // 中灰色
                    Color(0xFF616161)                // 深灰色
                )

                // 分成3行显示，每行最多9个颜色
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