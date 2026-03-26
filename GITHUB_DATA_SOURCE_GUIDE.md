# GitHub JSON 数据源配置指南

## 概述

使用 GitHub 托管 JSON 格式的问题数据，通过 Raw Content API 动态获取。这是最简单、最免费的方案。

## 快速开始

### 1. 创建 GitHub 仓库

1. 登录 GitHub，创建新仓库：`daily-questions`
2. 设置为公开仓库（Public）
3. 初始化 README

### 2. 创建问题数据文件

在仓库根目录创建以下文件：

#### `questions.json` - 所有问题
```json
[
  {
    "id": "q_001",
    "title": "ViewModel 的作用域应该如何选择？",
    "background": "在开发 Android 应用时...",
    "optionA": "根据生命周期需求选择",
    "optionB": "统一使用 Fragment 级别",
    "whyA": "ViewModel 的作用域选择应该基于数据共享的需求...",
    "whyNotB": "统一使用 Fragment 级别 ViewModel 看似简化了架构...",
    "relatedKnowledge": "ViewModel 生命周期、ViewModelScope",
    "practicalScenario": "Master-Detail 架构场景...",
    "category": "ANDROID_CORE",
    "difficulty": "INTERMEDIATE",
    "tags": ["android", "viewmodel", "lifecycle"],
    "metadata": {
      "id": "q_001",
      "createdAt": 1710240000000,
      "updatedAt": 1710240000000
    }
  }
]
```

#### `today.json` - 今日问题
```json
{
  "id": "q_001",
  "title": "ViewModel 的作用域应该如何选择？",
  ...
}
```

#### `ANDROID_CORE.json` - Android 核心问题
```json
[
  { "id": "q_001", ... },
  { "id": "q_002", ... }
]
```

### 3. 配置应用

编辑 `QuestionConfig.kt`：

```kotlin
object QuestionConfig {
    // 启用 GitHub 数据源
    const val USE_GITHUB_DATA_SOURCE = true

    // GitHub 配置
    const val GITHUB_USER = "your-username"
    const val GITHUB_REPO = "daily-questions"
    const val GITHUB_BRANCH = "main"
}
```

## 目录结构

```
daily-questions/
├── questions.json          # 所有问题
├── today.json              # 今日问题（可选）
├── ANDROID_CORE.json       # Android 核心分类
├── DATA_STRUCTURE.json     # 数据结构分类
├── DESIGN_PATTERN.json     # 设计模式分类
├── JAVA_KOTLIN.json        # Java/Kotlin 分类
└── README.md               # 说明文档
```

## 文件格式规范

### Question 对象格式

```json
{
  "id": "q_xxx",                    // 问题唯一标识
  "title": "问题标题",                // 15-25字
  "background": "问题背景",           // 50-100字
  "optionA": "选项A（正确答案）",     // 20-30字
  "optionB": "选项B（错误答案）",     // 20-30字
  "whyA": "为什么选择A",             // ≥400字
  "whyNotB": "为什么不选B",           // ≥400字
  "relatedKnowledge": "相关知识点",  // 可选
  "practicalScenario": "应用场景",   // 可选
  "category": "ANDROID_CORE",        // 枚举值
  "difficulty": "INTERMEDIATE",      // BEGINNER/INTERMEDIATE/ADVANCED
  "tags": ["android", "viewmodel"],  // 3-5个标签
  "metadata": {
    "id": "q_xxx",
    "createdAt": 1710240000000,     // 毫秒时间戳
    "updatedAt": 1710240000000
  }
}
```

### Category 枚举值

- `ANDROID_CORE` - Android 核心
- `DATA_STRUCTURE` - 数据结构
- `DESIGN_PATTERN` - 设计模式
- `JAVA_KOTLIN` - Java/Kotlin

### Difficulty 枚举值

- `BEGINNER` - 初级
- `INTERMEDIATE` - 中级
- `ADVANCED` - 高级

## 示例数据文件

完整的示例数据文件：

