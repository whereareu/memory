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
https://raw.githubusercontent.com/[username]/memory-data/main/articles.json
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
