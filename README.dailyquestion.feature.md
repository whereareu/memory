# 每日Android一问功能文档

## 功能概述

"每日Android一问"是 Memory 应用的插件化功能，每天为用户提供精选的 Android 技术问题，涵盖 Android 核心、数据结构、设计模式、Java/Kotlin 等领域。每个问题都提供深度解析，正反两面对比论证，帮助开发者深入理解技术原理。

## 核心功能

### 1. 每日问题推送
- 每天自动更新一道精选问题
- 考虑难度分布（初级:中级:高级 = 4:4:2）
- 避免短期内重复相同问题

### 2. 深度解析
- 正反两面对比论证（≥800字）
- 为什么选择正确答案
- 为什么排除错误答案
- 相关知识点扩展
- 实际应用场景说明

### 3. 历史记录
- 查看所有历史问题
- 按分类筛选
- 按难度筛选
- 搜索功能

### 4. 数据管理
- 问题删除功能
- 数据导入/导出（JSON格式）
- 本地数据库持久化

## 技术架构

### 目录结构
```
features/dailyquestion/
├── data/
│   ├── Question.kt              # 数据模型
│   ├── QuestionMetadata.kt      # 元数据
│   ├── QuestionCategory.kt      # 分类枚举
│   ├── QuestionDifficulty.kt    # 难度枚举
│   ├── QuestionAnalysis.kt      # 解析内容
│   ├── QuestionEntity.kt        # 数据库实体
│   ├── QuestionDao.kt           # 数据库访问
│   ├── QuestionDatabase.kt      # 数据库定义
│   ├── QuestionRepository.kt    # 仓库接口
│   ├── QuestionRepositoryImpl.kt # 仓库实现
│   └── QuestionInitialData.kt   # 初始问题数据
├── domain/
│   ├── DailyQuestionScheduler.kt    # 每日调度器
│   ├── DailyQuestionPreferences.kt  # 偏好设置
│   └── QuestionSelector.kt          # 问题选择器
├── presentation/
│   ├── DailyQuestionActivity.kt     # Activity
│   ├── DailyQuestionViewModel.kt    # ViewModel
│   ├── DailyQuestionViewModelFactory.kt # Factory
│   ├── DailyQuestionScreen.kt       # 主界面
│   ├── QuestionUiState.kt           # UI状态
│   ├── QuestionCard.kt              # 问题卡片
│   ├── HistoryListView.kt           # 历史列表
│   └── AnswerDetailScreen.kt        # 详情页
├── plugin/
│   ├── QuestionPlugin.kt            # 插件接口
│   ├── QuestionPluginLoader.kt      # 插件加载器
│   └── DailyQuestionPluginImpl.kt   # 插件实现
└── DailyQuestionTest.kt             # 单元测试
```

### 状态管理

使用 Kotlin Flow + StateFlow 进行状态管理：

```kotlin
// UI 状态
sealed interface QuestionUiState {
    object Initial : QuestionUiState
    object Loading : QuestionUiState
    data class Success(val question: Question) : QuestionUiState
    data class HistoryList(val questions: List<Question>) : QuestionUiState
    data class QuestionDetail(val question: Question) : QuestionUiState
    data class DeleteConfirm(val question: Question) : QuestionUiState
    data class Error(val message: String) : QuestionUiState
}

// 操作状态
sealed interface QuestionActionState {
    object None : QuestionActionState
    object Refreshing : QuestionActionState
    data class Deleting(val questionId: String) : QuestionActionState
}
```

### 数据持久化

使用 Room Database 进行数据持久化：

```kotlin
@Database(
    entities = [QuestionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class QuestionDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
}
```

### 每日更新机制

使用 DataStore 保存每日状态，通过 WorkManager 实现定时更新：

```kotlin
class DailyQuestionScheduler(
    private val context: Context,
    private val repository: QuestionRepository
) {
    suspend fun getTodayQuestion(forceRefresh: Boolean = false): Question?
    suspend fun refreshTodayQuestion(): Question?
    suspend fun initialize(): Int
}
```

## 问题内容规范

### 格式要求
```
## 标题
简短明确的问题描述

## 问题背景
实际开发场景或面试场景

## 选项A
正确答案描述

## 选项B
错误答案描述

## 深度解析
### 为什么选择A（≥400字）
详细论证，包含原理、实现、优势

### 为什么不选B（≥400字）
对比分析，指出问题、局限、错误原因

### 相关知识点扩展
相关技术点、延伸阅读

### 实际应用场景
真实项目中的应用示例

## 相关标签
android, viewmodel, lifecycle
```

### 分类分布
- **Android核心**（30%）：View系统、四大组件、生命周期、权限管理
- **数据结构**（20%）：常用数据结构、算法复杂度、Android集合框架
- **设计模式**（20%）：常用设计模式、Android架构模式
- **Java/Kotlin**（30%）：语言特性、并发编程、内存管理

### 难度级别
- **初级**：基础概念，日常开发常见问题
- **中级**：深入理解，需要综合分析
- **高级**：原理级理解，涉及源码或性能优化

## 使用示例

### 启动功能
```kotlin
// 从主界面启动
startActivity(Intent(this, DailyQuestionActivity::class.java))
```

### 观察今日问题
```kotlin
viewModel.uiState.collectAsState().value.let { state ->
    when (state) {
        is QuestionUiState.Success -> {
            // 显示问题卡片
            QuestionCard(
                question = state.question,
                onClick = { viewModel.viewQuestionDetail(it) }
            )
        }
        // ... 其他状态处理
    }
}
```

### 刷新今日问题
```kotlin
viewModel.refreshTodayQuestion()
```

### 查看历史记录
```kotlin
viewModel.loadHistory()
```

## 依赖注入

使用手动依赖注入：

```kotlin
class AppContainer(context: Context) {
    val questionRepository: QuestionRepositoryImpl = QuestionRepositoryImpl(context)
    val dailyQuestionScheduler: DailyQuestionScheduler = DailyQuestionScheduler(
        context = context,
        repository = questionRepository
    )
    val questionPluginLoader: QuestionPluginLoader = QuestionPluginLoader(context)

    suspend fun initializePlugins() {
        val dailyQuestionPlugin = DailyQuestionPluginImpl(context)
        questionPluginLoader.registerPlugin(dailyQuestionPlugin)
    }
}
```

## 测试

### 运行单元测试
```bash
./gradlew testDebugUnitTest
```

### 测试覆盖
- 数据模型验证
- 问题选择算法
- 状态管理
- 数据库操作

## 注意事项

1. **问题内容质量**：每个问题的解析内容不低于800字，确保深度和广度
2. **分类平衡**：保持各分类的合理分布，避免过于集中在某个领域
3. **难度递进**：通过难度分布，满足不同水平开发者的需求
4. **数据持久化**：所有问题都保存在本地数据库，支持离线访问
5. **插件化设计**：采用插件化架构，便于扩展和维护

## 未来规划

- [ ] 添加收藏功能
- [ ] 添加学习进度追踪
- [ ] 支持自定义问题
- [ ] 添加问题评论/讨论功能
- [ ] 实现社交分享功能
- [ ] 添加错题本功能
- [ ] 支持导出为 PDF
