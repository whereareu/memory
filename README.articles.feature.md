# 技术文章功能 (Articles)

## 功能概述

从 GitHub 获取 Python 爬虫生成的技术文章数据，并在 Android 应用中展示。

## 入口

- 主页面快速访问区域的"文章"图标
- 包名：`com.quanneng.memory.features.articles.ui.ArticleListActivity`

## 核心功能

### 1. 文章列表展示
- 从 GitHub Raw 获取最新文章数据
- 展示文章标题、摘要、封面图、作者、来源等信息
- 支持下拉刷新

### 2. 来源筛选
- 按文章来源筛选（掘金、CSDN、Medium 等）
- Tab 切换交互

### 3. 文章搜索
- 实时搜索文章标题、摘要、标签
- 大小写不敏感

### 4. 文章详情
- 点击跳转到原文阅读
- 支持内置 WebView 或外部浏览器

## 技术架构

### 目录结构
```
features/articles/
├── model/
│   └── Article.kt          # 数据模型
├── data/
│   └── ArticleRepository.kt # 数据仓库
└── ui/
    ├── ArticleListActivity.kt   # Activity 入口
    ├── ArticleViewModel.kt      # ViewModel
    └── screen/
        └── ArticleListScreen.kt # Compose UI
```

### 架构分层
- **UI 层**：ArticleListScreen (Compose) + ArticleListActivity
- **业务层**：ArticleViewModel
- **数据层**：ArticleRepository (使用 Ktor HTTP Client)
- **模型层**：Article, ArticleData, ArticleSource

### 状态管理
- 使用 Kotlin Flow 进行响应式状态管理
- UI 状态封装在 `ArticleUiState` 密封类中：
  - `Loading`：加载中
  - `Success`：成功（包含文章列表、筛选状态）
  - `Error`：错误（包含错误信息）

### 依赖注入
- 通过 AppContainer 手动依赖注入
- Repository 支持协程调度器注入

## 数据格式

### GitHub Raw URL
```
https://raw.githubusercontent.com/whereareu/memory-data/main/articles.json
```

### JSON 结构
```json
{
  "version": "1.0",
  "last_updated": "2026-03-26T17:19:40.488549",
  "sources": [
    {
      "name": "掘金",
      "icon": "https://juejin.cn/favicon.ico",
      "url": "https://juejin.cn"
    }
  ],
  "articles": [
    {
      "id": "367b6e42f484",
      "title": "Kotlin 协程原理详解",
      "summary": "详细解析 Kotlin 协程的内部实现...",
      "author": "掘金作者",
      "source": "掘金",
      "url": "https://juejin.cn/post/xxx",
      "cover_image": "https://...",
      "tags": ["Kotlin", "协程"],
      "published_at": "2026-03-26T16:21:08",
      "read_time_minutes": 5
    }
  ]
}
```

## 关键调试记忆

### 问题1: ProGuard移除日志导致无法诊断
**症状**: `Log.d()` 调用无任何输出，无法追踪问题

**原因**: `proguard-rules.pro` 中配置了 `-assumenosideeffects class android.util.Log`
```
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    ...
}
```
这告诉ProGuard这些日志方法没有副作用，可以被安全移除。

**解决**: 注释掉该规则，debug构建保留日志
```
# -assumenosideeffects class android.util.Log { ... }
```

**位置**: `app/proguard-rules.pro:23-30`

---

### 问题2: GitHub Raw Content-Type不匹配
**症状**:
```
Expected response body of the type 'class ArticleData' but was 'class ByteBufferChannel'
Response header `ContentType: text/plain; charset=utf-8`
Request header `Accept: application/json`
Response status `200 OK`
```

**原因**: `raw.githubusercontent.com` 返回 `Content-Type: text/plain; charset=utf-8`
而非 `application/json`。Ktor的ContentNegotiation插件只处理预配置的内容类型。

**解决**: 手动解析响应体，绕过ContentNegotiation的自动类型检测
```kotlin
// 修复前：使用自动body()解析
val body: ArticleData = response.body()  // 失败：NoTransformationFoundException

// 修复后：手动获取文本并解析
val responseText = response.bodyAsText()
val body = json.decodeFromString<ArticleData>(responseText)  // 成功
```

**位置**: `app/src/main/java/com/quanneng/memory/features/articles/data/ArticleRepository.kt:68-70`

---

### 调试经验总结
1. **日志第一原则**: 遇到问题先确认日志是否被混淆工具移除
2. **Content-Type陷阱**: 第三方API的Content-Type可能与文档不符，需实测
3. **手动降级策略**: 自动序列化失败时，手动解析是可靠的fallback
4. **日志输出位置**: 使用PID过滤日志而非包名，避免匹配到系统组件

## 使用示例

### 启动文章列表
```kotlin
val intent = Intent(context, ArticleListActivity::class.java)
context.startActivity(intent)
```

### 添加新的数据源
在 Python 爬虫项目中：
1. 继承 `BaseCrawler`
2. 实现 `get_articles()` 方法
3. 在 `main.py` 中注册

## 调试命令

```bash
# 编译安装
./gradlew assembleDebug
adb -s <DEVICE_ID> install -r app/build/outputs/apk/debug/app-debug.apk

# 启动Activity
adb -s <DEVICE_ID> shell am start -n com.quanneng.memory/.features.articles.ui.ArticleListActivity

# 查看日志（使用PID过滤更精确）
adb -s <DEVICE_ID> logcat -c
adb -s <DEVICE_ID> shell "ps | grep quanneng.memory" | awk '{print $2}'
adb -s <DEVICE_ID> logcat | grep "<PID>"
```

## 注意事项

1. **网络请求**：使用 Ktor HTTP Client，支持 JSON 序列化
2. **图片加载**：使用 Coil 异步加载，支持缓存
3. **错误处理**：网络失败时显示错误视图，支持重试
4. **性能优化**：LazyColumn 懒加载，图片缓存

## 后续优化方向

1. **本地缓存**：使用 Room 数据库缓存文章
2. **离线阅读**：下载文章内容到本地
3. **收藏功能**：支持收藏和稍后阅读
4. **阅读历史**：记录阅读历史
5. **推送通知**：新文章推送提醒
6. **更多数据源**：CSDN、Medium、知乎专栏等
