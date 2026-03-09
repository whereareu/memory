package com.quanneng.memory

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quanneng.memory.core.datastore.CountdownPreferences
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
                when (widgetType) {
                    3 -> DateCounterEditScreen(
                        widgetType = widgetType,
                        onBackPressed = { finish() }
                    )
                    4 -> CountdownEditScreen(
                        widgetType = widgetType,
                        onBackPressed = { finish() }
                    )
                    else -> EditScreen(
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
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

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
        DatePickerDialog(
            selectedDateMillis = targetDate.toEpochDay() * 24 * 60 * 60 * 1000,
            onDateSelected = { millis ->
                millis?.let {
                    targetDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * 日期选择器对话框
 * 使用滚轮式选择器，性能更好
 * @param selectedDateMillis 当前选中的日期（毫秒时间戳）
 * @param onDateSelected 日期选择回调，参数为选中的日期毫秒时间戳，null 表示取消
 * @param onDismiss 取消回调
 */
@Composable
fun DatePickerDialog(
    selectedDateMillis: Long? = null,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val initialDate = selectedDateMillis?.let {
        LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
    } ?: LocalDate.now()

    var selectedYear by remember { mutableIntStateOf(initialDate.year) }
    var selectedMonth by remember { mutableIntStateOf(initialDate.monthValue) }
    var selectedDay by remember { mutableIntStateOf(initialDate.dayOfMonth) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "选择日期",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 年份选择器
                    WheelPicker(
                        items = (2000..2030).map { "${it}年" },
                        initialIndex = (selectedYear - 2000).coerceIn(0, 30),
                        onSelectedChange = { index -> selectedYear = 2000 + index }
                    )

                    // 月份选择器
                    WheelPicker(
                        items = (1..12).map { "${it}月" },
                        initialIndex = selectedMonth - 1,
                        onSelectedChange = { index -> selectedMonth = index + 1 }
                    )

                    // 日期选择器
                    val maxDay = remember(selectedYear, selectedMonth) {
                        when (selectedMonth) {
                            2 -> if (selectedYear % 4 == 0 && (selectedYear % 100 != 0 || selectedYear % 400 == 0)) 29 else 28
                            4, 6, 9, 11 -> 30
                            else -> 31
                        }
                    }
                    val adjustedDay = selectedDay.coerceAtMost(maxDay)
                    if (adjustedDay != selectedDay) {
                        LaunchedEffect(adjustedDay) {
                            selectedDay = adjustedDay
                        }
                    }

                    WheelPicker(
                        items = (1..maxDay).map { "${it}日" },
                        initialIndex = (adjustedDay - 1).coerceIn(0, maxDay - 1),
                        onSelectedChange = { index -> selectedDay = index + 1 }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDateSelected(null) }) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val resultDate = LocalDate.of(selectedYear, selectedMonth, selectedDay)
                        onDateSelected(resultDate.toEpochDay() * 24 * 60 * 60 * 1000)
                    }) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

/**
 * 滚轮选择器
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onSelectedChange: (Int) -> Unit
) {
    val itemHeight = 44.dp
    val containerHeight = 150.dp
    val density = androidx.compose.ui.platform.LocalDensity.current

    // 初始位置：让目标项显示在中间
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = 0  // 从0开始，让 LaunchedEffect 来精确定位
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // 用于标记初始化滚动是否完成
    var isInitialized by remember { mutableStateOf(false) }

    // 滚动到初始位置（居中）- 只在首次加载时执行
    LaunchedEffect(Unit) {
        val offsetPx = with(density) { ((containerHeight - itemHeight) / 2).roundToPx() }
        // initialIndex + 1 是因为顶部有一个占位 item
        listState.scrollToItem(initialIndex + 1, -offsetPx)
        isInitialized = true
    }

    // 计算当前选中项（中间位置）
    // 由于顶部有一个占位 item，所以实际的 data index = lazyList index - 1
    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val containerCenter = layoutInfo.viewportStartOffset +
                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2

            // 找到最接近中心的项目
            val centerItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                kotlin.math.abs(itemCenter - containerCenter)
            }

            // 减去顶部占位项，得到实际数据索引
            val dataItemIndex = (centerItem?.index ?: 1) - 1
            // 确保索引在有效范围内（过滤掉顶部和底部占位项）
            when {
                dataItemIndex < 0 -> 0
                dataItemIndex >= items.size -> items.size - 1
                else -> dataItemIndex
            }
        }
    }

    // 只在初始化完成后通知选中变化
    LaunchedEffect(selectedIndex, isInitialized) {
        if (isInitialized) {
            onSelectedChange(selectedIndex)
        }
    }

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(containerHeight)
    ) {
        // 选中行高亮（中间位置）
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部占位
            item {
                Spacer(modifier = Modifier.height((containerHeight - itemHeight) / 2))
            }

            items(items.size) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = if (index == selectedIndex)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // 底部占位
            item {
                Spacer(modifier = Modifier.height((containerHeight - itemHeight) / 2))
            }
        }
    }
}

/**
 * 倒计时编辑界面
 * 用于 widgetType = 4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownEditScreen(
    widgetType: Int = 4,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 状态
    var title by remember { mutableStateOf("目标日期") }
    var targetDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }
    var titleSize by remember { mutableFloatStateOf(14f) }
    var dateSize by remember { mutableFloatStateOf(16f) }
    var daysSize by remember { mutableFloatStateOf(24f) }
    var titleColor by remember { mutableStateOf(Color(0xFFFFFFFF.toInt())) }
    var dateColor by remember { mutableStateOf(Color(0xFFFFFFFF.toInt())) }
    var daysColor by remember { mutableStateOf(Color(0xFFFF5722.toInt())) }
    var backgroundColor by remember { mutableStateOf(Color(0xDD000000.toInt())) }

    // 日期选择器状态
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    // 加载保存的全局配置
    LaunchedEffect(Unit) {
        val prefs = CountdownPreferences(
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
                title = { Text("倒计时") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 重置按钮
                    IconButton(onClick = {
                        title = "目标日期"
                        targetDate = LocalDate.now().plusDays(30)
                        titleSize = 14f
                        dateSize = 16f
                        daysSize = 24f
                        titleColor = Color(0xFFFFFFFF.toInt())
                        dateColor = Color(0xFFFFFFFF.toInt())
                        daysColor = Color(0xFFFF5722.toInt())
                        backgroundColor = Color(0xDD000000.toInt())
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
                    }
                    // 保存按钮
                    IconButton(onClick = {
                        scope.launch {
                            // 保存全局配置
                            val prefs = CountdownPreferences(
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
                    // 剩余天数
                    val remainingDays = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        targetDate
                    ).toInt()
                    Text(
                        text = if (remainingDays >= 0) "剩余 $remainingDays 天" else "已过期 ${-remainingDays} 天",
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
                        placeholder = { Text("例如：考试倒计时") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 日期选择
                    OutlinedTextField(
                        value = targetDate.format(dateFormatter),
                        onValueChange = {},
                        label = { Text("目标日期") },
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
                                Color(0xFFFF5722), Color(0xFF1976D2), Color(0xFF388E3C),
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
                                Color(0xFFFF5722), Color(0xFFFF7043), Color(0xFFFF8A65),
                                Color(0xFFD32F2F), Color(0xFFE91E63), Color(0xFF9C27B0),
                                Color(0xFFFFC107), Color(0xFF4FC3F7), Color(0xFF00BCD4)
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
                                    Color(0xFFFF5722), Color(0x80FF5722.toInt()), Color(0xFF1976D2),
                                    Color(0x801976D2.toInt()), Color(0xFFD32F2F), Color(0x80D32F2F.toInt()),
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
        DatePickerDialog(
            selectedDateMillis = targetDate.toEpochDay() * 24 * 60 * 60 * 1000,
            onDateSelected = { millis ->
                millis?.let {
                    targetDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}