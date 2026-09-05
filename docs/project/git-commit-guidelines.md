---
title: Git Commit 规范
status: 已采用
scope: Consumer-local Governance
source: agentic-dev master@394d1c3cde04b35940d5e33b7cbcaaf6557678ce
---

# Git Commit 规范

## 1. 目的

本规范统一 `jilinjobs-cms` 的 Git Commit Message，保证人工和 Agent 在 Fresh Context 中能够直接从提交历史理解主要变更目的，并避免依赖聊天记录或临时约定。

本规范选择性固化自当前 Consumer 采用的 `agentic-dev` baseline 中 `docs/guides/git-commit-guidelines.md` 的通用规则，并根据本 Consumer 已有提交历史扩展实际使用的 Type / Scope。它不继承 `agentic-dev` 自身项目事实。

## 2. 基本格式

统一采用：

```text
<type>(<scope>): <中文摘要>
```

必要时可以省略 Scope：

```text
<type>: <中文摘要>
```

规则：

- `type` 使用小写英文；
- `scope` 使用小写英文；
- 摘要以中文为主要叙述语言；
- 技术标准、代码标识、文件名、API、框架名等必要英文术语可以保留；
- 摘要直接说明主要动作和对象；
- 默认不在摘要末尾添加句号。

示例：

```text
feat(party): 增加党建正式页面入口
fix(public): 修正栏目切换异步状态
refactor(migration): 收敛 EU-31 数据库迁移基线
test(migration): 验证历史内容幂等升级
docs(method): 固化 Consumer-local 验证规则
chore(repo): 清理一次性迁移工具
```

以下写法不符合当前项目规范：

```text
docs(eu-31): define migration baseline requirements
fix migration issue
update docs
```

## 3. Type

优先复用当前仓库已经稳定使用的 Type：

| Type | 用途 |
|---|---|
| `feat` | 新增用户可见能力、业务能力或工程能力 |
| `fix` | 修复实现、配置、迁移、验证或运行问题 |
| `refactor` | 重构结构或实现，不改变既定外部语义 |
| `test` | 新增或调整测试、验证和回归证据 |
| `docs` | Requirement、Specification、Technical Plan、Method、Roadmap 等文档变化 |
| `chore` | 仓库维护、清理和一次性工具 |
| `ci` | GitHub Actions、CI / Review Workflow 调整 |
| `build` | 构建、打包及构建工具相关变化 |
| `style` | 不改变行为的格式或样式整理 |
| `data` | Consumer-owned canonical / baseline 数据变化 |

只有出现明确、持续的责任语义时才增加新的 Type。

## 4. Scope

Scope 表示稳定责任域，不表示单个文件名或一次性任务编号。

优先复用当前仓库已经形成的稳定 Scope，例如：

- `migration`
- `public`
- `party`
- `admin`
- `backend`
- `config`
- `resource`
- `review`
- `method`
- `governance`
- `project`
- `repo`

可以在确有稳定责任域时使用其他 Scope，但不得为了单个文件、临时脚本或一次性概念机械创造 Scope。

EU 编号可以出现在中文摘要中，但不建议仅把 `eu-31` 作为 Scope 代替真实责任域。

## 5. 摘要与 Body

摘要应：

- 使用中文主述；
- 以明确动作开头；
- 说明主要修改对象；
- 保持简短并具有独立可读性。

普通提交默认不要求 Body。

只有摘要无法说明重要 why、兼容性边界、迁移约束或 Trade-off 时才增加 Body。Body 同样以中文主述，不重复逐项解释 diff。

## 6. 单一变更目的

一个 Commit 应表达一个主要逻辑目的。

可以同时修改多个文件，只要这些文件共同完成同一目的。不同 Authority 层、独立实现目标或互不相关的修复应按需要拆分。

临时执行过程中可以形成中间 Commit，但在 PR 进入 Ready for Review 前应检查：

- Commit Message 是否全部符合本规范；
- 临时、试探或英文过程性提交是否需要重写 / squash；
- 最终提交结构是否仍有助于 Review 与 Fresh Context 恢复。

不得因为使用 squash merge，就忽略 Feature Branch 中明显违反项目规范的 Commit Message。

## 7. 历史重写与 Evidence

如果在 PR 合并前为了修正 Commit Message、整理临时历史或规范化 stacked ancestry 而重写 Branch：

- 最终文件树和 PR Diff 必须重新核对；
- PR Head SHA 必须重新读取确认；
- 与旧 Head 绑定的 Current Evidence 不机械继承；
- 按 `docs/project/development-method.md` 和 `docs/project/review-feedback-cycle.md` 判断受影响验证并取得必要的新 Head Evidence；
- Feature PR 仍不得未经项目负责人明确授权自行合并。

## 8. Commit 前检查

提交前至少确认：

1. 是否只有一个主要目的；
2. `type` 是否准确；
3. `scope` 是否属于稳定责任域；
4. 摘要是否以中文清楚说明动作和对象；
5. 是否混入无关临时文件、生成物或调试内容；
6. 当前变化是否达到适合提交的稳定状态；
7. 是否已执行与变更风险匹配的必要验证。

## 9. Merge 边界

本规范只约束 Commit Message 与 PR 合并前的 Commit History 收敛。

Merge Strategy、PR Title、Squash Policy、Tag、Release、Deploy 继续服从 Repository Authority 与项目负责人决策。Squash merge 的最终提交标题也应继续采用本规范的中文 Conventional Commit 形式。
