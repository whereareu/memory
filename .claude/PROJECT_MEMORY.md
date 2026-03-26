# Memory 项目结构记忆文档

> 本文档用于快速理解项目结构，避免反复读取文件浪费时间

## 项目基本信息

- **项目名称**: Memory
- **包名**: `com.quanneng.memory`
- **应用ID**: `com.quanneng.memory`
- **编译SDK**: 36
- **最低SDK**: 24 (Android 7.0)
- **语言**: Kotlin
- **构建工具**: Gradle (Kotlin DSL)

## 技术栈

### UI框架
- **Jetpack Compose** + Material3
- **Coil** - 图片加载
- **Compose Navigation** - 导航

### 数据层
- **DataStore Preferences** - 轻量级数据存储
- **Room Database** - 本地数据库 (FlashThought功能)
- **Kotlin Serialization** - JSON序列化
- **Ktor HTTP Client** - 网络请求 (支持GitHub数据源)

### 架构
- **MVVM** 架构
- **手动依赖注入** (AppContainer)
- **Kotlin Flow** - 响应式数据流
- **Kotlin Coroutines** - 异步处理

### 测试
- **JUnit5** - 测试框架
- **MockK** - Mock框架
- **Turbine** - Flow测试

## 项目目录结构

```
app/src/main/java/com/quanneng/memory/
├── App.kt                          # Application入口，持有DI容器
├── Splash.kt                       # 启动页（主入口Activity）
├── Main.kt                         # 主页面，快速访问入口
├── Edit.kt                         # 小部件编辑页面
├── MemoryApp.kt                    # 自定义Application类
│
├── core/                           # 核心通用代码（≥2功能共用）
│   ├── datastore/                  # DataStore封装
│   │   ├── MultiInstanceWidgetPreferences.kt
│   │   ├── DateCounterPreferences.kt
│   │   ├── CountdownPreferences.kt
│   │   └── EditPreferences.kt
│   ├── dispatchers/                # 协程调度器
│   │   ├── DispatcherProvider.kt
│   │   └── IoDispatcher.kt (注解)
│   ├── widget/                     # 小部件通用工具
│   │   └── WidgetUpdater.kt
│   └── di/                         # 依赖注入
│       └── AppContainer.kt         # 手动DI容器
│
└── features/                       # 业务功能（单数命名，内部禁止再分层）
    ├── widget/                     # 小部件功能
    │   ├── widget/                 # 小部件Provider
    │   │   ├── TextWidget.kt       # 通用Widget (type=0)
    │   │   ├── TextWidget1.kt      # 质量Widget (type=1)
    │   │   ├── TextWidget2.kt      # 名言Widget (type=2)
    │   │   ├── TextWidget3.kt      # 日期计数器 (type=3)
    │   │   ├── TextWidget4.kt      # 倒计时 (type=4)
    │   │   └── WidgetSizeProvider.kt
    │   ├── configuration/          # 小部件配置
    │   │   ├── WidgetConfigActivity.kt
    │   │   └── WidgetConfigViewModel.kt
    │   ├── data/                   # 数据层
    │   │   ├── WidgetRepository.kt
    │   │   └── WidgetDataSource.kt
    │   └── model/                  # 数据模型
    │       ├── WidgetConfig.kt
    │       ├── DateCounterConfig.kt
    │       └── CountdownConfig.kt
    │
    ├── flashthought/               # 闪现功能
    │   ├── widget/                 # 闪现Widget
    │   │   └── FlashThoughtWidget.kt
    │   ├── ui/                     # UI层
    │   │   ├── FlashThoughtQuickAddActivity.kt
    │   │   └── FlashThoughtListActivity.kt
    │   ├── data/                   # 数据层
    │   │   ├── FlashThoughtRepository.kt
    │   │   ├── FlashThoughtDao.kt
    │   │   ├── FlashThoughtDatabase.kt
    │   │   └── FlashThoughtConverters.kt
    │   └── model/                  # 数据模型
    │       └── FlashThought.kt
    │
    ├── applist/                    # 应用列表管理
    │   ├── ui/
    │   │   ├── AppListActivity.kt
    │   │   ├── AppListViewModel.kt
    │   │   ├── AppListViewModelFactory.kt
    │   │   └── AppListScreen.kt
    │   ├── data/
    │   │   └── AppListRepository.kt
    │   └── model/
    │       └── AppInfo.kt
    │
    ├── articles/                   # 技术文章功能
    │   ├── ui/
    │   │   ├── ArticleListActivity.kt
    │   │   ├── ArticleViewModel.kt
    │   │   └── screen/
    │   │       └── ArticleListScreen.kt
    │   ├── data/
    │   │   └── ArticleRepository.kt
    │   └── model/
    │       └── Article.kt
    │
    └── dailyquestion/              # 每日Android一问
        ├── presentation/           # 展示层
        ├── data/                   # 数据层
        └── model/                  # 数据模型
```

