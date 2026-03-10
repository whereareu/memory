# Claude 项目规范（Ralph Loop 专用）

## 工作流（必须严格执行）
1. 读取 TODO.md，挑选最高优先级未完成任务
2. 完整完成该任务（包括测试）
3. 更新 TODO.md（打钩 + 添加新子任务）
4. git commit -m "feat: xxx"（有意义的消息）
5. 更新 PROGRESS.md
6. 继续下一个任务
7. 只有全部任务完成时，在回复最后加上：EXIT_SIGNAL: true + DONE

## 规则
- 永远先读现有文件再改
- 代码必须可运行 + 测试通过
- 遇到不确定时优先查现有文件，不问用户
- 每完成一个循环后自动继续

## 设计原则

### SOLID 原则

| 原则 | 全称 | 要求 | 示例 |
|------|------|------|------|
| **S** | Single Responsibility | 单一职责 | 每个类/函数只做一件事，Repository 只管数据，ViewModel 只管状态 |
| **O** | Open/Closed | 开闭原则 | 通过接口扩展功能，禁止修改已验证的 core 代码 |
| **L** | Liskov Substitution | 里氏替换 | 子类可无缝替换父类，避免破坏父类契约 |
| **I** | Interface Segregation | 接口隔离 | 接口小而专注，客户端不依赖不需要的方法 |
| **D** | Dependency Inversion | 依赖倒置 | 高层模块不依赖低层，都依赖抽象（Repository 接口） |

### 核心实践原则

| 原则 | 含义 | 应用 |
|------|------|------|
| **DRY** | Don't Repeat Yourself | 相同逻辑不超过 2 次，第 3 次必须抽取 |
| **KISS** | Keep It Simple, Stupid | 优先简单方案，过度设计是技术债 |
| **YAGNI** | You Aren't Gonna Need It | 不写未来可能用的代码，只解决当前需求 |
| **Composition Over Inheritance** | 组合优于继承 | 优先组合实现复用，避免继承树爆炸 |

### 项目特定原则

1. **目录即模块**：`features/xxx/` 内禁止再分层，高内聚
2. **零 Base 类**：出现 `BaseActivity/Fragment/VM` 即红
3. **数据层隔离**：所有数据操作通过 Repository，VM 禁止直接访问数据源
4. **Flow 优先**：状态变化使用 Flow，避免回调地狱
5. **测试先行**：新 feature 必须有对应 `FeatureTest.kt`