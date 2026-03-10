# 应用列表管理功能 (AppList)

## 功能概述
提供完整的手机应用管理功能，包括应用列表展示、搜索、卸载、详情查看等。

## 入口
- 主页面快速访问区域的"应用"图标
- 包名：`com.quanneng.memory.features.applist.ui.AppListActivity`

## 核心功能

### 1. 应用列表展示
- 显示所有已安装的用户应用
- 支持切换显示/隐藏系统应用
- 应用图标、名称、包名、版本等信息展示
- 系统应用特殊标识

### 2. 下拉刷新
- 下拉刷新应用列表
- 刷新时显示加载指示器
- 保持当前筛选状态

### 3. 应用搜索
- 点击顶部"搜索"按钮进入搜索模式
- 支持按应用名称和包名搜索
- 实时过滤显示结果
- 大小写不敏感

### 4. 应用操作
- **单击**：进入应用详情页
- **长按**：弹出操作菜单
  - 打开应用
  - 应用详情
  - 卸载应用（用户应用）

### 5. 应用详情
- 应用图标和基本信息
- 版本信息（版本名、版本号）
- 安装时间、更新时间
- 系统应用标识
- 快速操作（打开、卸载）

### 6. 应用卸载
- 应用内二次确认弹窗
- 调用系统卸载流程
- 卸载成功后自动刷新列表

## 技术架构

### 目录结构
```
features/applist/
├── data/
│   └── AppListRepository.kt      # 数据仓库
├── model/
│   └── AppInfo.kt                 # 应用数据模型
├── ui/
│   ├── AppListActivity.kt         # Activity 入口
│   ├── AppListViewModel.kt        # ViewModel
│   ├── AppListViewModelFactory.kt # ViewModel 工厂
│   └── AppListScreen.kt           # Compose UI
└── README.applist.md              # 本文档
```

### 架构分层
- **UI 层**：AppListScreen (Compose) + AppListActivity
- **业务层**：AppListViewModel
- **数据层**：AppListRepository
- **模型层**：AppInfo

### 状态管理
- 使用 Kotlin Flow 进行响应式状态管理
- UI 状态封装在 `AppListUiState` 密封类中：
  - `Loading`：加载中
  - `Success`：成功（包含应用列表、刷新状态、搜索状态）
  - `Error`：错误（包含错误信息）
  - `AppDetail`：应用详情
  - `AppMenu`：操作菜单
  - `UninstallConfirm`：卸载确认

### 依赖注入
- 使用手动依赖注入（AppContainer）
- Repository 通过 AppContainer 单例注入
- ViewModel 通过 Factory 创建

## 使用示例

### 启动应用列表
```kotlin
val intent = Intent(context, AppListActivity::class.java)
context.startActivity(intent)
```

### 搜索应用
1. 点击顶部"搜索"按钮
2. 在搜索框中输入关键词
3. 实时显示过滤结果

### 卸载应用
1. 长按应用项
2. 点击"卸载应用"
3. 在确认弹窗中点击"卸载"
4. 系统弹出最终确认对话框
5. 确认后完成卸载

## 注意事项
- 系统应用无法卸载，操作菜单中不显示卸载选项
- 卸载操作需要用户在系统对话框中二次确认
- 应用图标使用 Coil 异步加载，支持缓存优化