```json
[
  {
    "id": "q_001",
    "title": "ViewModel 的作用域应该如何选择？",
    "background": "在开发 Android 应用时，我们需要在不同场景下使用 ViewModel。有些开发者倾向于在 Activity 级别创建 ViewModel，而有些则在 Fragment 级别创建。",
    "optionA": "根据生命周期需求选择：跨 Fragment 共享数据用 Activity 级别，Fragment 独有数据用 Fragment 级别",
    "optionB": "统一使用 Fragment 级别 ViewModel，保持每个 Fragment 的独立性",
    "whyA": "ViewModel 的作用域选择应该基于数据共享的需求和生命周期特点...\n\n（详细内容，≥400字）",
    "whyNotB": "统一使用 Fragment 级别 ViewModel 看似简化了架构，但在实际开发中会带来诸多问题...\n\n（详细内容，≥400字）",
    "relatedKnowledge": "- ViewModel 生命周期\n- ViewModelScope\n- SavedStateHandle",
    "practicalScenario": "典型场景：Master-Detail 架构（如邮件应用）\n- 左侧邮件列表，右侧邮件详情\n- 两个 Fragment 共享选中的邮件数据",
    "category": "ANDROID_CORE",
    "difficulty": "INTERMEDIATE",
    "tags": ["android", "viewmodel", "lifecycle", "architecture"],
    "metadata": {
      "id": "q_001",
      "createdAt": 1710240000000,
      "updatedAt": 1710240000000
    }
  },
  {
    "id": "q_002",
    "title": "Context 的正确使用方式是什么？",
    "background": "Android 开发中，Context 是最常用的类之一，但也是最容易误用的。不同场景下需要不同类型的 Context。",
    "optionA": "根据生命周期选择：Application Context 用于全局单例和长生命周期对象，Activity/Service Context 用于 UI 相关和绑定生命周期的操作",
    "optionB": "统一使用 Application Context，避免内存泄漏",
    "whyA": "Context 的正确使用确实需要根据场景选择...\n\n（详细内容，≥400字）",
    "whyNotB": "统一使用 Application Context 看似简化了 Context 的使用...\n\n（详细内容，≥400字）",
    "relatedKnowledge": "- Context 继承关系\n- ApplicationContext vs ActivityContext\n- 内存泄漏",
    "practicalScenario": "单例模式中的 Context 使用：\n- 图片加载库内部自动处理 Context\n- Dialog 必须使用 Activity Context",
    "category": "ANDROID_CORE",
    "difficulty": "INTERMEDIATE",
    "tags": ["android", "context", "lifecycle", "memory"],
    "metadata": {
      "id": "q_002",
      "createdAt": 1710240000000,
      "updatedAt": 1710240000000
    }
  }
]
```

## Raw URL 格式

GitHub Raw Content URL 格式：

```
https://raw.githubusercontent.com/{user}/{repo}/{branch}/{path}
```

示例：
```
https://raw.githubusercontent.com/your-username/daily-questions/main/questions.json
```

## 更新数据

### 方法一：直接在 GitHub 网页编辑

1. 打开文件
2. 点击编辑（铅笔图标）
3. 修改内容
4. 提交更改

### 方法二：Git 命令行

```bash
git clone https://github.com/your-username/daily-questions.git
cd daily-questions
# 编辑文件
git add .
git commit -m "Add new questions"
git push
```

### 方法三：GitHub Desktop

使用 GitHub Desktop 应用图形界面操作。

## 注意事项

1. **文件编码**：确保 JSON 文件使用 UTF-8 编码
2. **JSON 格式**：确保 JSON 格式正确，可以使用 JSONLint 验证
3. **文件大小**：单个文件建议不超过 1MB
4. **更新频率**：建议每天更新一次 `today.json`
5. **版本控制**：Git 会自动保存历史版本，可以随时回滚

## 测试 API

在浏览器中测试 Raw URL：

```
https://raw.githubusercontent.com/your-username/daily-questions/main/questions.json
```

如果浏览器能正确显示 JSON 内容，说明配置成功。

## 迁移现有问题

将 `QuestionInitialData.kt` 中的问题导出为 JSON：

```kotlin
val json = Json { prettyPrint = true }
val jsonString = json.encodeToString(QuestionInitialData.getInitialQuestions())
// 将 jsonString 保存到 questions.json
```