## 已完成功能模块

### 1. 小部件系统 (Widget)
- ✅ 支持6种独立小部件类型
- ✅ 多实例支持，每个实例独立配置
- ✅ 自适应布局 (2x2, 4x2, 4x4)
- ✅ 自定义文本、字体、颜色、背景
- ✅ 实时预览

### 2. 闪现功能 (FlashThought)
- ✅ 快速记录想法
- � Room数据库持久化
- ✅ 置顶功能
- ✅ 历史列表查看

### 3. 应用列表管理 (AppList)
- ✅ 显示所有已安装应用
- ✅ 应用搜索/筛选
- ✅ 下拉刷新
- ✅ 应用详情查看
- ✅ 应用卸载
- ✅ 网格布局支持（3/4/5列切换）
- ✅ 排序功能（按名称/安装时间/更新时间）

### 4. 每日Android一问 (DailyQuestion)
- ✅ 问题展示
- ✅ 混合数据源策略（本地 → GitHub → 智谱AI → 静态兜底）
- ✅ 历史记录

### 5. 技术文章 (Articles)
- ✅ Python 爬虫（掘金平台，自动过滤 Android/Kotlin 相关）
- ✅ GitHub Actions 定时更新（每小时）
- ✅ Android 端展示（来源筛选、实时搜索、下拉刷新）
- ✅ Ktor HTTP Client 集成
- ✅ 独立数据仓库 (memory-data)
- ✅ 自动数据同步

## 架构规范

### 目录规范
- `features/` - 业务功能，全部单数
- `core/` - ≥2功能共用的核心代码，内部禁止再分层
- 同功能代码同目录，避免多级嵌套

### 命名规范
- XML id: `snake_case`
- 类/方法: 功能名直译，避免缩写
- 包名: `com.example.feature`
- 文件名 = 类名

### 零Base类
- 禁止任何 `BaseActivity/Fragment/VM`
- 通用逻辑高内聚在 core

### 生命周期与并发
- Activity/Fragment 只负责导航与绑定
- VM 内仅允许 `viewModelScope.launch{}`
- 数据层所有接口必须为 `suspend` 并加 `@IoDispatcher`

### 单元测试规范
- 每个 feature 一个 `FeatureTest.kt`
- 所有 `public suspend` & VM 公开 Flow 必须被测试
- 测试命名: `被测_输入_预期`
- PR增量代码 Jacoco ≥ 80%

## 关键入口点

### 应用入口
- **启动页**: `Splash.kt` (LAUNCHER Activity)
- **主页面**: `Main.kt`
- **应用类**: `MemoryApp.kt` (持有 AppContainer)

### 功能入口
- **小部件编辑**: `Edit.kt`
- **应用列表**: `features/applist/ui/AppListActivity.kt`
- **闪现列表**: `features/flashthought/ui/FlashThoughtListActivity.kt`
- **每日一问**: `features/dailyquestion/presentation/DailyQuestionActivity.kt`
- **技术文章**: `features/articles/ui/ArticleListActivity.kt`

### 小部件Provider
- **通用**: `features/widget/widget/TextWidget.kt`
- **质量**: `features/widget/widget/TextWidget1.kt`
- **名言**: `features/widget/widget/TextWidget2.kt`
- **日期计数**: `features/widget/widget/TextWidget3.kt`
- **倒计时**: `features/widget/widget/TextWidget4.kt`
- **闪现**: `features/flashthought/widget/FlashThoughtWidget.kt`

### 依赖注入
- **容器**: `core/di/AppContainer.kt`
- 通过 `MemoryApp.container` 访问所有依赖

## 状态管理模式

