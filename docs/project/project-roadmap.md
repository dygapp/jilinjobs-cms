# 项目演进路线与当前状态

本文是 `jilinjobs-cms` Consumer Repository 的 Project Roadmap，用于记录本项目自身的演进路线、阶段状态、已完成里程碑、当前目标和下一步工作。

本文只描述 Consumer 项目事实，不继承 `dygapp/agentic-dev/docs/project/project-roadmap.md` 中的项目状态。`agentic-dev` 只提供 AI Agent 开发 Method、Operating Guide 和 Skills 知识来源。

## 使用规则

- 路线图记录项目级状态，不替代 Specification、Technical Plan 或 Execution Unit。
- 路线变化通过 Git 历史保留，不在 README、聊天或临时任务中维护第二份长期状态。
- 状态语义：
  - **已完成**：存在当前仓库可验证证据；
  - **当前**：正在推进的阶段或目标；
  - **下一步**：已经确定的近期工作；
  - **条件性后续**：需要新的真实证据后再评估。

## 方法基线

当前 AI Agent 开发方法来源：

```text
Repository:
dygapp/agentic-dev

Baseline:
master@3e0b99d85d968f138e6eae9bc51ea1b7a710748e
```

Consumer 使用原则：

- `agentic-dev` 决定如何工作（Method / Operating Guide / Skills）；
- `jilinjobs-cms` 自身文档决定产品事实、Scope 和验收要求；
- 不直接复制 `agentic-dev` 的项目事实、路线状态或实验结论；
- 当需要升级方法基线时，先更新 Consumer 中记录的 baseline，再依据新的 baseline 工作。

## 总体路线

| 路线 | 状态 | 结果 |
|---|---|---|
| Consumer Repository Bootstrap | 已完成 | 建立独立 Consumer Authority、Specification、Technical Plan 和验证边界 |
| 信息发布核心能力纵向建设 | 已完成 | 完成栏目、导航、文章、发布、公开页面、附件、浏览量和响应式能力建设 |
| Feature-wide Convergence 验证 | 已完成 | 完成首次功能范围整体验证并关闭已发现验证缺口 |
| Consumer 继续演进验证 | 当前 | 验证 Fresh Context 基于 Consumer Authority 恢复项目状态，并开展新的真实工作 |
| 新业务能力扩展 | 条件性后续 | 根据新的 Product Intent 和 Authority 增量定义 |

## 已完成里程碑

| 项目 | 证据 |
|---|---|
| Consumer Authority 建立 | `AGENTS.md`、`README.md` |
| Specification 建立 | `docs/specifications/center-main-site-core.md` |
| Technical Plan 建立 | `docs/technical/` |
| 多个 Execution Unit 完成 | PR、CI、Browser Verification 记录 |
| Feature-wide Convergence 完成 | 当前 GitHub 记录 |

## 当前阶段

当前核心目标：

> 基于已有 Consumer Repository 开展继续演进验证，不重复 Bootstrap，不机械扩展方法和 Skill。

重点验证：

1. Fresh Context 是否可以仅依靠 Consumer Repository Authority 恢复工作状态；
2. 新需求是否可以通过 Specification → Slice → Execution → Convergence 闭环推进；
3. Consumer 是否保持与 `agentic-dev` 的职责边界；
4. 验收要求是否持续关联实现责任和 Current Evidence。

## 下一步工作

1. 根据新的真实需求建立后续 Specification；
2. 必要时创建 Technical Plan；
3. 按 Execution Unit 进行独立验证；
4. 持续更新本 Roadmap 中的项目级状态。

## 不属于本 Roadmap 的内容

以下内容不在本文维护：

- 单次执行命令；
- 临时实施步骤；
- 单个文件修改过程；
- Agent 私有推理过程；
- `agentic-dev` 方法内部演进状态。

## 更新触发条件

出现以下情况时更新本文：

- 项目阶段变化；
- 重要里程碑完成；
- 当前目标变化；
- 方法 baseline 发生正式升级并影响 Consumer 使用方式；
- 新证据改变项目路线判断。
