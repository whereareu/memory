package com.quanneng.memory.features.widget.configuration

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.quanneng.memory.MemoryApp
import com.quanneng.memory.R
import com.quanneng.memory.features.widget.model.WidgetConfig
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * 小部件配置Activity
 * 用户可以配置文本内容、字体大小和颜色
 */
class WidgetConfigActivity : AppCompatActivity() {

    private lateinit var viewModel: WidgetConfigViewModel
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    // UI组件
    private lateinit var textInput: TextInputEditText
    private lateinit var textSizeSeekBar: SeekBar
    private lateinit var textSizeValue: com.google.android.material.textview.MaterialTextView
    private lateinit var previewContainer: LinearLayout
    private lateinit var previewText: com.google.android.material.textview.MaterialTextView
    private lateinit var textColorContainer: LinearLayout
    private lateinit var backgroundColorContainer: LinearLayout
    private lateinit var saveButton: MaterialButton

    // 颜色选项
    private val textColors = listOf(
        0xFF212121.toInt(), // 黑色
        0xFF757575.toInt(), // 灰色
        0xFFFFFFFF.toInt(), // 白色
        0xFFD32F2F.toInt(), // 红色
        0xFF1976D2.toInt(), // 蓝色
        0xFF388E3C.toInt(), // 绿色
        0xFFF57C00.toInt(), // 橙色
        0xFF7B1FA2.toInt(), // 紫色
        0xFFE91E63.toInt(), // 粉色
        0xFF00BCD4.toInt(), // 青色
        0xFFFFC107.toInt(), // 琥珀色
        0xFF795548.toInt()  // 棕色
    )

    private val backgroundColors = listOf(
        0xFFFFFFFF.toInt(),           // 纯白色
        0xFFFAFAFA.toInt(),           // 浅灰色
        0xFFF5F5F5.toInt(),           // 灰白色
        0x80FFFFFF.toInt(),           // 白色 50% 透明
        0x80FAFAFA.toInt(),           // 浅灰 50% 透明
        0xCC212121.toInt(),           // 黑色 80% 不透明
        0x80000000.toInt(),           // 黑色 50% 透明
        0xDD000000.toInt(),           // 黑色 87% 不透明
        0xFF212121.toInt(),           // 深黑色
        0xFF1976D2.toInt(),           // 蓝色
        0x801976D2.toInt(),           // 蓝色 50% 透明
        0xFF388E3C.toInt(),           // 绿色
        0x80388E3C.toInt(),           // 绿色 50% 透明
        0xFFD32F2F.toInt(),           // 红色
        0xFFF57C00.toInt(),           // 橙色
        0xFF7B1FA2.toInt(),           // 紫色
        0xFFE91E63.toInt(),           // 粉色
        0xFF00BCD4.toInt(),           // 青色
        0xFFFFC107.toInt(),           // 琥珀色
        0xFF3F51B5.toInt(),           // 靛蓝色
        0x803F51B5.toInt(),           // 靛蓝 50% 透明
        0xFF607D8B.toInt(),           // 蓝灰色
        0xFF9E9E9E.toInt(),           // 中灰色
        0xFF616161.toInt()            // 深灰色
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)

        // 获取小部件ID
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        // 如果ID无效，取消配置
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        initViews()
        initViewModel()
        setupColorPickers()
        setupListeners()
    }

    private fun initViews() {
        textInput = findViewById(R.id.text_input)
        textSizeSeekBar = findViewById(R.id.text_size_seekbar)
        textSizeValue = findViewById(R.id.text_size_value)
        previewContainer = findViewById(R.id.preview_container)
        previewText = findViewById(R.id.preview_text)
        textColorContainer = findViewById(R.id.text_color_container)
        backgroundColorContainer = findViewById(R.id.background_color_container)
        saveButton = findViewById(R.id.save_button)
    }

    private fun initViewModel() {
        val repository = (application as MemoryApp).container.widgetRepository
        viewModel = ViewModelProvider(
            this,
            WidgetConfigViewModelFactory(repository, appWidgetId)
        )[WidgetConfigViewModel::class.java]

        // 观察UI状态
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is WidgetConfigUiState.Loading -> {
                        // 显示加载状态
                    }
                    is WidgetConfigUiState.Success -> {
                        // 直接使用小部件的独立配置，不再加载全局配置
                        updateUI(state.config)
                    }
                    is WidgetConfigUiState.Error -> {
                        // 显示错误
                    }
                }
            }
        }
    }

    private fun updateUI(config: WidgetConfig) {
        textInput.setText(config.text)
        textSizeSeekBar.progress = config.textSize.toInt()
        textSizeValue.text = "${config.textSize.toInt()}sp"
        updatePreview(config)
    }

    private fun setupColorPickers() {
        // 文字颜色选择器
        textColors.forEach { color ->
            val colorView = createColorView(color)
            colorView.setOnClickListener {
                viewModel.updateTextColor(color)
                updatePreviewFromViewModel()
            }
            textColorContainer.addView(colorView)
        }

        // 背景颜色选择器
        backgroundColors.forEach { color ->
            val colorView = createColorView(color)
            colorView.setOnClickListener {
                viewModel.updateBackgroundColor(color)
                updatePreviewFromViewModel()
            }
            backgroundColorContainer.addView(colorView)
        }
    }

    private fun createColorView(color: Int): View {
        val size = (40 * resources.displayMetrics.density).toInt()
        val margin = (8 * resources.displayMetrics.density).toInt()

        val view = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                setMargins(margin, margin, margin, margin)
            }
            setBackgroundColor(color)
        }

        return view
    }

    private fun setupListeners() {
        // 文本输入监听
        textInput.setOnEditorActionListener { _, _, _ ->
            viewModel.updateText(textInput.text.toString())
            updatePreviewFromViewModel()
            false
        }

        // 字体大小滑块监听
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSizeValue.text = "${progress}sp"
                if (fromUser) {
                    viewModel.updateTextSize(progress.toFloat())
                    updatePreviewFromViewModel()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 保存按钮
        saveButton.setOnClickListener {
            saveConfigAndFinish()
        }
    }

    private fun updatePreviewFromViewModel() {
        val state = viewModel.uiState.value
        if (state is WidgetConfigUiState.Success) {
            updatePreview(state.config)
        }
    }

    private fun updatePreview(config: WidgetConfig) {
        previewText.text = config.text
        previewText.textSize = config.textSize
        previewText.setTextColor(config.textColor)
        previewContainer.setBackgroundColor(config.backgroundColor)
    }

    private fun saveConfigAndFinish() {
        lifecycleScope.launch {
            val state = viewModel.uiState.value
            if (state is WidgetConfigUiState.Success) {
                val success = viewModel.saveConfig(state.config)
                if (success) {
                    // 更新小部件
                    val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                    (application as MemoryApp).container.widgetUpdater.updateWidget(appWidgetId)

                    // 返回结果
                    val resultValue = Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    setResult(RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}

/**
 * ViewModelFactory
 */
class WidgetConfigViewModelFactory(
    private val repository: com.quanneng.memory.features.widget.data.WidgetRepository,
    private val appWidgetId: Int
) : androidx.lifecycle.ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WidgetConfigViewModel::class.java)) {
            return WidgetConfigViewModel(repository, appWidgetId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
