# 项目任务清单（Ralph Loop 唯一真相来源）

---

## 当前任务：每日Android一问插件

### 总体目标
实现"每日Android一问"插件功能，每日自动更新精选Android技术问题，提供深度解析（正反对比，≥800字），涵盖Android、数据结构、设计模式、Java/Kotlin等领域，支持历史记录管理与删除操作。

---

## 待办任务

### 1. 数据模型设计
- [x] 设计 Question 数据模型（问题、答案、标签、日期、难度）
- [x] 设计 QuestionMetadata 元数据（创建时间、更新时间、是否已读）
- [x] 设计 QuestionRepository 接口
- [x] 定义问题分类枚举（Android/数据结构/设计模式/Java/Kotlin）
- [x] 定义难度级别枚举（初级/中级/高级）

### 2. 数据源与内容管理
- [x] 创建问题库（至少50道初始问题）
- [x] 实现问题内容存储（Room Database）
- [x] 实现 QuestionRepository 数据层
- [x] 添加问题导入/导出功能（JSON格式）
- [x] 实现问题去重逻辑（在 QuestionSelector 中实现）
- [x] 实现 GitHub 数据源（GitHubQuestionDataSource）
- [x] 实现智谱 AI 数据源（ZhipuQuestionDataSource）
- [x] 实现混合数据源策略（HYBRID 模式）

### 3. 每日更新机制
- [x] 实现 DailyQuestionScheduler 调度器
- [ ] 使用 WorkManager 实现每日定时任务
- [x] 实现问题选择算法（避免重复、考虑难度分布）
- [x] 添加手动刷新功能
- [ ] 实现更新通知（可选）

### 4. ViewModel 与状态管理
- [x] 创建 DailyQuestionViewModel
- [x] 设计 QuestionUiState 状态类
- [x] 实现问题加载 Flow
- [x] 实现历史记录加载 Flow
- [x] 实现删除操作状态管理

### 5. UI 层实现
- [x] 创建 DailyQuestionScreen 主界面
- [x] 实现问题卡片组件（QuestionCard）
- [x] 实现答案详情页（AnswerDetailScreen）
- [x] 实现历史记录列表（HistoryList）
- [x] 实现删除确认对话框
- [x] 添加刷新按钮和下拉刷新

### 6. 内容展示优化
- [x] 实现答案Markdown渲染（基础文本显示）
- [ ] 添加代码高亮显示（可选优化）
- [x] 实现可折叠的章节（问题/答案/分析）
- [x] 添加标签过滤功能（在 ViewModel 中实现）
- [x] 实现搜索功能（按问题/标签）

### 7. 插件化集成
- [x] 定义 QuestionPlugin 接口
- [x] 实现插件加载机制
- [x] 在主界面添加插件入口
- [ ] 实现插件配置管理
- [ ] 添加插件启用/禁用开关

### 8. 数据持久化
- [x] 实现 Room Database 建表
- [x] 创建 DAO 接口
- [ ] 实现 Database Migrations（可选优化）
- [ ] 添加数据备份/恢复功能（可选功能）
- [ ] 实现数据清理策略（可选）

### 9. 测试覆盖
- [x] Repository 单元测试
- [x] ViewModel 单元测试
- [x] Scheduler 测试
- [ ] UI 组件测试（Compose UI Test）
- [ ] 数据库测试

### 10. 文档完善
- [x] 创建 README.dailyquestion.md
- [x] 编写问题贡献指南
- [ ] 添加时序图和架构说明
- [x] 编写使用示例
- [x] 创建问题模板

---

## 问题内容规范

### 问题格式要求
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
- 为什么选择A（详细论证，≥400字）
- 为什么不选B（对比分析，≥400字）
- 相关知识点扩展
- 实际应用场景

## 相关标签
android, viewmodel, lifecycle
```

### 问题分类与分布
- **Android核心**（30%）：View系统、四大组件、生命周期、权限管理
- **数据结构**（20%）：常用数据结构、算法复杂度、Android集合框架
- **设计模式**（20%）：常用设计模式、Android架构模式
- **Java/Kotlin**（30%）：语言特性、并发编程、内存管理

### 难度分布
- 初级：基础概念，日常开发常见问题
- 中级：深入理解，需要综合分析
- 高级：原理级理解，涉及源码或性能优化

---

## 技术架构

### 目录结构
```
features/dailyquestion/
├── data/
│   ├── Question.kt              # 数据模型
│   ├── QuestionDao.kt           # 数据库访问
│   ├── QuestionDatabase.kt      # 数据库定义
│   ├── QuestionRepository.kt    # 仓库接口
│   └── QuestionRepositoryImpl.kt # 仓库实现
├── domain/
│   ├── QuestionScheduler.kt     # 每日调度器
│   └── QuestionSelector.kt      # 问题选择算法
├── presentation/
│   ├── DailyQuestionViewModel.kt
│   ├── DailyQuestionScreen.kt
│   ├── QuestionCard.kt
│   ├── AnswerDetailScreen.kt
│   └── HistoryList.kt
├── plugin/
│   ├── QuestionPlugin.kt        # 插件接口
│   └── QuestionPluginLoader.kt  # 插件加载器
└── DailyQuestionTest.kt          # 单元测试
```

### 技术栈
- **UI**: Jetpack Compose + Material3
- **数据库**: Room
- **调度**: WorkManager
- **状态管理**: Flow + StateFlow
- **Markdown渲染**: Markwon 或 Compose Markdown
- **依赖注入**: 手动 DI (AppContainer)

---

**使用说明**：
1. 任务完成后标记为 `[x]`
2. 运行 `./archive_tasks.sh` 归档已完成任务到 DONE.md
3. 运行 `./ralph.sh` 开始自动循环执行

完成后请在回复末尾写：EXIT_SIGNAL: true + DONE
