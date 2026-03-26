# 技术文章爬虫

自动爬取技术文章并发布到 GitHub。

## 功能特性

- ✅ 支持多个技术平台（掘金、CSDN、Medium等）
- ✅ 智能过滤 Android/Kotlin 相关文章
- ✅ 每小时自动更新（GitHub Actions）
- ✅ 数据验证和去重
- ✅ JSON 格式输出，易于集成

## 快速开始

### 安装依赖

```bash
pip install -r requirements.txt
```

### 本地运行

```bash
cd src
python main.py
```

数据将输出到 `data/articles.json`。

### GitHub 自动运行

将此项目推送到 GitHub，启用 Actions 即可每小时自动运行。

## 项目结构

```
├── src/
│   ├── crawlers/          # 爬虫实现
│   │   ├── base.py       # 基础爬虫类
│   │   └── juejin.py     # 掘金爬虫
│   ├── models/           # 数据模型
│   │   └── article.py    # 文章模型
│   └── main.py           # 主程序
├── data/
│   └── articles.json     # 生成的数据
└── .github/
    └── workflows/
        └── crawler.yml   # GitHub Actions 配置
```

## 数据格式

```json
{
  "version": "1.0",
  "last_updated": "2025-03-26T10:00:00Z",
  "sources": [...],
  "articles": [...]
}
```

详细格式见 `ARCHITECTURE.md`。

## 添加新的数据源

1. 继承 `BaseCrawler`
2. 实现 `get_articles()` 方法
3. 在 `main.py` 中注册

## Android 集成

在 Android 项目中使用 Ktor 读取生成的 JSON：

```kotlin
val url = "https://raw.githubusercontent.com/xxx/memory-data/main/articles.json"
val client = HttpClient()
val json = client.get(url).bodyAsText()
val data = Json.decodeFromString<ArticleData>(json)
```

## 许可

MIT