使用 Kotlin Flow + StateFlow，UI状态封装在密封类中：

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: T) : UiState()
    data class Error(val message: String) : UiState()
}
```

## 资源文件

### 布局文件 (app/src/main/res/layout/)
- `widget_text_*.xml` - 文本小部件布局
- `widget_date_counter_*.xml` - 日期计数器布局
- `widget_countdown_*.xml` - 倒计时布局
- `widget_flash_thought_*.xml` - 闪现小部件布局
- `activity_*.xml` - Activity配置界面布局

### 小部件配置 (app/src/main/res/xml/)
- `widget_info.xml` - 通用小部件
- `widget_info_1.xml` - 质量小部件
- `widget_info_2.xml` - 名言小部件
- `widget_info_3.xml` - 日期计数器
- `widget_info_4.xml` - 倒计时
- `widget_info_flash_thought.xml` - 闪现小部件

## 常用命令

### 构建与测试
```bash
# 构建Debug版本
./gradlew assembleDebug

# 运行单元测试
./gradlew testDebugUnitTest

# 运行特定测试类
./gradlew test --tests AppListTest
```

## GitHub 仓库信息

### 主仓库 (whereareu/memory)
- **URL**: https://github.com/whereareu/memory
- **用途**: Android 应用主项目
- **分支**: main

### 数据仓库 (whereareu/memory-data)
- **URL**: https://github.com/whereareu/memory-data
- **用途**: 存储爬虫生成的文章数据
- **Raw URL**: https://raw.githubusercontent.com/whereareu/memory-data/main/articles.json
- **更新频率**: 每小时 (GitHub Actions 自动)
- **最后更新**: 2026-03-26 09:58:20 UTC

### GitHub Actions
- **Workflow**: `memory-data/.github/workflows/sync.yml`
- **触发方式**:
  - 定时: `cron: '0 * * * *'` (每小时)
  - 手动: `gh workflow run "Sync Articles from Memory"`
- **权限**: `contents: write`
- **状态**: ✅ Active

## 项目进度

- **当前版本**: 1.0
- **最后更新**: 2026-03-26
- **已完成**: 5个主要功能模块
- **最新完成**: 技术文章爬虫 + GitHub Actions 自动更新

## 注意事项

1. **性能红线**: assembleDebug ≤ 90s，冷启动 ≤ 800ms
2. **单文件public符号 ≤ 5**
3. **禁止主线程IO和长耗时计算**
4. **测试覆盖率 ≥ 80%**
5. **所有数据层接口必须使用 @IoDispatcher 注解**

## 相关文档

### 功能文档
- `README.widget.md` - 小部件功能详细文档
- `README.applist.md` - 应用列表功能详细文档
- `README.articles.feature.md` - 技术文章功能详细文档
- `crawler/README.md` - Python 爬虫项目文档

### 架构与实施
- `ARCHITECTURE.md` - 技术文章爬虫架构设计
- `IMPLEMENTATION_SUMMARY.md` - 技术文章功能完整实施总结

### 进度记录
- `PROGRESS.md` - 项目开发进度记录
- `DONE.md` - 已完成任务归档

### 项目记忆
- `.claude/PROJECT_MEMORY.md` - 本文档（项目结构快速记忆）

## 重要链接

### GitHub 仓库
- **主仓库**: https://github.com/whereareu/memory
- **数据仓库**: https://github.com/whereareu/memory-data
- **Actions**: https://github.com/whereareu/memory-data/actions

### 数据 API
- **Raw JSON**: https://raw.githubusercontent.com/whereareu/memory-data/main/articles.json
- **备用 URL**: https://cdn.jsdelivr.net/gh/whereareu/memory-data@main/articles.json

## 外部项目

### crawler/ - Python 爬虫项目
- **位置**: `crawler/` (主项目子目录)
- **Python 版本**: 3.12
- **虚拟环境**: `crawler/venv/`

#### 目录结构
```
crawler/
├── src/
│   ├── crawlers/          # 爬虫实现
│   │   ├── base.py       # 基础爬虫类
│   │   └── juejin.py     # 掘金爬虫（已实现）
│   ├── models/           # 数据模型
│   │   └── article.py    # 文章模型 (Pydantic)
│   └── main.py           # 主程序
├── data/
│   └── articles.json     # 生成的数据文件
├── tests/
│   └── test_crawler.py   # 单元测试
├── .github/workflows/
│   └── crawler.yml       # 备用 workflow (未使用)
├── venv/                 # Python 虚拟环境
├── requirements.txt      # 依赖列表
├── .gitignore           # Git 忽略规则
├── debug_api.py         # API 调试工具
└── README.md            # 爬虫文档
```

#### 已实现的数据源
- ✅ **掘金 (Juejin)** - Android/Kotlin 相关文章
  - API: `https://juejin.cn/recommend_api/v1/article/recommend_all_feed`
  - 过滤: Android, Kotlin, Compose, Jetpack 等关键词
  - 字段: 标题、摘要、作者、封面、发布时间、阅读时间

