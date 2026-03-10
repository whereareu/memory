# 项目任务清单（Ralph Loop 唯一真相来源）

## 总体目标
开发 Android 应用管理功能，实现手机应用列表展示、管理和快速访问

## 当前任务

### 应用列表管理功能（AppList Feature）

- [ ] 1. 创建数据层 - AppListRepository
  - [ ] 1.1 定义 AppInfo 数据模型（packageName, label, icon, version, installTime）
  - [ ] 1.2 实现 PackageManager 封装，获取所有已安装应用
  - [ ] 1.3 实现应用卸载接口（suspend + @IoDispatcher）
  - [ ] 1.4 实现应用详情查询接口
  - [ ] 1.5 添加应用过滤逻辑（系统应用/用户应用）

- [ ] 2. 创建 ViewModel - AppListViewModel
  - [ ] 2.1 实现 Flow<State> 状态管理
  - [ ] 2.2 实现下拉刷新逻辑
  - [ ] 2.3 实现应用卸载确认与执行
  - [ ] 2.4 实现应用搜索/过滤功能
  - [ ] 2.5 处理权限请求（REQUEST_DELETE_PACKAGES）

- [ ] 3. 实现 UI 层
  - [ ] 3.1 创建 AppListActivity（仅负责导航与绑定）
  - [ ] 3.2 实现 LazyColumn（替代 RecyclerView）展示应用列表
  - [ ] 3.3 实现下拉刷新组件（PullRefreshIndicator）
  - [ ] 3.4 实现应用卡片项（图标 + 名称 + 包名 + 版本）
  - [ ] 3.5 实现长按菜单（卸载/详情/快捷方式）
  - [ ] 3.6 实现应用详情弹窗
  - [ ] 3.7 实现搜索栏
  - [ ] 3.8 优化列表性能（key、稳定参数、图片缓存）

- [ ] 4. 集成到主界面
  - [ ] 4.1 在 Main.kt 添加应用列表入口
  - [ ] 4.2 添加快速访问图标（AppsOutlined）
  - [ ] 4.3 更新导航跳转逻辑

- [ ] 5. 权限配置
  - [ ] 5.1 添加 REQUEST_DELETE_PACKAGES 权限到 AndroidManifest
  - [ ] 5.2 添加查询包使用统计权限（可选）
  - [ ] 5.3 处理 API 26+ 卸载权限兼容

- [ ] 6. 单元测试 - AppListTest.kt
  - [ ] 6.1 测试 Repository 获取应用列表
  - [ ] 6.2 测试应用过滤逻辑
  - [ ] 6.3 测试 ViewModel 状态变化
  - [ ] 6.4 测试下拉刷新 Flow
  - [ ] 6.5 测试卸载功能
  - [ ] 6.6 确保覆盖率 ≥ 80%

- [ ] 7. 文档与配套
  - [ ] 7.1 创建 README.applist（含时序图、入口说明）
  - [ ] 7.2 更新 PROGRESS.md
  - [ ] 7.3 性能测试（加载 100+ 应用流畅度）

- [ ] 8. 代码质量与优化
  - [ ] 8.1 确保单文件 public 符号 ≤ 5
  - [ ] 8.2 确保 assembleDebug ≤ 90s
  - [ ] 8.3 优化图标加载（使用 Coil 或 Glide）
  - [ ] 8.4 实现 DiffUtil 优化列表更新

## 目录结构规划
```
features/
  aplist/
    model/
      - AppInfo.kt
    data/
      - AppListRepository.kt
    ui/
      - AppListActivity.kt
      - AppListViewModel.kt
  README.applist
  AppListTest.kt
```

完成后请在回复末尾写：EXIT_SIGNAL: true
