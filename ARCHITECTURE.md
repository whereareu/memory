# 技术文章爬虫 + GitHub + Android 展示方案

## 整体架构

```
┌─────────────────┐      ┌──────────────┐      ┌─────────────┐
│  Python 爬虫    │ ───> │  GitHub 仓库 │ ───> │  Android App│
│  (定时任务)     │      │  (JSON 数据) │      │  (展示)      │
└─────────────────┘      └──────────────┘      └─────────────┘
```

## 项目结构

```
memory/
├── app/                          # Android 项目
│   └── src/main/java/com/quanneng/memory/
│       └── features/
│           └── articles/         # 新增：文章功能
│               ├── model/        # Article.kt
│               ├── data/         # ArticleRepository.kt
│               ├── ui/           # ArticleListActivity.kt
│               └── README.articles.md
│
└── crawler/                      # 新增：Python 爬虫项目
    ├── src/
    │   ├── crawlers/
    │   │   ├── base.py          # 基础爬虫类
    │   │   ├──掘金.py           # 掘金爬虫
    │   │   ├── csdn.py          # CSDN爬虫
    │   │   └── medium.py        # Medium爬虫
    │   ├── models/
    │   │   └── article.py       # 文章数据模型
    │   └── main.py              # 主程序
    ├── data/
    │   └── articles.json        # 生成的数据文件
    ├── tests/
    │   └── test_crawler.py
    ├── requirements.txt
    ├── .github/
    │   └── workflows/
    │       └── crawler.yml      # GitHub Actions 定时任务
    ├── README.md
    └── README.crawler.md
```

## 数据格式 (JSON)

```json
{
  "version": "1.0",
  "last_updated": "2025-03-26T10:00:00Z",
  "sources": [
    {
      "name": "掘金",
      "icon": "https://xxx.com/icon.png",
      "url": "https://juejin.cn"
    }
  ],
  "articles": [
    {
      "id": "unique_id",
      "title": "文章标题",
      "summary": "文章摘要",
      "author": "作者名",
      "source": "掘金",
      "url": "https://juejin.cn/post/xxx",
      "cover_image": "https://xxx.com/cover.png",
      "tags": ["Android", "Kotlin"],
      "published_at": "2025-03-26T09:00:00Z",
      "read_time_minutes": 8
    }
  ]
}
```

## 技术选型

### Python 爬虫
- **requests** - HTTP 请求
- **beautifulsoup4** - HTML 解析
- **lxml** - 解析器
- **pydantic** - 数据验证
- **pytest** - 单元测试

### GitHub Actions
- 定时任务：每小时运行一次 (cron: `0 * * * *`)
- 自动提交 JSON 到仓库

### Android 端
- 复用现有 **Ktor HTTP Client**
- **Coil** 图片加载
- **Jetpack Compose** UI
- **Room** 本地缓存

## API 设计

### GitHub Raw URL
```
https://raw.githubusercontent.com/[username]/memory-data/main/articles.json
```

### 本地缓存策略
1. 优先读取本地 Room 数据库
2. 后台请求 GitHub 更新
3. 有新数据则更新数据库并刷新 UI
4. 网络失败则显示本地缓存数据

## Android 功能特性

### 文章列表页
- 顶部 Tab 切换（全部/Android/Kotlin/Compose）
- 下拉刷新
- 上拉加载更多
- 文章卡片展示（封面、标题、摘要、标签、阅读时间）

### 文章详情
- Webview 展示原文
- 或跳转到原链接

### 小部件支持
- 推荐文章小部件
- 每小时自动更新

## 实施步骤

### 阶段 1: Python 爬虫 (当前)
- [x] 创建 crawler 项目结构
- [ ] 实现基础爬虫类
- [ ] 实现掘金爬虫
- [ ] 实现数据模型和验证
- [ ] 编写单元测试
- [ ] 本地运行验证

### 阶段 2: GitHub 自动化
- [ ] 创建 data 仓库
- [ ] 配置 GitHub Actions
- [ ] 验证定时任务

### 阶段 3: Android 集成
- [ ] 创建 articles feature
- [ ] 实现 Repository 和数据层
- [ ] 实现 UI (Compose)
- [ ] 接入 GitHub 数据源
- [ ] 添加本地缓存

### 阶段 4: 优化
- [ ] 添加更多数据源
- [ ] 性能优化
- [ ] 错误处理

## 开始实施

准备创建 Python 爬虫项目...
