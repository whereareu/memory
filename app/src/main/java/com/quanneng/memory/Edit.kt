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
import androidx.compose.material.icons.filled.DateRange
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
import com.quanneng.memory.core.datastore.DateCounterPreferences
import com.quanneng.memory.core.datastore.EditPreferences
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                // 根据 widgetType 显示不同的编辑界面
                if (widgetType == 3) {
                    DateCounterEditScreen(
                        widgetType = widgetType,
                        onBackPressed = { finish() }
                    )
                } else {
                    EditScreen(
                        widgetType = widgetType,
                        onBackPressed = { finish() }
                    )
                }
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
        3 -> "日期计数器"
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

/**
 * 日期计数器编辑界面
 * 用于 widgetType = 3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCounterEditScreen(
    widgetType: Int = 3,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 状态
    var title by remember { mutableStateOf("开始日期") }
    var targetDate by remember { mutableStateOf(LocalDate.now()) }
    var titleSize by remember { mutableFloatStateOf(14f) }
    var dateSize by remember { mutableFloatStateOf(16f) }
    var daysSize by remember { mutableFloatStateOf(24f) }
    var titleColor by remember { mutableStateOf(Color(0xFFFFFFFF.toInt())) }
    var dateColor by remember { mutableStateOf(Color(0xFFFFFFFF.toInt())) }
    var daysColor by remember { mutableStateOf(Color(0xFF4FC3F7.toInt())) }
    var backgroundColor by remember { mutableStateOf(Color(0xDD000000.toInt())) }

    // 日期选择器状态
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    // 加载保存的全局配置
    LaunchedEffect(Unit) {
        val prefs = DateCounterPreferences(
            context,
            com.quanneng.memory.core.dispatchers.DispatcherProvider()
        )
        title = prefs.getGlobalTitle()
        targetDate = prefs.getGlobalTargetDate()
        titleSize = prefs.getGlobalTitleSize()
        dateSize = prefs.getGlobalDateSize()
        daysSize = prefs.getGlobalDaysSize()
        titleColor = Color(prefs.getGlobalTitleColor())
        dateColor = Color(prefs.getGlobalDateColor())
        daysColor = Color(prefs.getGlobalDaysColor())
        backgroundColor = Color(prefs.getGlobalBackgroundColor())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日期计数器") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 重置按钮
                    IconButton(onClick = {
                        title = "开始日期"
                        targetDate = LocalDate.now()
                        titleSize = 14f
                        dateSize = 16f
                        daysSize = 24f
                        titleColor = Color(0xFFFFFFFF.toInt())
                        dateColor = Color(0xFFFFFFFF.toInt())
                        daysColor = Color(0xFF4FC3F7.toInt())
                        backgroundColor = Color(0xDD000000.toInt())
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
                    }
                    // 保存按钮
                    IconButton(onClick = {
                        scope.launch {
                            // 保存全局配置
                            val prefs = DateCounterPreferences(
                                context,
                                com.quanneng.memory.core.dispatchers.DispatcherProvider()
                            )
                            prefs.saveGlobalTitle(title)
                            prefs.saveGlobalTargetDate(targetDate)
                            prefs.saveGlobalTitleSize(titleSize)
                            prefs.saveGlobalDateSize(dateSize)
                            prefs.saveGlobalDaysSize(daysSize)
                            prefs.saveGlobalTitleColor(titleColor.toArgb())
                            prefs.saveGlobalDateColor(dateColor.toArgb())
                            prefs.saveGlobalDaysColor(daysColor.toArgb())
                            prefs.saveGlobalBackgroundColor(backgroundColor.toArgb())

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
                .verticalScroll(rememberScrollState())
        ) {
            // 预览区域 - 更大更突出
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题
                    Text(
                        text = title.ifEmpty { "标题" },
                        fontSize = titleSize.sp,
                        color = titleColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // 目标日期
                    Text(
                        text = targetDate.format(dateFormatter),
                        fontSize = dateSize.sp,
                        color = dateColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // 经过天数
                    val elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(
                        targetDate,
                        LocalDate.now()
                    ).toInt()
                    Text(
                        text = "已过 $elapsedDays 天",
                        fontSize = daysSize.sp,
                        color = daysColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 基础信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "基础设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // 标题输入
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("标题") },
                        placeholder = { Text("例如：恋爱纪念日") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 日期选择
                    OutlinedTextField(
                        value = targetDate.format(dateFormatter),
                        onValueChange = {},
                        label = { Text("开始日期") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                            }
                        }
                    )
                }
            }

            // 字体大小卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "字体大小",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // 标题大小
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "标题",
                            modifier = Modifier.width(60.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = titleSize,
                            onValueChange = { titleSize = it },
                            valueRange = 10f..24f,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${titleSize.toInt()}",
                            modifier = Modifier.width(30.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // 日期大小
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "日期",
                            modifier = Modifier.width(60.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = dateSize,
                            onValueChange = { dateSize = it },
                            valueRange = 12f..32f,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${dateSize.toInt()}",
                            modifier = Modifier.width(30.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // 天数大小
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "天数",
                            modifier = Modifier.width(60.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = daysSize,
                            onValueChange = { daysSize = it },
                            valueRange = 16f..48f,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${daysSize.toInt()}",
                            modifier = Modifier.width(30.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 颜色设置卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "颜色设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // 标题颜色
                    Column {
                        Text(
                            "标题",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val colors = listOf(
                                Color.White, Color(0xFFE0E0E0), Color(0xFFBDBDBD),
                                Color(0xFF4FC3F7), Color(0xFF1976D2), Color(0xFF388E3C),
                                Color(0xFFF57C00), Color(0xFFD32F2F), Color(0xFFE91E63)
                            )
                            colors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(color)
                                        .clickable { titleColor = color }
                                        .then(
                                            if (titleColor == color)
                                                Modifier.background(Color.White.copy(alpha = 0.3f))
                                            else Modifier
                                        )
                                )
                            }
                        }
                    }

                    // 天数颜色
                    Column {
                        Text(
                            "天数",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val colors = listOf(
                                Color(0xFF4FC3F7), Color(0xFF29B6F6), Color(0xFF03A9F4),
                                Color(0xFF0288D1), Color(0xFFD32F2F), Color(0xFFFF5722),
                                Color(0xFFFFC107), Color(0xFF8BC34A), Color(0xFFE91E63)
                            )
                            colors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(color)
                                        .clickable { daysColor = color }
                                        .then(
                                            if (daysColor == color)
                                                Modifier.background(Color.White.copy(alpha = 0.3f))
                                            else Modifier
                                        )
                                )
                            }
                        }
                    }

                    // 背景颜色
                    Column {
                        Text(
                            "背景",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val bgColors = listOf(
                                listOf(
                                    Color(0xDD000000.toInt()), Color(0xCC000000.toInt()), Color(0x99000000.toInt()),
                                    Color(0x66000000.toInt()), Color(0xFF212121), Color(0xFF424242),
                                    Color(0xFF616161), Color(0xFF757575), Color(0xFF9E9E9E)
                                ),
                                listOf(
                                    Color(0xFF1976D2), Color(0x801976D2.toInt()), Color(0xFF388E3C),
                                    Color(0x80388E3C.toInt()), Color(0xFFD32F2F), Color(0x80D32F2F.toInt()),
                                    Color(0xFFF57C00), Color(0xFF7B1FA2), Color(0xFF00BCD4)
                                )
                            )

                            bgColors.forEach { rowColors ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowColors.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(color)
                                                .clickable { backgroundColor = color }
                                                .then(
                                                    if (backgroundColor == color)
                                                        Modifier.background(Color.White.copy(alpha = 0.3f))
                                                    else Modifier
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 日期选择器对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDateSelected = { millis ->
                millis?.let {
                    targetDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 日期选择器对话框
 */
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(null)
            }) {
                Text("取消")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDateSelected(System.currentTimeMillis())
            }) {
                Text("确定")
            }
        },
        text = { content() }
    )
}