#### 依赖项
- `requests` - HTTP 请求
- `beautifulsoup4` - HTML 解析
- `lxml` - 解析器
- `pydantic` - 数据验证
- `python-dateutil` - 日期处理

#### 运行方式
```bash
# 本地运行
cd crawler
source venv/bin/activate
python src/main.py

# 或使用 python3
python3 src/main.py
```

#### 数据流程图
```
┌─────────────────────────────────────────────────────────────┐
│                    GitHub Actions 循环                        │
├─────────────────────────────────────────────────────────────┤
│  1. 每小时触发 (cron: '0 * * * *')                          │
│     ↓                                                        │
│  2. 从 memory 仓库拉取爬虫代码                               │
│     ↓                                                        │
│  3. 运行 Python 爬虫 (掘金 API)                             │
│     ↓                                                        │
│  4. 生成 articles.json                                      │
│     ↓                                                        │
│  5. 提交并推送到 memory-data 仓库                           │
│     ↓                                                        │
│  6. Android App 通过 Raw URL 读取                           │
└─────────────────────────────────────────────────────────────┘

GitHub Raw URL:
https://raw.githubusercontent.com/whereareu/memory-data/main/articles.json
```

## Android 技术文章功能详情

### 数据模型 (Article.kt)
```kotlin
// 文章来源枚举
enum class ArticleSource { JUEJIN, CSDN, MEDIUM, ANDROID_DEVELOPERS_BLOG }

// 文章数据类
data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val author: String,
    val source: ArticleSource,
    val url: String,
    val coverImage: String?,
    val tags: List<String>,
    val publishedAt: String,  // ISO 8601
    val readTimeMinutes: Int
)

// 数据容器
data class ArticleData(
    val version: String,
    val lastUpdated: String,
    val sources: List<ArticleSourceInfo>,
    val articles: List<Article>
)
```

### Repository 实现 (ArticleRepository.kt)
- **HTTP Client**: Ktor (Android Engine)
- **序列化**: kotlinx.serialization + JSON
- **方法**:
  - `fetchArticles()` - 获取完整数据
  - `getAllArticles()` - 获取文章列表
  - `getArticlesBySource()` - 按来源筛选
  - `searchArticles()` - 搜索文章
- **线程**: 所有方法使用 `@IoDispatcher` 注解

### UI 状态管理 (ArticleViewModel.kt)
```kotlin
sealed class ArticleUiState {
    object Loading
    data class Success(
        val articles: List<Article>,
        val filteredArticles: List<Article>,
        val sources: List<ArticleSource>,
        val selectedSource: ArticleSource?,
        val searchQuery: String
    )
    data class Error(val message: String)
}
```

### UI 功能 (ArticleListScreen.kt)
- **来源筛选**: ScrollableTabRow (全部/掘金/CSDN...)
- **搜索功能**: 实时搜索（标题、摘要、标签）
- **下拉刷新**: SwipeRefresh
- **文章卡片**: 封面图、标题、摘要、作者、时间、阅读时间
- **错误处理**: 重试按钮

## 常用命令补充

### GitHub Actions 相关
```bash
# 查看运行状态
cd /tmp/memory-data
gh run list

# 手动触发更新
gh workflow run "Sync Articles from Memory"

# 查看最近的 workflow
gh run view

# 查看 workflow 日志
gh run view --log
```

### Python 爬虫调试
```bash
# 调试掘金 API
cd crawler
source venv/bin/activate
python debug_api.py

# 查看生成的数据
cat data/articles.json | python3 -m json.tool

# 运行测试
pytest tests/
```

### 数据验证
```bash
# 验证 GitHub Raw 数据
curl "https://raw.githubusercontent.com/whereareu/memory-data/main/articles.json" | python3 -m json.tool

# 检查最后更新时间
curl -s "https://raw.githubusercontent.com/whereareu/memory-data/main/articles.json" | \
  python3 -c "import sys, json; print(json.load(sys.stdin)['last_updated'])"
```